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

import java.text.ParseException;
import java.util.ArrayList;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import sk.linhard.openair.displaymodel.OverviewData;
import sk.linhard.openair.displaymodel.OverviewDataSessionItem;
import sk.linhard.openair.displaymodel.OverviewDisplay;
import sk.linhard.openair.eventmodel.Session;
import sk.linhard.openair.eventmodel.util.Util;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Shows the program overview of the whole event.
 * 
 * @author Michal Linhard <michal@linhard.sk>
 * 
 */
public class OverviewActivity extends Activity {

   private Runnable updateListView = new Runnable() {
      @Override
      public void run() {
         updateList();
      }
   };
   private Runnable updateDebugClock = new Runnable() {
      @Override
      public void run() {
         updateDebugClock();
      }
   };

   private static Object[] toItems(OverviewData data) {
      if (data == null) {
         return new Object[0];
      }
      ArrayList<Object> result = new ArrayList<Object>();
      for (OverviewDataSessionItem item : data.getItems()) {
         result.add(item);
         for (Session itemSession : item.getSessions()) {
            result.add(itemSession);
         }
      }
      return result.toArray();
   }

   private class OverviewAdapter extends ArrayAdapter<Object> implements OnItemClickListener {

      public OverviewAdapter(Context context, OverviewData data) {
         super(context, 0, toItems(data));
      }

      @Override
      public View getView(int position, View convertView, ViewGroup parent) {
         Object item = getItem(position);
         if (item.getClass() == OverviewDataSessionItem.class) {
            OverviewDataSessionItem item1 = (OverviewDataSessionItem) item;
            View view = LayoutInflater.from(getContext()).inflate(R.layout.overview_row_location, parent, false);
            TextView textOverviewLocation = (TextView) view.findViewById(R.id.textOverviewLocation);
            textOverviewLocation.setText(item1.getLocation().getName());
            return view;
         } else if (item.getClass() == Session.class) {
            Session item1 = (Session) item;
            View view = LayoutInflater.from(getContext()).inflate(R.layout.overview_row_session, parent, false);
            TextView textOverviewSession = (TextView) view.findViewById(R.id.textOverviewSession);
            if (item1.isRunning(app.getShiftedTime())) {
               textOverviewSession.setText("\u25b6" + item1.getFormattedStartTime() + " " + item1.getName());
               textOverviewSession.setTextColor(Color.WHITE);
            } else {
               textOverviewSession.setText(item1.getFormattedStartTime() + " " + item1.getName());
            }
            return view;
         } else {
            throw new IllegalStateException("Unexpected item type!");
         }

      }

      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
         Object item = getItem(position);
         if (item.getClass() == OverviewDataSessionItem.class) {
            OverviewDataSessionItem item1 = (OverviewDataSessionItem) item;
            Intent i = new Intent(OverviewActivity.this, LocationProgramActivity.class)
                  .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            i.putExtra("location", item1.getLocation());
            startActivity(i);
         } else if (item.getClass() == Session.class) {
            Session item1 = (Session) item;
            Intent i = new Intent(OverviewActivity.this, SessionDetailsActivity.class)
                  .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            i.putExtra("session", item1);
            startActivity(i);
         } else {
            throw new IllegalStateException("Unexpected item type!");
         }
      }

   }

   private static final String TAG = "OverviewActivity";

   private OpenAirApplication app;
   private ListView listOverview;
   private TextView debugClock;
   private OverviewData displayData;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.overview);
      app = (OpenAirApplication) getApplication();
      listOverview = (ListView) findViewById(R.id.listOverview);
      debugClock = (TextView) LayoutInflater.from(this).inflate(R.layout.debug_clock, null);
      debugClock.setVisibility(app.isShowDebugTime() ? View.VISIBLE : View.INVISIBLE);
      final WindowManager mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
      debugClock.post(new Runnable() {

         public void run() {
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams(LayoutParams.WRAP_CONTENT,
                  LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_APPLICATION,
                  WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                  PixelFormat.TRANSLUCENT);
            lp.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
            mWindowManager.addView(debugClock, lp);
         }
      });
   }

   public void updateDebugClock() {
      debugClock.setText(app.getShiftedTimeFormatted());
      debugClock.postDelayed(updateDebugClock, 200);
   }

   private void cleanAndExitToEventList() {
      displayData = null;
      OverviewAdapter adapter = new OverviewAdapter(this, displayData);
      listOverview.setAdapter(adapter);
      listOverview.setOnItemClickListener(adapter);
      Toast.makeText(this, getString(R.string.textNoActiveEvent), Toast.LENGTH_SHORT).show();
      finish();
      startActivity(new Intent(this, EventListActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
   }

   private void updateList() {
      Log.d(TAG, "Updating overview");
      StoredEvent activeEvent = app.getActiveEvent();
      if (activeEvent == null) {
         cleanAndExitToEventList();
      } else if (!app.load(activeEvent)) {
         app.unsetActive(activeEvent);
         cleanAndExitToEventList();
      } else {
         displayData = new OverviewDisplay(activeEvent.getEvent()).getData(app.getShiftedTime(), 2, activeEvent
               .getEvent().getLocationNames(), 60);
         OverviewAdapter adapter = new OverviewAdapter(this, displayData);
         listOverview.setAdapter(adapter);
         listOverview.setOnItemClickListener(adapter);
         sheduleUpdateAt(displayData.getNextDisplayChange());
      }

   }

   private void sheduleUpdateAt(DateTime aTime) {
      if (aTime == null) {
         return;
      }
      long millisDelay = new Duration(app.getShiftedTime(), aTime).getMillis();
      Log.d(TAG, "Scheduling next update for " + aTime + " that's in " + millisDelay + " ms");
      if (millisDelay <= 0) {
         updateList();
      } else {
         listOverview.removeCallbacks(updateListView);
         listOverview.postDelayed(updateListView, millisDelay);
      }
   }

   protected static DateTime dateTime(String aString) {
      try {
         return Util.dateTime(aString);
      } catch (ParseException e) {
         return null;
      }
   }

   @Override
   protected void onResume() {
      super.onResume();
      updateList();
      if (app.isShowDebugTime()) {
         debugClock.setVisibility(View.VISIBLE);
         updateDebugClock();
      } else {
         debugClock.removeCallbacks(updateDebugClock);
         debugClock.setVisibility(View.INVISIBLE);
      }
   }

   @Override
   protected void onPause() {
      super.onPause();
      debugClock.removeCallbacks(updateDebugClock);
      listOverview.removeCallbacks(updateListView);
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
      case R.id.itemDebugTime:
         showDialog(DIALOG_DEBUG_TIME);
         break;
      case R.id.itemDebugNextSwitch:
         app.setShiftedTime(displayData.getNextDisplayChange());
         updateList();
         break;
      case R.id.itemToggleDebugTime:
         if (app.isShowDebugTime()) {
            app.setShowDebugTime(false);
            item.setChecked(false);
            debugClock.setVisibility(View.INVISIBLE);
            debugClock.removeCallbacks(updateDebugClock);
         } else {
            app.setShowDebugTime(true);
            item.setChecked(true);
            debugClock.setVisibility(View.VISIBLE);
            updateDebugClock();
         }
         break;
      }
      return true;

   }

   @Override
   protected Dialog onCreateDialog(int id, Bundle args) {
      if (id == DIALOG_DEBUG_TIME) {
         LayoutInflater factory = LayoutInflater.from(this);
         final View textEntryView = factory.inflate(R.layout.debug_time_dialog, null);
         final EditText editBox = (EditText) textEntryView.findViewById(R.id.editDebugTime);
         editBox.setText(app.getShiftedTimeFormatted());
         if (args != null) {
            String unparceable = args.getString("unparceable");
            if (unparceable != null) {
               editBox.setText(unparceable);
               editBox.setTextColor(Color.RED);
            }
         }
         return new AlertDialog.Builder(this).setTitle(R.string.textDebugTime).setView(textEntryView)
               .setPositiveButton(R.string.textDebugTimeSet, new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int whichButton) {
                     String textToParse = editBox.getText().toString();
                     DateTime shiftedTime = null;
                     try {
                        shiftedTime = OpenAirApplication.DEBUG_TIME_FORMAT.parseDateTime(textToParse);
                     } catch (Exception e) {
                     }
                     if (shiftedTime == null) {
                        Bundle bundle = new Bundle();
                        bundle.putString("unparceable", textToParse);
                        showDialog(DIALOG_DEBUG_TIME, bundle);
                     } else {
                        app.setShiftedTime(shiftedTime);
                        Log.d(TAG, "Shifted time set to: " + app.getShiftedTimeFormatted());
                        listOverview.removeCallbacks(updateListView);
                        listOverview.post(updateListView);
                     }
                  }
               }).create();
      } else {
         return super.onCreateDialog(id);
      }
   }

   private static final int DIALOG_DEBUG_TIME = 1;

}
