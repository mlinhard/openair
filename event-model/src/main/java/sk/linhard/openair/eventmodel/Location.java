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
package sk.linhard.openair.eventmodel;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;

import org.joda.time.DateTime;

/**
 * Represents a location the session takes place at.
 * 
 * @author Michal Linhard <michal@linhard.sk>
 */
public class Location implements Iterable<DayProgram>, Serializable {
   private Event event;

   private String name;
   private String shortName;
   private DayProgram[] dayPrograms;
   private LocationMetadata metadata;

   private class DayProgramIterator implements Iterator<DayProgram> {
      private int currentPos = 0;

      @Override
      public boolean hasNext() {
         if (dayPrograms == null) {
            return false;
         }
         return currentPos < dayPrograms.length;
      }

      @Override
      public DayProgram next() {
         DayProgram result = dayPrograms[currentPos];
         currentPos++;
         return result;
      }

      @Override
      public void remove() {
         throw new UnsupportedOperationException();
      }
   }

   public Location(String aName) {
      this.name = aName;
      this.shortName = null;
      this.metadata = null;
   }

   public DayProgram addDay(DateTime aDayStart) {
      DayProgram dayProgram = new DayProgram(aDayStart);
      dayProgram.setLocation(this);
      if (dayPrograms == null) {
         dayPrograms = new DayProgram[] { dayProgram };
         return dayProgram;
      }
      DayProgram[] newdayPrograms = Arrays.copyOf(dayPrograms, dayPrograms.length + 1);
      newdayPrograms[dayPrograms.length] = dayProgram;
      dayPrograms = newdayPrograms;
      return dayProgram;
   }

   public String getUrl() {
      return metadata == null ? null : metadata.getUrl();
   }

   public void setUrl(String anUrl) {
      if (metadata == null) {
         metadata = new LocationMetadata();
      }
      metadata.setUrl(anUrl);
   }

   public String getDescription() {
      return metadata == null ? null : metadata.getDescription();
   }

   public void setDescription(String aDescription) {
      if (metadata == null) {
         metadata = new LocationMetadata();
      }
      metadata.setDescription(aDescription);
   }

   public String getName() {
      return name;
   }

   public String getShortName() {
      return shortName;
   }

   public void setShortName(String shortName) {
      this.shortName = shortName;
   }

   public void setName(String name) {
      this.name = name;
   }

   @Override
   public Iterator<DayProgram> iterator() {
      return new DayProgramIterator();
   }

   public LocationMetadata getMetadata() {
      return metadata;
   }

   public void setMetadata(LocationMetadata metadata) {
      this.metadata = metadata;
   }

   public DayProgram findDayProgram(DateTime startDate) {
      for (DayProgram d : this) {
         if (d.getDayStart().equals(startDate)) {
            return d;
         }
      }
      return null;
   }

   public Event getEvent() {
      return event;
   }

   public void setEvent(Event event) {
      this.event = event;
   }

   /**
    * Returns the first day program which has some relevant performances with respect to current
    * time.
    * 
    * @param aTime
    *           current time.
    * 
    * @return first relevant day program. may be null.
    */
   public DayProgram getFirstRelevantDayProgram(DateTime aTime) {
      for (DayProgram eachProg : dayPrograms) {
         if (eachProg.hasRelevantSessions(aTime)) {
            return eachProg;
         }
      }
      return null;
   }

   public DayProgram[] getDayPrograms() {
      return dayPrograms;
   }

   public void setDayPrograms(DayProgram[] dayPrograms) {
      this.dayPrograms = dayPrograms;
   }

}
