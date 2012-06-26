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
import java.text.SimpleDateFormat;

import org.joda.time.DateTime;

/**
 * Domain model object for an announcement.
 * Announcement is a brief event-global message that is being shown on all displays
 * displaying anything concerning given event.
 * 
 * @author Michal Linhard <michal@linhard.sk> 
 *
 */
public class Announcement implements Serializable {
	private boolean active;
	private DateTime activeFrom;
	private DateTime activeTo;
	private String text;
	private int order;
	
	Announcement() {
		// nothing to do
	}

	/**
	 * Determine if the announcement is active in given time.
	 * 
	 * this is true iff
	 * active = true
	 * activeFrom <= aTime
	 * activeTo >= aTime
	 * @param aTime
	 * @return
	 */
	public boolean isActive(DateTime aTime) {
		if (!isActive()) {
			return false;
		}
		if (getActiveFrom() != null && getActiveFrom().isAfter(aTime)) {
			return false;
		}
		if (getActiveTo() != null && getActiveTo().isBefore(aTime)) {
			return false;
		}
		return true;
	}
	
	public boolean isActive() {
		return active;
	}
	void setActive(boolean someActive) {
		active = someActive;
	}
	public DateTime getActiveFrom() {
		return activeFrom;
	}
	void setActiveFrom(DateTime someActiveFrom) {
		activeFrom = someActiveFrom;
	}
	public DateTime getActiveTo() {
		return activeTo;
	}
	void setActiveTo(DateTime someActiveTo) {
		activeTo = someActiveTo;
	}
	public String getText() {
		return text;
	}
	void setText(String someText) {
		text = someText;
	}
	public int getOrder() {
		return order;
	}
	void setOrder(int someOrder) {
		order = someOrder;
	}
	
	@Override
	public String toString() {
		SimpleDateFormat format = new SimpleDateFormat("dd.MM HH:mm");
		StringBuffer sb = new StringBuffer();
		sb.append("(");
		sb.append(getText());
		if (!isActive()) {
			sb.append(", INACT");
		} else {
			sb.append(", ACT");
			if (getActiveFrom() != null || getActiveTo() != null) {
				sb.append(" <");
				if (getActiveFrom() != null) {
					sb.append(format.format(getActiveFrom().toDate()));
				} else {
					sb.append("-");
				}
				sb.append(", ");
				if (getActiveTo() != null) {
					sb.append(format.format(getActiveTo()));
				} else {
					sb.append("-");
				}
				sb.append(">");
			}
		}
		sb.append(")");
		return sb.toString();
	}	
}
