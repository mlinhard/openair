/*
 * OpenAir Event Program Model
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
package sk.linhard.openair.eventmodel.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.StringTokenizer;

import org.joda.time.DateTime;
import org.joda.time.Duration;

/**
 * Utilities.
 * 
 * @author Michal Linhard <michal@linhard.sk>
 */
public class Util {
   public static final String XML_DATETIME_FORMAT = "dd-MM-yyyy HH:mm";
   public static final String XML_DATE_FORMAT = "dd-MM-yyyy";
   public static final long MILIS_IN_SEC = 1000;
   public static final long MILIS_IN_MIN = 60 * MILIS_IN_SEC;
   public static final long MILIS_IN_HOUR = 60 * MILIS_IN_MIN;

   public static DateTime dateTime(String aString) throws ParseException {
      SimpleDateFormat theFormat = new SimpleDateFormat(XML_DATETIME_FORMAT);
      theFormat.setTimeZone(Calendar.getInstance().getTimeZone());
      return new DateTime(theFormat.parse(aString));
   }

   public static DateTime date(String aString) throws ParseException {
      SimpleDateFormat theFormat = new SimpleDateFormat(XML_DATE_FORMAT);
      theFormat.setTimeZone(Calendar.getInstance().getTimeZone());
      return new DateTime(theFormat.parse(aString));
   }

   public static String[] tokenize(String aString, String aDelimiter) {
      StringTokenizer theTokenizer = new StringTokenizer(aString, aDelimiter);
      String[] theResult = new String[theTokenizer.countTokens()];
      int cnt = 0;
      while (theTokenizer.hasMoreTokens()) {
         theResult[cnt++] = theTokenizer.nextToken();
      }
      return theResult;
   }

   public static Duration duration(String aDuration) {
      String[] theTokens = tokenize(aDuration, ":");
      if (theTokens.length != 2) {
         return null;
      }
      try {
         long hours = new Long(theTokens[0]);
         long mins = new Long(theTokens[1]);
         return new Duration(hours * MILIS_IN_HOUR + mins * MILIS_IN_MIN);
      } catch (NumberFormatException theException) {
         return null;
      }
   }

   public static String formatDateTime(DateTime aDateTime) {
      if (aDateTime == null) {
         return null;
      }
      SimpleDateFormat theFormat = new SimpleDateFormat(XML_DATETIME_FORMAT);
      theFormat.setTimeZone(Calendar.getInstance().getTimeZone());
      return theFormat.format(aDateTime.toDate());
   }

   public static String formatDate(DateTime aDateTime) {
      if (aDateTime == null) {
         return null;
      }
      SimpleDateFormat theFormat = new SimpleDateFormat(XML_DATE_FORMAT);
      theFormat.setTimeZone(Calendar.getInstance().getTimeZone());
      return theFormat.format(aDateTime.toDate());
   }

   public static String formatDuration(Duration duration) {
      if (duration == null) {
         return null;
      }
      long miliseconds = duration.getMillis();
      long hours = miliseconds / MILIS_IN_HOUR;
      long mins = (miliseconds % MILIS_IN_HOUR) / MILIS_IN_MIN;
      StringBuffer theSB = new StringBuffer();
      theSB.append(hours);
      theSB.append(":");
      if (mins < 10) {
         theSB.append("0");
      }
      theSB.append(mins);
      return theSB.toString();
   }

   public static Object clone(Object anObj) throws IOException, ClassNotFoundException {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ObjectOutputStream os = new ObjectOutputStream(bos);
      os.writeObject(anObj);
      os.close();
      ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
      Object ret = is.readObject();
      is.close();
      return ret;
   }
}
