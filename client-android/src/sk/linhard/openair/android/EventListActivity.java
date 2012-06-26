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

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

public class EventListActivity extends Activity implements OnClickListener {
    private ListView listEvents;
    private StoredEventAdapter storedEventAdapter;
    private Cursor cursor;
    private SQLiteDatabase db;
    private StoredEventDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.eventlist);
        listEvents = (ListView) findViewById(R.id.listEvents);
        dbHelper = new StoredEventDBHelper(this);
        db = dbHelper.getReadableDatabase();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cursor = db.query(StoredEventDBHelper.TABLE, null, null, null, null, null, null);
        startManagingCursor(cursor);
        storedEventAdapter = new StoredEventAdapter(this, cursor);
        listEvents.setAdapter(storedEventAdapter);
    }

    @Override
    public void onClick(View v) {

    }
}
