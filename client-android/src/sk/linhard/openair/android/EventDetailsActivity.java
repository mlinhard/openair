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
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 
 * Event details
 * 
 * @author Michal Linhard <michal@linhard.sk>
 */
public class EventDetailsActivity extends Activity implements OnClickListener {
   private OpenAirApplication app;
   private StoredEvent storedEvent;
   private TextView textSessionDetails;
   private Button buSetAsActive;
   private Button buDelete;
   private Button buUpdate;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.eventdetails);
      app = (OpenAirApplication) getApplication();
      storedEvent = (StoredEvent) getIntent().getExtras().get("storedEvent");
      if (storedEvent == null) {
         throw new IllegalStateException("No event defined!");
      }
      if (storedEvent.getEvent() == null) {
         if (!app.load(storedEvent)) {
            Toast.makeText(this, "Unable to load event " + storedEvent.getName(), Toast.LENGTH_LONG).show();
            finish();
         }
      }
      textSessionDetails = (TextView) findViewById(R.id.textEventDetails);
      buDelete = (Button) findViewById(R.id.buDelete);
      buUpdate = (Button) findViewById(R.id.buUpdate);
      buSetAsActive = (Button) findViewById(R.id.buSetAsActive);
      buDelete.setOnClickListener(this);
      buUpdate.setOnClickListener(this);
      buSetAsActive.setOnClickListener(this);
      StringBuffer s = new StringBuffer(storedEvent.getEvent().getName());
      s.append("\n\n");
      if (storedEvent.getEvent().getMetadata() != null && storedEvent.getEvent().getMetadata().getDescription() != null) {
         s.append(storedEvent.getEvent().getMetadata().getDescription());
      } else {
         s.append("No description available");
      }

      textSessionDetails.setText(s.toString());
   }

   @Override
   public void onClick(View v) {
      if (buDelete == v) {
         if (!app.delete(storedEvent)) {
            Log.e(TAG, "Couldn't remove stored event " + storedEvent + " from internal database");
         }
         finish();
         startActivity(new Intent(EventDetailsActivity.this, EventListActivity.class)
               .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
      } else if (buUpdate == v) {
         Toast.makeText(this, "Not yet implemented", Toast.LENGTH_SHORT).show();
      } else if (buSetAsActive == v) {
         app.setAsActive(storedEvent);
         finish();
         startActivity(new Intent(EventDetailsActivity.this, OverviewActivity.class)
               .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
      } else {
         Log.e(TAG, "Unexpected click source");
      }
   }

   @Override
   protected void onDestroy() {
      super.onDestroy();
      // deserialized event can be garbage collected if it's not the active one
      if (!storedEvent.isActive()) {
         storedEvent.setEvent(null);
      }
   }

   private static final String TAG = "EventDetails";
}
