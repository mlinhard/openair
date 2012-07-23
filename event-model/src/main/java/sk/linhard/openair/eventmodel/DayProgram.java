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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Duration;

/**
 * Day program is a collection of sessions for a certain location on a certain day.
 * 
 * @author Michal Linhard <michal@linhard.sk>
 */
public class DayProgram implements Iterable<Session>, Serializable {
   private DateTime dayStart;
   private Session[] sessions;

   private Location location;

   private class SessionIterator implements Iterator<Session> {
      private int currentPos = 0;

      @Override
      public boolean hasNext() {
         if (sessions == null) {
            return false;
         }
         return currentPos < sessions.length;
      }

      @Override
      public Session next() {
         Session result = sessions[currentPos];
         currentPos++;
         return result;
      }

      @Override
      public void remove() {
         throw new UnsupportedOperationException();
      }
   }

   DayProgram(DateTime aDayStart) {
      this.dayStart = aDayStart;
   }

   public Session addSession(Session session) {
      session.setDayProgram(this);
      if (sessions == null) {
         sessions = new Session[] { session };
         return session;
      }
      Session[] newsessions = Arrays.copyOf(sessions, sessions.length + 1);
      newsessions[sessions.length] = session;
      sessions = newsessions;

      // TODO: validation on overlay
      // this is where the logic happens, it takes into consideration whether the session is cancelled and other
      // old version or other stuff
      return session;
   }

   public Session addSession(String aTitle, DateTime aStart, Duration aDuration) {
      return addSession(new Session(aTitle, aStart, aDuration));
   }

   public DateTime getDayStart() {
      return dayStart;
   }

   @Override
   public Iterator<Session> iterator() {
      return new SessionIterator();
   }

   /**
    * Returns true iff this program contains any relevant performances with respect to the given
    * time
    * 
    * @param aTime
    *           Current time.
    * @return True if this program has any relevant performances.
    */
   public boolean hasRelevantSessions(DateTime aTime) {
      // if the last performance in a chronological list is not relevant
      // then all preceeding performances are also irrelevant.
      // therefore it is sufficient to check if the last performance is relevant
      return getLastSession().isRelevant(aTime);
   }

   public Session getLastSession() {
      return sessions[sessions.length - 1];
   }

   /**
    * Return all relevant performances with regard to given time. Relevant performance is
    * performance that is eigher running or in the future.
    * 
    * @param aTime
    *           Current time.
    * @return List of performances. May be empty.
    */
   public Session[] getRelevantSessions(DateTime aTime) {
      List<Session> result = new ArrayList<Session>();
      for (Session session : this) {
         if (session.isRelevant(aTime)) {
            result.add(session);
         }
      }
      return result.toArray(new Session[result.size()]);
   }

   public Location getLocation() {
      return location;
   }

   public void setLocation(Location location) {
      this.location = location;
   }

   public DayProgram getPreviousProgram() {
      DayProgram[] dps = getLocation().getDayPrograms();
      for (int i = 1; i < dps.length; i++) {
         if (dps[i] == this) {
            return dps[i - 1];
         }
      }
      return null;
   }

   /**
    * Filter either the old version or the new version of the program. performances will be sorted
    * by start.
    * 
    * new version contains all performances that have not changed an new versions of those that
    * changed.
    * 
    * old version contains all performances that have not changed an old versions of those that
    * changed.
    * 
    * @return old or new program version
    */
   Session[] filterSessionsVersion(boolean anOldVersion) {
      if (sessions == null) {
         return new Session[0];
      }
      int cnt = 0;
      for (int i = 0; i < sessions.length; i++) {
         Session eachP = sessions[i];
         if (eachP.hasChanges()) {
            if (anOldVersion) {
               if (eachP.isOldVersion() || eachP.isCancelled()) {
                  cnt++;
               }
            } else {
               if (eachP.isNewVersion()) {
                  cnt++;
               }
            }
         } else {
            cnt++;
         }
      }
      if (cnt == 0) {
         return null;
      }
      Session[] result = new Session[cnt];
      cnt = 0;
      for (int i = 0; i < sessions.length; i++) {
         Session eachP = sessions[i];
         if (eachP.hasChanges()) {
            if (anOldVersion) {
               if (eachP.isOldVersion() || eachP.isCancelled()) {
                  result[cnt] = eachP;
                  cnt++;
               }
            } else {
               if (eachP.isNewVersion()) {
                  result[cnt] = eachP;
                  cnt++;
               }
            }
         } else {
            result[cnt] = eachP;
            cnt++;
         }
      }
      return result;
   }

   public Session[] getSessions() {
      return sessions;
   }

   public void setSessions(Session[] sessions) {
      this.sessions = sessions;
   }
}
