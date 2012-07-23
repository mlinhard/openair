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

import sk.linhard.openair.eventmodel.Event;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * List of all stored events.
 * 
 * @author mlinhard
 */
public class EventListActivity extends Activity {
   private static final String TAG = "EventListActivity";
   private ListView listEvents;
   private OpenAirApplication app;

   private class EventListAdapter extends ArrayAdapter<StoredEvent> implements OnItemClickListener {

      public EventListAdapter(Context context) {
         super(context, 0, app.getStoredEvents());
      }

      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
         StoredEvent storedEvent = (StoredEvent) getItem(position);
         try {
            Event event = app.load(storedEvent);
            Intent i = new Intent(EventListActivity.this, EventDetailsActivity.class)
                  .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            i.putExtra("event", event);
            startActivity(i);
         } catch (Exception e) {
            Log.e(TAG, "Error loading event", e);
            Toast.makeText(EventListActivity.this, "Error loading event.", Toast.LENGTH_LONG).show();
         }
      }

      @Override
      public View getView(int position, View convertView, ViewGroup parent) {
         StoredEvent storedEvent = (StoredEvent) getItem(position);
         View view = LayoutInflater.from(getContext()).inflate(R.layout.eventlistrow, parent, false);
         TextView textEventTitle = (TextView) view.findViewById(R.id.textEventTitle);
         textEventTitle.setText(storedEvent.getName());
         return view;
      }

   }

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.eventlist);
      listEvents = (ListView) findViewById(R.id.listEvents);
      app = (OpenAirApplication) getApplication();
   }

   @Override
   protected void onResume() {
      super.onResume();
      EventListAdapter adapter = new EventListAdapter(this);
      listEvents.setAdapter(adapter);
      listEvents.setOnItemClickListener(adapter);
   }
}
