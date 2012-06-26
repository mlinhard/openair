/*
 * OpenAir Android Client
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
package sk.linhard.openair.android;

import android.content.Context;
import android.database.Cursor;
import android.widget.SimpleCursorAdapter;

public class StoredEventAdapter extends SimpleCursorAdapter {
	static final String[] from = { StoredEventDBHelper.C_NAME }; // <2>
	static final int[] to = { R.id.eventName }; // <3>

	// Constructor
	public StoredEventAdapter(Context context, Cursor c) { // <4>
		super(context, R.layout.eventlistrow, c, from, to);
	}

}
