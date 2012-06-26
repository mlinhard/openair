/*
 * OpenAir Event Program Model
 * Copyright (C) 2010 Michal Linhard <michal@linhard.sk>
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
package sk.linhard.openair.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.junit.Test;

import sk.linhard.openair.eventmodel.DayProgram;
import sk.linhard.openair.eventmodel.Event;
import sk.linhard.openair.eventmodel.Session;
import sk.linhard.openair.eventmodel.Location;
import sk.linhard.openair.eventmodel.util.ModelMarshaller;
import sk.linhard.openair.eventmodel.util.Util;

/**
 * Tests the model.
 *
 * @author Michal Linhard <michal@linhard.sk> 
 */
public class ModelTestCase {
	private Event createTestEvent() throws Exception {
		Event e = new Event();
		e.setName("Super Event");
		Location stageA = e.addLocation("Stage A");
		DayProgram stageAday1 = stageA.addDay(dateTime("01-01-2010 0:00"));
		stageAday1.addSession("Performance A1", dateTime("01-01-2010 10:00"), duration("1:00"));
		stageAday1.addSession("Performance A2", dateTime("01-01-2010 11:30"), duration("1:00"));
		stageAday1.addSession("Performance A3", dateTime("01-01-2010 13:00"), duration("1:00"));

		DayProgram stageAday2 = stageA.addDay(dateTime("02-01-2010 0:00"));
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
		Session b5 = stageBday2.addSession("Performance B5", dateTime("02-01-2010 11:30"), duration("1:00"));
		Session b6 = stageBday2.addSession("Performance B6", dateTime("02-01-2010 13:00"), duration("2:00"));
		
		Session b5new = b5.change(null, dateTime("02-01-2010 15:30"), duration("1:00"));
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
	protected static String[] tokenize(String aString, String aDelimiter) {
		return Util.tokenize(aString, aDelimiter);
	}
	protected static Duration duration(String aDuration) throws Exception {
		return Util.duration(aDuration);
	}
	
	@Test
	public void testSerialization() throws Exception {
		Event e = createTestEvent(); 
		new File("target/testdata").mkdirs();
		ModelMarshaller.marshall(e, new FileOutputStream("target/testdata/eventoutput.xml"));
		ModelMarshaller.unmarshall(new FileInputStream("target/testdata/eventoutput.xml"));
		ModelMarshaller.marshallZip(e, new FileOutputStream("target/testdata/eventoutput.zip"));
		ModelMarshaller.unmarshallZip(new FileInputStream("target/testdata/eventoutput.zip"));
	}
}
