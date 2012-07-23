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
import java.io.FileOutputStream;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import sk.linhard.openair.eventmodel.DayProgram;
import sk.linhard.openair.eventmodel.Event;
import sk.linhard.openair.eventmodel.Location;
import sk.linhard.openair.eventmodel.Session;
import sk.linhard.openair.eventmodel.util.ModelMarshaller;
import sk.linhard.openair.eventmodel.util.Util;
import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
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
   private Event activeEvent;
   private Duration timeShift;

   @Override
   public void onCreate() {
      super.onCreate();

      File eventsStorage = getEventsDir();

      if (!eventsStorage.exists()) {
         if (!eventsStorage.mkdirs()) {
            Log.e(TAG, "Couldn't create dir " + eventsStorage.getAbsolutePath());
         }
      }

      if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
         Log.e(TAG, "External storage not accessible for writing");
      }

      storedEventsDBHelper = new StoredEventDBHelper(this);

      // SQLiteDatabase db = storedEventsDBHelper.getWritableDatabase();
      // db.delete(StoredEventDBHelper.TABLE, null, null);
      //
      storedEvents = storedEventsDBHelper.getList();
      StoredEvent activeEvent = getActiveStoredEvent();
      if (activeEvent != null) {
         try {
            this.activeEvent = load(activeEvent);
         } catch (Exception e) {
            Log.e(TAG, "Error loading active event", e);
         }
      } else {
         try {
            createTestStoredEvent();
            storedEvents = storedEventsDBHelper.getList();
            activeEvent = getActiveStoredEvent();
            try {
               this.activeEvent = load(activeEvent);
            } catch (Exception e) {
               Log.e(TAG, "Error loading active event", e);
            }
         } catch (Exception e) {
            Log.e(TAG, "Error creating test event", e);
         }
      }
      try {
         setShiftedTime(dateTime("10-08-2012 15:00"));
      } catch (Exception e) {
         Log.e(TAG, "Error setting debug time", e);
      }
   }

   public Event load(StoredEvent storedEvent) throws Exception {
      FileInputStream fis = null;
      try {
         fis = new FileInputStream(storedEvent.getPath());
         return ModelMarshaller.unmarshallZip(fis);
      } finally {
         if (fis != null) {
            fis.close();
         }
      }
   }

   private File getEventsDir() {
      return new File(new File(Environment.getExternalStorageDirectory(), "OpenAir"), "events");
   }

   private void createTestStoredEvent() throws Exception {
      File eventsDir = getEventsDir();
      File testEventFile = new File(eventsDir, "test_event.zip");
      FileOutputStream fos = new FileOutputStream(testEventFile);

      Event testEvent = createTestEvent2();
      ModelMarshaller.marshallZip(testEvent, fos);
      fos.close();

      SQLiteDatabase db = storedEventsDBHelper.getWritableDatabase();

      StoredEvent e1 = new StoredEvent();
      e1.setActive(true);
      e1.setName("Grape Festival 2012");
      e1.setPath(testEventFile.getAbsolutePath());
      e1.setUri("http://openair.linhard.sk/events/test_event");

      StoredEventDBHelper.insert(db, e1);
      db.close();
   }

   private Event createTestEvent() throws Exception {
      Event e = new Event();
      e.setName("Super Event");
      Location locationA = e.addLocation("Stage A");
      DayProgram stageAday1 = locationA.addDay(dateTime("01-01-2010 0:00"));
      stageAday1.addSession("Performance A1", dateTime("01-01-2010 10:00"), duration("1:00"));
      stageAday1.addSession("Performance A2", dateTime("01-01-2010 11:30"), duration("1:00"));
      stageAday1.addSession("Performance A3", dateTime("01-01-2010 13:00"), duration("1:00"));

      DayProgram stageAday2 = locationA.addDay(dateTime("02-01-2010 0:00"));
      stageAday2.addSession("Performance A4", dateTime("02-01-2010 10:00"), duration("1:00"));
      stageAday2.addSession("Performance A5", dateTime("02-01-2010 11:30"), duration("1:00"));
      stageAday2.addSession("Performance A6", dateTime("02-01-2010 13:00"), duration("2:00"));

      Location stageB = e.addLocation("Stage B");
      DayProgram stageBday1 = stageB.addDay(dateTime("01-01-2010 0:00"));
      stageBday1.addSession("Performance B1", dateTime("01-01-2010 10:00"), duration("1:00"));
      stageBday1.addSession("Performance B2", dateTime("01-01-2010 11:30"), duration("1:00"));
      stageBday1.addSession("Performance B3", dateTime("01-01-2010 13:00"), duration("1:00"));

      DayProgram stageBday2 = stageB.addDay(dateTime("02-01-2010 0:00"));
      stageBday2.addSession("Performance B4", dateTime("02-01-2010 10:00"), duration("1:00"));
      // Session b5 = stageBday2.addSession("Performance B5",
      // dateTime("02-01-2010 11:30"), duration("1:00"));
      Session b6 = stageBday2.addSession("Performance B6", dateTime("02-01-2010 13:00"), duration("2:00"));

      // Session b5new = b5.change(null, dateTime("02-01-2010 15:30"),
      // duration("1:00"));
      b6.cancel();

      Location stageC = e.addLocation("Stage C");
      DayProgram stageCday1 = stageC.addDay(dateTime("01-01-2010 0:00"));
      stageCday1.addSession("Performance C1", dateTime("01-01-2010 10:00"), duration("1:00"));
      stageCday1.addSession("Performance C2", dateTime("01-01-2010 11:30"), duration("1:00"));
      stageCday1.addSession("Performance C3", dateTime("01-01-2010 13:00"), duration("1:00"));

      return e;
   }

   private Event createTestEvent2() throws Exception {
      Event e = new Event();
      e.setName("Grape Festival 2012");
      Location o2Stage = e.addLocation("O2 STAGE");
      DayProgram o2Stage1 = o2Stage.addDay(dateTime("10-08-2012 0:00"));
      o2Stage1.addSession("FUNNY FACES", dateTime("10-08-2012 15:00"), duration("1:00"));
      o2Stage1.addSession("RARA AVIS", dateTime("10-08-2012 16:30"), duration("1:00"));
      o2Stage1.addSession("WOLF GANG (UK)", dateTime("10-08-2012 18:30"), duration("1:00"));
      o2Stage1.addSession("BLOOD RED SHOES (UK)", dateTime("10-08-2012 20:30"), duration("1:00"));
      o2Stage1.addSession("THE SUBWAYS (UK)", dateTime("10-08-2012 22:30"), duration("1:00"));
      o2Stage1.addSession("THE BLOODY BEETROOTS (IT)", dateTime("11-08-2012 00:30"), duration("1:00"));

      DayProgram o2Stage2 = o2Stage.addDay(dateTime("11-08-2012 0:00"));
      o2Stage2.addSession("THE CELLMATES", dateTime("11-08-2012 12:00"), duration("1:00"));
      o2Stage2.addSession("GOT BLUE BALLS", dateTime("11-08-2012 13:30"), duration("1:00"));
      o2Stage2.addSession("A BANQUET (CZ)", dateTime("11-08-2012 15:00"), duration("1:00"));
      o2Stage2.addSession("NEW IVORY (UK)", dateTime("11-08-2012 16:50"), duration("1:00"));
      o2Stage2.addSession("TATA BOJS (CZ)", dateTime("11-08-2012 18:40"), duration("1:00"));
      o2Stage2.addSession("PARA", dateTime("11-08-2012 20:30"), duration("1:00"));
      o2Stage2.addSession("MORCHEEBA (UK)", dateTime("11-08-2012 22:30"), duration("1:00"));
      o2Stage2.addSession("EXAMPLE (UK)", dateTime("12-08-2012 00:45"), duration("1:00"));

      Location seatIbizaStage = e.addLocation("SEAT IBIZA STAGE");

      DayProgram seatIbizaStage1 = seatIbizaStage.addDay(dateTime("10-08-2012 0:00"));
      seatIbizaStage1.addSession("VEC + LIVĚ BAND", dateTime("10-08-2012 17:30"), duration("1:00"));
      seatIbizaStage1.addSession("PRAGO UNION (CZ)", dateTime("10-08-2012 19:30"), duration("1:00"));
      seatIbizaStage1.addSession("LE PAYACO", dateTime("10-08-2012 21:30"), duration("1:00"));
      seatIbizaStage1.addSession("NOISECUT", dateTime("10-08-2012 23:50"), duration("1:00"));
      seatIbizaStage1.addSession("PUDING PANI ELVISOVEJ", dateTime("11-08-2012 01:30"), duration("1:00"));

      DayProgram seatIbizaStage2 = seatIbizaStage.addDay(dateTime("11-08-2012 0:00"));
      seatIbizaStage2.addSession("AIRFARE (CZ)", dateTime("11-08-2012 14:20"), duration("1:00"));
      seatIbizaStage2.addSession("REPUBLIC OF TWO (CZ)", dateTime("11-08-2012 16:00"), duration("1:00"));
      seatIbizaStage2.addSession("BILLY BARMAN", dateTime("11-08-2012 17:50"), duration("1:00"));
      seatIbizaStage2.addSession("THE TWILIGHT SAD", dateTime("11-08-2012 19:40"), duration("1:00"));
      seatIbizaStage2.addSession("LAVAGANCE", dateTime("11-08-2012 21:30"), duration("1:00"));
      seatIbizaStage2.addSession("MODESTEP (UK)", dateTime("11-08-2012 23:45"), duration("1:00"));
      seatIbizaStage2.addSession("SKYLINE (CZ)", dateTime("12-08-2012 02:00"), duration("1:00"));
      seatIbizaStage2.addSession("LIXX", dateTime("12-08-2012 03:00"), duration("1:00"));
      seatIbizaStage2.addSession("OLIS BAKULU & SLIGHT", dateTime("12-08-2012 04:30"), duration("1:00"));

      Location reddsStage = e.addLocation("REDD´S STAGE");
      DayProgram reddsStage1 = reddsStage.addDay(dateTime("10-08-2012 0:00"));
      reddsStage1.addSession("THE BRIGHT EYE", dateTime("10-08-2012 17:30"), duration("1:00"));
      reddsStage1.addSession("MAKE MY HEART EXPLODE (CZ)", dateTime("10-08-2012 19:30"), duration("1:00"));
      reddsStage1.addSession("PLEASE THE TREES (CZ)", dateTime("10-08-2012 21:30"), duration("1:00"));
      reddsStage1.addSession("PURIST", dateTime("10-08-2012 23:50"), duration("1:00"));
      reddsStage1.addSession("DJ KATO (CZ)", dateTime("11-08-2012 02:00"), duration("1:00"));
      reddsStage1.addSession("DJ MARO (CZ)", dateTime("11-08-2012 03:00"), duration("1:00"));

      DayProgram reddsStage2 = reddsStage.addDay(dateTime("11-08-2012 0:00"));
      reddsStage2.addSession("TBA", dateTime("10-08-2012 10:00"), duration("1:00"));
      reddsStage2.addSession("HAF & BEYUZ NA DEČKE", dateTime("10-08-2012 12:50"), duration("1:00"));
      reddsStage2.addSession("LUS3", dateTime("10-08-2012 16:00"), duration("1:00"));
      reddsStage2.addSession("WALTER SCHNITZELSSON", dateTime("10-08-2012 17:50"), duration("1:00"));
      reddsStage2.addSession("VOODOOYOUDO", dateTime("10-08-2012 19:20"), duration("1:00"));
      reddsStage2.addSession("TORNÁDO LUE", dateTime("10-08-2012 21:30"), duration("1:00"));
      reddsStage2.addSession("DIEGO", dateTime("10-08-2012 23:45"), duration("1:00"));

      return e;
   }

   protected static DateTime dateTime(String aString) throws Exception {
      return Util.dateTime(aString);
   }

   protected static Duration duration(String aDuration) throws Exception {
      return Util.duration(aDuration);
   }

   public StoredEvent getActiveStoredEvent() {
      for (StoredEvent se : storedEvents) {
         if (se.isActive()) {
            return se;
         }
      }
      return null;
   }

   public Event getActiveEvent() {
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
}
