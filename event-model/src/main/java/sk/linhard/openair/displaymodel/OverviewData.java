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

import java.util.Arrays;

import org.joda.time.DateTime;

/**
 * Data for displaying programme overview.
 * 
 * @author Michal Linhard
 * 
 */
public class OverviewData {
   private DateTime time;
   private DateTime nextDisplayChange;
   private OverviewDataSessionItem[] items;
   private DateTime dateNotice;

   public OverviewDataSessionItem[] getItems() {
      return items;
   }

   public void setItems(OverviewDataSessionItem[] someItems) {
      items = someItems;
   }

   public DateTime getTime() {
      return time;
   }

   public void setTime(DateTime someTime) {
      time = someTime;
   }

   public DateTime getNextDisplayChange() {
      return nextDisplayChange;
   }

   public void setNextDisplayChange(DateTime someNextDisplayChange) {
      nextDisplayChange = someNextDisplayChange;
   }

   public DateTime getDateNotice() {
      return dateNotice;
   }

   public void setDateNotice(DateTime someFutureTime) {
      dateNotice = someFutureTime;
   }

   @Override
   public String toString() {
      return Arrays.asList(items).toString();
   }

}
