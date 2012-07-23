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

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Represents one item in the program for one day on one location.
 * 
 * @author Michal Linhard <michal@linhard.sk>
 */
public class Session implements Serializable {
   private static transient DateTimeFormatter START_TIME_FORMAT = DateTimeFormat.forPattern("HH:mm");

   private DayProgram dayProgram;
   private String name;
   private String shortName;
   private DateTime start;
   private Duration duration;
   private Session oldVersion;
   private Session newVersion;
   private SessionMetadata metadata;
   private boolean cancelled;

   public Session(String aTitle, DateTime aStart, Duration aDuration) {
      this.name = aTitle;
      this.shortName = null;
      this.start = aStart;
      this.duration = aDuration;
      this.oldVersion = null;
      this.newVersion = null;
      this.metadata = null;
      this.cancelled = false;
   }

   public String getUrl() {
      return metadata == null ? null : metadata.getUrl();
   }

   public void setUrl(String anUrl) {
      if (metadata == null) {
         metadata = new SessionMetadata();
      }
      metadata.setUrl(anUrl);
   }

   public String getDescription() {
      return metadata == null ? null : metadata.getDescription();
   }

   public void setDescription(String aDescription) {
      if (metadata == null) {
         metadata = new SessionMetadata();
      }
      metadata.setDescription(aDescription);
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
      if (oldVersion != null) {
         oldVersion.name = name;
      }
      if (newVersion != null) {
         newVersion.name = name;
      }
   }

   public String getShortName() {
      return shortName;
   }

   public SessionMetadata getMetadata() {
      return metadata;
   }

   public void setMetadata(SessionMetadata metadata) {
      this.metadata = metadata;
   }

   public void setShortName(String shortName) {
      this.shortName = shortName;
      if (oldVersion != null) {
         oldVersion.shortName = shortName;
      }
      if (newVersion != null) {
         newVersion.shortName = shortName;
      }
   }

   public DateTime getStart() {
      return start;
   }

   public void setStart(DateTime start) {
      this.start = start;
   }

   public Duration getDuration() {
      return duration;
   }

   public void setDuration(Duration duration) {
      this.duration = duration;
   }

   public Session getOldVersion() {
      return oldVersion;
   }

   public void setOldVersion(Session oldVersion) {
      this.oldVersion = oldVersion;
   }

   public Session getNewVersion() {
      return newVersion;
   }

   public void setNewVersion(Session newVersion) {
      this.newVersion = newVersion;
   }

   public boolean isCancelled() {
      return cancelled;
   }

   public void setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
   }

   public void cancel() {
      setCancelled(true);
   }

   public DayProgram getDayProgram() {
      return dayProgram;
   }

   void setDayProgram(DayProgram dayProgram) {
      this.dayProgram = dayProgram;
   }

   public Session change(DayProgram aDay, DateTime aStart, Duration aDuration) {
      // TODO: what if changing already changed ?
      // we don't have to support this at the moment
      DateTime newStart = aStart == null ? start : aStart;
      Duration newDuration = aDuration == null ? duration : aDuration;
      Session session = new Session(name, newStart, newDuration);
      session.setShortName(shortName);
      session.setMetadata(metadata);
      session.setOldVersion(this);
      this.setNewVersion(session);
      if (aDay == null) {
         return dayProgram.addSession(session);
      } else {
         return aDay.addSession(session);
      }
   }

   public boolean isOldVersion() {
      return newVersion != null;
   }

   public boolean isNewVersion() {
      return oldVersion != null;
   }

   public boolean hasChanges() {
      return isMoved() || isCancelled();
   }

   public boolean isMoved() {
      return (newVersion != null || oldVersion != null);
   }

   /**
    * Session is moved when it's changed and it's been moved to different day or different location.
    * 
    * @return
    */
   public boolean isMovedDifferentLocation() {
      if (newVersion != null) {
         return newVersion.dayProgram != dayProgram;
      } else if (oldVersion != null) {
         return oldVersion.dayProgram != dayProgram;
      } else {
         return false;
      }
   }

   public DateTime getEnd() {
      return start.plus(duration);
   }

   /**
    * Returns true iff this performance is relevant with respect to given time. A performance is
    * relevant when it is either running or in future.
    * 
    * @param aTime
    *           Current time.
    * @return True iff relevant.
    */
   public boolean isRelevant(DateTime aTime) {
      return aTime.isBefore(getEnd());
   }

   public boolean isRunning(DateTime aNow) {
      return !start.isAfter(aNow) && !getEnd().isBefore(aNow);
   }

   public Location getLocation() {
      return getDayProgram().getLocation();
   }

   public String getFormattedStartTime() {
      return START_TIME_FORMAT.print(start);
   }
}
