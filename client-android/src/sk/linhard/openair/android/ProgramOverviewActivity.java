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

import sk.linhard.openair.eventmodel.DayProgram;
import sk.linhard.openair.eventmodel.Event;
import sk.linhard.openair.eventmodel.Session;
import sk.linhard.openair.eventmodel.Location;
import sk.linhard.openair.eventmodel.util.Util;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

/**
 * Shows the program overview of the whole event.
 * 
 * @author mlinhard
 * 
 */
public class ProgramOverviewActivity extends Activity {

    private static final String TAG = "ProgramOverviewActivity";

    private TextView textProgram;
    private OpenAirApplication app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.programoverview);
        textProgram = (TextView) findViewById(R.id.textProgram);
        app = (OpenAirApplication) getApplication();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Event activeEvent = app.getActiveEvent();
        if (activeEvent == null) {
            textProgram.setText("No active event!");
            // switch to event overview
            Log.e(TAG, "Didn't find active event");
        } else {
            StringBuffer s = new StringBuffer();
            s.append("Event: ");
            s.append(activeEvent.getName());
            s.append("\n");
            for (Location location : activeEvent) {
                s.append("\nProgram for ");
                s.append(location.getName());
                for (DayProgram dp : location) {
                    s.append("\n  Day ");
                    s.append(Util.formatDate(dp.getDayStart()));
                    for (Session session : dp) {
                        s.append("\n    ");
                        s.append(Util.formatDateTime(session.getStart()));
                        s.append(" ");
                        s.append(session.getName());
                    }

                }
            }
            textProgram.setText(s.toString());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.programoverview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
        case R.id.itemEvents:
            startActivity(new Intent(this, EventListActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
            break;
        }
        return true;
    }
}
