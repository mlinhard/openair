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
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.joda.time.DateTime;

import sk.linhard.openair.eventmodel.exceptions.EventModelException;
import sk.linhard.openair.eventmodel.util.Util;

/**
 * The root class of the model. Container for all the event program data.
 * 
 * @author Michal Linhard <michal@linhard.sk> 
 */
public class Event implements Iterable<Location>, Serializable {
	private String uri;
	private String name;
	private String shortName;
	private Long version;
	private DateTime versionTime;
	private EventMetadata metadata;
	private Location[] locations;
	private Announcement[] announcements;
	
	public Location addLocation(String aName) {
		if (findLocation(aName) != null) {
			throw new EventModelException(EventModelException.DUPLICATE_LOCATION_NAME);
		}
		Location location = new Location(aName);
		location.setEvent(this);
		if (locations == null) {
			locations = new Location[] { location };
			return location;
		}
		Location[] newarray = Arrays.copyOf(locations, locations.length + 1);
		newarray[locations.length] = location;
		locations = newarray;
		return location;
	}
	
	public Location findLocation(String aName) {
		for (Location s : this) {
			if (aName.equals(s.getName())) {
				return s;
			}
		}
		return null;
	}
	
	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public DateTime getVersionTime() {
		return versionTime;
	}

	public void setVersionTime(DateTime versionTime) {
		this.versionTime = versionTime;
	}

	
	private class LocationIterator implements Iterator<Location> {
		private int currentPos = 0;
		@Override
		public boolean hasNext() {
			return locations != null && currentPos < locations.length;
		}
		@Override
		public Location next() {
			Location result = locations[currentPos];
			currentPos++;
			return result;
		}
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	@Override
	public Iterator<Location> iterator() {
		return new LocationIterator();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Location[] getLocations() {
		return locations;
	}

	public void setLocations(Location[] locations) {
		this.locations = locations;
	}	
	

	public String getUrl() {
		return metadata == null ? null : metadata.getUrl();
	}

	public void setUrl(String anUrl) {
		if (metadata == null) {
			if (anUrl == null) {
				return;
			}
			metadata = new EventMetadata();
		}
		metadata.setUrl(anUrl);
	}

	public String getDescription() {
		return metadata == null ? null : metadata.getDescription();
	}

	public void setDescription(String aDescription) {
		if (metadata == null) {
			if (aDescription == null) {
				return;
			}
			metadata = new EventMetadata();
		}
		metadata.setDescription(aDescription);
	}
	
	public EventMetadata getMetadata() {
		return metadata;
	}
	
	@SuppressWarnings("unchecked")
	public List<DateTime> getDates() {
		HashSet<DateTime> dates = new HashSet<DateTime>();
		for (Location s : this) {
			for (DayProgram d : s) {
				if (!dates.contains(d.getDayStart())) {
					dates.add(d.getDayStart());
				}	
			}
		}
		List<DateTime> dateList = new ArrayList<DateTime>(dates);
		Collections.sort(dateList);
		return dateList;
	}
	
	public String[] getLocationNames() {
		String[] names = new String[locations.length];
		for (int i = 0; i < locations.length; i++) {
			names[i] = locations[i].getName();
		}
		return names;
	}
	
	/**
	 * Get programs for different locations on the same day.
	 */
	public List<DayProgram> getDayProgramsByDate(DateTime startDate) {
		List<DayProgram> ret = new ArrayList<DayProgram>();
		for (Location s : this) {
			for (DayProgram d : s) {
				if (d.getDayStart().equals(startDate)) {
					ret.add(d);
				}
			}
		}
		return ret;
	}
	
	/**
	 * anOldVersion == true :
	 * Returns the clone of the original version of the event, where
	 * all changes are discarded and only original versions of all changes are left.
	 * anOldVersion == false : 
	 * Returns the clone of the original version of th event, where there
	 * are no changes and the planned model corresponds to the actual model
	 * of the original model.
	 * 
	 * @param anOldVersion flag telling whether old or new version is to be returned
	 * @return Event clone stripped to the new or old version.
	 */
	public Event getReducedVersion(boolean anOldVersion) {
		Event reduced;
		try {
			reduced = (Event) Util.clone(this);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		for (Location s : reduced) {
			reduceToVersion(s, anOldVersion);
		}
		return reduced;
	}
	
	private void reduceToVersion(Location s, boolean anOldVersion) {
		List<DayProgram> dplist = new ArrayList<DayProgram>(s.getDayPrograms().length);
		for (DayProgram d : s) {
			Session[] reduced = d.filterSessionsVersion(anOldVersion);
			if (reduced != null && reduced.length > 0) {
				stripChanges(reduced);
				d.setSessions(reduced);
				dplist.add(d);
			}
		}
		s.setDayPrograms(dplist.toArray(new DayProgram[dplist.size()]));
	}
	
	private void stripChanges(Session[] plist) {
		for (Session p : plist) {
			p.setNewVersion(null);
			p.setOldVersion(null);
			p.setCancelled(false);
		}
	}
	
	public boolean hasActiveAnnouncements(DateTime aTime) {
		if (!hasAnnouncements()) {
			return false;
		}
		for (Announcement eachAnn : announcements) {
			if (eachAnn.isActive(aTime)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean hasAnnouncements() {
		return announcements != null && announcements.length > 0;
	}

	public String getActiveAnnouncementsString(DateTime aTime) {
		if (!hasAnnouncements()) {
			return "";
		}
		StringBuffer sb = new StringBuffer();
		for (Announcement eachAnn : announcements) {
			if (eachAnn.isActive(aTime)) {
				sb.append(eachAnn.getText());
				sb.append(" \u2730 ");
			}
		}
		return sb.toString();
	}

	public Announcement[] getAnnouncements() {
		return announcements;
	}

	public void setAnnouncements(Announcement[] announcements) {
		this.announcements = announcements;
	}
}
