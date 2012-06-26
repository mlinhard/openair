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

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class StoredEventDBHelper extends SQLiteOpenHelper {
    static final String TAG = "StoredEventDBHelper";
    static final String DB_NAME = "storedevents.db"; // <2>
    static final int DB_VERSION = 2; // <3>
    static final String TABLE = "storedevents"; // <4>
    static final String C_ID = BaseColumns._ID;
    static final String C_NAME = "name";
    static final String C_URI = "uri";
    static final String C_PATH = "path";
    static final String C_VERSION = "version";
    static final String C_ACTIVE = "active";
    Context context;

    public StoredEventDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table " + TABLE + " (" + C_ID + " int primary key, " + C_NAME + " text, " + C_URI + " text, " + C_PATH + " text, " + C_VERSION
                + " int, " + C_ACTIVE + " int)";

        db.execSQL(sql);

        Log.d(TAG, "onCreated sql: " + sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { // <8>

        db.execSQL("drop table if exists " + TABLE);
        Log.d(TAG, "onUpdated");
        onCreate(db);
    }

    public List<StoredEvent> getList() {
        SQLiteDatabase db = null;
        try {
            db = getReadableDatabase();
            Cursor c = db.query(TABLE, null, C_ACTIVE + " = 1", null, null, null, null);
            List<StoredEvent> rr = new ArrayList<StoredEvent>(c.getCount());
            while (c.moveToNext()) {
                StoredEvent r = new StoredEvent();
                r.setId(c.getString(c.getColumnIndex(C_ID)));
                r.setName(c.getString(c.getColumnIndex(C_NAME)));
                r.setPath(c.getString(c.getColumnIndex(C_PATH)));
                r.setActive(c.getInt(c.getColumnIndex(C_ACTIVE)) == 1);
                r.setUri(c.getString(c.getColumnIndex(C_URI)));
                r.setVersion(c.getLong(c.getColumnIndex(C_VERSION)));
                rr.add(r);
            }
            return rr;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public static ContentValues toContentValues(StoredEvent e) {
        ContentValues cv = new ContentValues();
        cv.put(StoredEventDBHelper.C_NAME, e.getName());
        cv.put(StoredEventDBHelper.C_PATH, e.getPath());
        cv.put(StoredEventDBHelper.C_URI, e.getUri());
        cv.put(StoredEventDBHelper.C_VERSION, e.getVersion());
        cv.put(StoredEventDBHelper.C_ACTIVE, e.isActive() ? 1 : 0);
        return cv;
    }

    public static long insert(SQLiteDatabase db, StoredEvent e) {
        return db.insert(TABLE, null, toContentValues(e));
    }

}
