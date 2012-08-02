/*
 * OpenAir Android Client
 * Copyright (C) 2012 Michal Linhard <michal@linhard.sk>
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package sk.linhard.openair.android;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import sk.linhard.openair.eventmodel.util.ModelMarshaller;
import android.app.Application;
import android.os.Environment;
import android.util.Log;

/**
 * 
 * The OpenAir application
 * 
 * @author Michal Linhard <michal@linhard.sk>
 * 
 */
public class OpenAirApplication extends Application {
   private static final String TAG = "OpenAirApplication";

   private StoredEventDBHelper storedEventsDBHelper;
   private List<StoredEvent> storedEvents;
   private StoredEvent activeEvent;
   private Duration timeShift;
   private boolean showDebugTime;

   @Override
   public void onCreate() {
      super.onCreate();
      storedEventsDBHelper = new StoredEventDBHelper(this);
      // uncomment if you need to reset DB
      //      SQLiteDatabase db = storedEventsDBHelper.getWritableDatabase();
      //      storedEventsDBHelper.onUpgrade(db, 0, 0);
      //      db.close();
      updateStoredEvents();
      File eventsStorage = getEventsDir();

      if (!eventsStorage.exists()) {
         if (!eventsStorage.mkdirs()) {
            Log.e(TAG, "Couldn't create dir " + eventsStorage.getAbsolutePath());
         }
      }

      if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
         Log.e(TAG, "External storage not accessible for writing");
      }

   }

   private void updateStoredEvents() {
      try {
         storedEvents = storedEventsDBHelper.getAll();
         Collections.sort(storedEvents);
         activeEvent = getActiveStoredEvent();
      } catch (Exception e) {
         Log.e(TAG, "Error while updating list of stored events", e);
      }
   }

   public boolean load(StoredEvent storedEvent) {
      File f = new File(storedEvent.getPath());
      if (!f.exists()) {
         return false;
      }
      FileInputStream fis = null;
      try {
         fis = new FileInputStream(f);
         storedEvent.setEvent(ModelMarshaller.unmarshallZip(fis));
         return true;
      } catch (Exception e) {
         Log.e(TAG, "Error while loading " + storedEvent, e);
         return false;
      } finally {
         if (fis != null) {
            try {
               fis.close();
            } catch (IOException e) {
               Log.e(TAG, "Error while closing stream", e);
            }
         }
      }
   }

   public File getNewTemporaryEventFile() throws IOException {
      return File.createTempFile("Event-download-", ".zip", getEventsDir());
   }

   private File getEventsDir() {
      return new File(new File(Environment.getExternalStorageDirectory(), "OpenAir"), "events");
   }

   public StoredEvent getActiveStoredEvent() {
      for (StoredEvent se : storedEvents) {
         if (se.isActive()) {
            return se;
         }
      }
      return null;
   }

   public StoredEvent getActiveEvent() {
      return activeEvent;
   }

   public DateTime getShiftedTime() {
      DateTime r = new DateTime();
      if (timeShift == null) {
         return r;
      } else {
         return r.plus(timeShift);
      }
   }

   public String getShiftedTimeFormatted() {
      return DEBUG_TIME_FORMAT.print(getShiftedTime());
   }

   public void setShiftedTime(DateTime newNow) {
      DateTime now = new DateTime();
      timeShift = new Duration(now, newNow);
   }

   public static DateTimeFormatter DEBUG_TIME_FORMAT = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss");

   private static final int[] DAY_OF_WEEK = new int[] { R.string.textMonday, R.string.textTuesday,
         R.string.textWednesday, R.string.textThursday, R.string.textFriday, R.string.textSaturday, R.string.textSunday };

   private static DateTimeFormatter DAY_FORMAT = DateTimeFormat.forPattern("d. MMM");

   public String getDayString(DateTime time) {
      String dayString = getString(DAY_OF_WEEK[time.getDayOfWeek()]);
      return dayString + " " + DAY_FORMAT.print(time);
   }

   public List<StoredEvent> getStoredEvents() {
      return storedEvents;
   }

   public boolean isShowDebugTime() {
      return showDebugTime;
   }

   public void setShowDebugTime(boolean showDebugTime) {
      this.showDebugTime = showDebugTime;
   }

   public boolean delete(StoredEvent e) {
      try {
         storedEvents.remove(e);
         return storedEventsDBHelper.delete(e) == 1;
      } catch (Exception e1) {
         Log.e(TAG, "Error while deleting " + e, e1);
         return false;
      }
   }

   public boolean setAsActive(StoredEvent e) {
      try {
         if (activeEvent == null) {
            activeEvent = e;
            e.setActive(true);
            return storedEventsDBHelper.update(e) == 1;
         } else if (activeEvent.equals(e)) {
            Log.i(TAG, e + " is already active");
            return true;
         } else {
            activeEvent.setActive(false);
            e.setActive(true);
            boolean up1 = storedEventsDBHelper.update(activeEvent) == 1;
            boolean up2 = storedEventsDBHelper.update(e) == 1;
            return up1 && up2;
         }
      } catch (Exception e1) {
         Log.e(TAG, "Error while updating an event");
         return false;
      }
   }

   /**
    * 
    * Scan the filesystem for new events.
    * 
    * @return
    */
   public boolean scanFilesystem() {
      Set<String> knownPaths = new HashSet<String>();
      Set<String> knownNames = new HashSet<String>();
      for (StoredEvent s : storedEvents) {
         knownPaths.add(s.getPath());
         knownNames.add(s.getName());
      }
      File eventsDir = getEventsDir();
      File[] eventDirFiles = eventsDir.listFiles();
      if (eventDirFiles == null) {
         Log.e(TAG, "Failed to obtain listing for " + eventsDir.getAbsolutePath());
         return false;
      } else {
         boolean eventsAdded = false;
         for (File eventDirFile : eventDirFiles) {
            if (!knownPaths.contains(eventDirFile.getAbsolutePath())) {
               StoredEvent newEvent = addEventFromPath(knownNames, eventDirFile.getAbsolutePath());
               if (newEvent != null) {
                  knownPaths.add(eventDirFile.getAbsolutePath());
                  eventsAdded = true;
                  Log.i(TAG, "Found " + newEvent);
               }
            }
         }
         if (eventsAdded) {
            updateStoredEvents();
         }
         return eventsAdded;
      }
   }

   private StoredEvent addEventFromPath(Set<String> knownNames, String path) {
      StoredEvent unknownEvent = new StoredEvent();
      unknownEvent.setPath(path);
      if (load(unknownEvent)) {
         String nameCandidate = unknownEvent.getEvent().getName();
         while (knownNames.contains(nameCandidate)) {
            nameCandidate = nextNameCandidate(nameCandidate);
         }
         unknownEvent.setName(nameCandidate);
         unknownEvent.setVersion(unknownEvent.getEvent().getVersion());
         unknownEvent.setUri(unknownEvent.getEvent().getUri());
         try {
            storedEventsDBHelper.insert(unknownEvent);
            knownNames.add(unknownEvent.getName());
            return unknownEvent;
         } catch (Exception e) {
            Log.e(TAG, "Error while inserting " + unknownEvent, e);
            return null;
         }
      } else {
         return null;
      }
   }

   public boolean addDownloadedEvent(String path) {
      Set<String> knownNames = new HashSet<String>();
      for (StoredEvent s : storedEvents) {
         knownNames.add(s.getName());
      }
      if (addEventFromPath(knownNames, path) != null) {
         updateStoredEvents();
         return true;
      } else {
         return false;
      }
   }

   private String nextNameCandidate(String name) {
      String[] t = name.split(" ");
      if (t.length == 1) {
         return name + " 2";
      } else {
         Integer i = null;
         try {
            i = Integer.valueOf(t[t.length - 1]);
         } catch (Exception e) {
            // ignore
         }
         if (i == null) {
            return name + " 2";
         } else {
            return name.substring(0, name.lastIndexOf(t[t.length - 1])) + (i + 1);
         }
      }
   }

   public boolean unsetActive(StoredEvent e) {
      try {
         if (e.equals(activeEvent)) {
            activeEvent = null;
         }
         e.setActive(false);
         return storedEventsDBHelper.update(e) == 1;
      } catch (Exception e1) {
         Log.e(TAG, "Error while updating " + e, e1);
         return false;
      }
   }

}
