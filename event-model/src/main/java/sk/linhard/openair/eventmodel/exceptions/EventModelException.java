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
package sk.linhard.openair.eventmodel.exceptions;

/**
 * Error in event model. Thrown if some of the simple model constraints are violated.
 * 
 * @author Michal Linhard <michal@linhard.sk>
 */
public class EventModelException extends RuntimeException {
   public static final int DUPLICATE_LOCATION_NAME = 10;

   private int errorCode;

   public EventModelException(int anErrorCode) {
      this.errorCode = anErrorCode;
   }

   public int getErrorCode() {
      return errorCode;
   }
}
