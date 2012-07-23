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

import sk.linhard.openair.eventmodel.Location;
import sk.linhard.openair.eventmodel.Session;

/**
 * Data for one item (location) in a program overview.
 * 
 * @author Michal Linhard
 * 
 */
public class OverviewDataSessionItem {
   private Session[] sessions;
   private boolean active;
   private OverviewData parent;
   private Location location;

   public Session[] getSessions() {
      return sessions;
   }

   public void setSessions(Session[] someSessions) {
      sessions = someSessions;
   }

   public boolean isActive() {
      return active;
   }

   public void setActive(boolean someActive) {
      active = someActive;
   }

   public boolean isRunning() {
      if (sessions.length == 0) {
         return false;
      }
      return sessions[0].isRunning(getParent().getTime());
   }

   public OverviewData getParent() {
      return parent;
   }

   public void setParent(OverviewData someParent) {
      parent = someParent;
   }

   public Location getLocation() {
      return location;
   }

   public void setLocation(Location someLocation) {
      location = someLocation;
   }

   @Override
   public String toString() {
      StringBuffer sb = new StringBuffer(location.getName());
      sb.append("\n");
      for (Session session : sessions) {
         sb.append(session.getFormattedStartTime());
         sb.append(" - ");
         sb.append(session.getName());
         sb.append("\n");
      }
      return sb.toString();
   }
}
