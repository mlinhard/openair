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
package sk.linhard.openair.displaymodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joda.time.DateTime;

import sk.linhard.openair.eventmodel.DayProgram;
import sk.linhard.openair.eventmodel.Event;
import sk.linhard.openair.eventmodel.Location;
import sk.linhard.openair.eventmodel.Session;

/**
 * Displays overview of the program. This means showing a subset (or all) of locations with the
 * nearest X upcoming sessions.
 * 
 * @author Michal Linhard
 * 
 */
public class OverviewDisplay {

   private Event event;
   private Event eventNewVersion;

   public OverviewDisplay(Event anEvent) {
      event = anEvent;
   }

   private Event getEventNewVersion() {
      if (eventNewVersion == null && event != null) {
         eventNewVersion = event.getReducedVersion(false);
      }
      return eventNewVersion;
   }

   private Location mapToNewVersion(String aLocation) {
      return getEventNewVersion().findLocation(aLocation);
   }

   /**
    * Finds the minimal day from collection of day programs.
    * 
    * @param dayPrograms
    *           Day program array, may contain nulls.
    * @return minimal day or null if there is no program left.
    */
   private DateTime getMinDayDate(DayProgram[] dayPrograms) {
      if (dayPrograms == null || dayPrograms.length == 0) {
         return null;
      }
      DateTime result = null;
      for (DayProgram dp : dayPrograms) {
         if (dp != null) {
            if (result == null) {
               result = dp.getDayStart();
            } else {
               if (dp.getDayStart().isBefore(result)) {
                  result = dp.getDayStart();
               }
            }
         }
      }
      return result;
   }

   private DayProgram[] getFirstRelevantDayPrograms(Location[] locations, DateTime aTime) {
      DayProgram[] result = new DayProgram[locations.length];
      for (int i = 0; i < locations.length; i++) {
         result[i] = locations[i].getFirstRelevantDayProgram(aTime);
      }
      return result;
   }

   private Location[] getMappedLocations(String[] someLocations) {
      Location[] mapped = new Location[someLocations.length];
      for (int i = 0; i < someLocations.length; i++) {
         mapped[i] = mapToNewVersion(someLocations[i]);
      }
      return mapped;
   }

   public OverviewData getData(DateTime aTime, int maxSessionCount, String[] someLocations,
         int nextDayProgramNoticePeriod) {
      Location[] locations = getMappedLocations(someLocations);
      DayProgram[] dayPrograms = getFirstRelevantDayPrograms(locations, aTime);
      OverviewData result = new OverviewData();
      List<OverviewDataSessionItem> items = new ArrayList<OverviewDataSessionItem>();
      DateTime minday = getMinDayDate(dayPrograms);
      DateTime nextDisplayChange = null;
      for (int i = 0; i < dayPrograms.length; i++) {
         DayProgram dp = dayPrograms[i];
         OverviewDataSessionItem item = new OverviewDataSessionItem();
         if (dp != null && dp.getDayStart().equals(minday)) {
            item.setActive(true);
            Session[] relp = dp.getRelevantSessions(aTime);
            if (relp.length > maxSessionCount) {
               relp = Arrays.copyOfRange(relp, 0, maxSessionCount);
            }
            item.setSessions(relp);
            if (nextDisplayChange == null) {
               nextDisplayChange = getNextChange(dp, aTime);
            } else {
               DateTime dpndch = getNextChange(dp, aTime);
               if (dpndch.isBefore(nextDisplayChange)) {
                  nextDisplayChange = dpndch;
               }
            }
         } else {
            item.setActive(false);
            item.setSessions(new Session[0]);
         }
         item.setParent(result);
         item.setLocation(locations[i]);
         items.add(item);
      }
      result.setTime(aTime);
      result.setNextDisplayChange(nextDisplayChange);
      result.setItems(items.toArray(new OverviewDataSessionItem[items.size()]));
      if (minday != null) {
         setLocationOverviewDateNotice(minday, result, dayPrograms, nextDayProgramNoticePeriod);
      }
      return result;
   }

   private void setLocationOverviewDateNotice(DateTime minday, OverviewData aData, DayProgram[] dayPrograms,
         int nextDayProgramNoticePeriod) {
      DateTime endOfPreviousDay = getEndOfPreviousDayMax(minday, dayPrograms);
      DateTime dateNoticeSwitchOff = endOfPreviousDay == null ? null : endOfPreviousDay
            .plusMinutes(nextDayProgramNoticePeriod);
      boolean dayNotStarted = aData.getTime().isBefore(minday);
      // if the day program hasn't started yet, we have to
      // display date notice, to let viewers know that program displayed
      // isn't for today
      boolean previousProgramJustEnded = (dateNoticeSwitchOff != null)
            && (dateNoticeSwitchOff.isAfter(aData.getTime()));
      // 60 minutes (nextDayProgramNoticePeriod) hasn't passed from the end of
      // last day's program, viewers need time to realise that the program
      // being displayed
      // is already for the next (declared) day

      if (dayNotStarted || previousProgramJustEnded) {
         aData.setDateNotice(minday);
         if (previousProgramJustEnded) {
            if (dateNoticeSwitchOff.isBefore(minday)) {
               dateNoticeSwitchOff = minday;
            }
            if (aData.getNextDisplayChange().isAfter(dateNoticeSwitchOff)) {
               aData.setNextDisplayChange(dateNoticeSwitchOff);
            }
         }
      }
   }

   private DateTime getEndOfPreviousDayMax(DateTime minday, DayProgram[] dayPrograms) {
      DateTime endOfPreviousDay = null;
      for (int i = 0; i < dayPrograms.length; i++) {
         DayProgram dp = dayPrograms[i];
         if (dp != null && dp.getDayStart().equals(minday)) {
            DayProgram prevDp = dp.getPreviousProgram();
            if (prevDp != null) {
               if (endOfPreviousDay == null) {
                  endOfPreviousDay = prevDp.getLastSession().getEnd();
               } else if (prevDp.getLastSession().getEnd().isAfter(endOfPreviousDay)) {
                  endOfPreviousDay = prevDp.getLastSession().getEnd();
               }
            }
         }
      }
      return endOfPreviousDay;
   }

   /**
    * compute the next point in time when the display displaying this day program will need to
    * update it's time dependent data.
    * 
    * @param aTime
    *           Current time.
    * @return time of next change or null
    */
   private DateTime getNextChange(DayProgram dp, DateTime aTime) {
      if (!dp.hasRelevantSessions(aTime)) {
         return null;
      }
      Session relp = dp.getRelevantSessions(aTime)[0];
      if (relp.isRunning(aTime)) {
         return relp.getEnd();
      }
      return relp.getStart();
   }

}
