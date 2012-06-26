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

        Event testEvent = createTestEvent();
        ModelMarshaller.marshallZip(testEvent, fos);
        fos.close();

        SQLiteDatabase db = storedEventsDBHelper.getWritableDatabase();

        StoredEvent e1 = new StoredEvent();
        e1.setActive(true);
        e1.setName("Test Event");
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
}
