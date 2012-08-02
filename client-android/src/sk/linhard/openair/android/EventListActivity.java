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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * List of all stored events.
 * 
 * @author Michal Linhard <michal@linhard.sk>
 */

public class EventListActivity extends Activity implements OnClickListener {
   private static final String TAG = "EventListActivity";
   private ListView listEvents;
   private OpenAirApplication app;
   private ProgressDialog downloadProgressDialog;
   private Dialog urlInputDialog;
   private EditText urlInputText;

   private class EventListAdapter extends ArrayAdapter<StoredEvent> implements OnItemClickListener {

      public EventListAdapter(Context context) {
         super(context, 0, app.getStoredEvents());
      }

      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
         StoredEvent storedEvent = (StoredEvent) getItem(position);
         try {
            Intent i = new Intent(EventListActivity.this, EventDetailsActivity.class)
                  .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            i.putExtra("storedEvent", storedEvent);
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

   private void updateList() {
      EventListAdapter adapter = new EventListAdapter(this);
      listEvents.setAdapter(adapter);
      listEvents.setOnItemClickListener(adapter);
   }

   @Override
   protected void onResume() {
      super.onResume();
      updateList();
      if (getIntent() != null && getIntent().getData() != null) {
         downloadUrl(getIntent().getData().toString());
      }
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      getMenuInflater().inflate(R.menu.eventlist, menu);
      return true;
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
      case R.id.itemScanFilesystem:
         if (app.scanFilesystem()) {
            updateList();
         }
         break;
      case R.id.itemGetFromWeb:
         urlInputText = new EditText(this);
         urlInputText.setText("http://openair.linhard.sk/bla.evt.zip");
         urlInputDialog = new AlertDialog.Builder(this).setTitle("Enter event URL").setView(urlInputText)
               .setPositiveButton(R.string.textBuOK, this).setNegativeButton(R.string.textBuCancel, this).show();
         break;
      }
      return true;

   }

   @Override
   public void onClick(DialogInterface dialog, int which) {
      if (dialog == urlInputDialog) {
         if (which == DialogInterface.BUTTON_POSITIVE) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(urlInputText.getWindowToken(), 0);
            urlInputDialog.dismiss();
            urlInputDialog = null;
            downloadUrl(urlInputText.getText().toString());
         } else if (which == DialogInterface.BUTTON_NEGATIVE) {
            urlInputDialog.dismiss();
            urlInputDialog = null;
         }
      } else if (dialog == downloadProgressDialog) {
         if (which == DialogInterface.BUTTON_NEGATIVE) {
            downloadProgressDialog.dismiss();
            downloadProgressDialog = null;
         }
      }
   }

   private void downloadUrl(String anURL) {
      downloadProgressDialog = new ProgressDialog(this);
      downloadProgressDialog.setMessage("Downloading event ...");
      downloadProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
      downloadProgressDialog.setMax(20);
      downloadProgressDialog.setProgress(10);
      downloadProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.textBuCancel), this);
      downloadProgressDialog.show();
      new DownloadTask().execute(anURL);
   }

   private class DownloadTask extends AsyncTask<String, Integer, String> {

      @Override
      protected String doInBackground(String... params) {
         HttpURLConnection urlConnection = null;
         FileOutputStream fileOutput = null;
         InputStream inputStream = null;
         try {
            URL url = new URL(params[0]);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);
            urlConnection.connect();
            File tempFile = app.getNewTemporaryEventFile();
            fileOutput = new FileOutputStream(tempFile);
            inputStream = urlConnection.getInputStream();
            int totalSize = urlConnection.getContentLength();
            publishProgress(0, totalSize);
            int downloadedSize = 0;
            byte[] buffer = new byte[1024];
            int bufferLength = 0;
            while ((bufferLength = inputStream.read(buffer)) > 0) {
               fileOutput.write(buffer, 0, bufferLength);
               downloadedSize += bufferLength;
               publishProgress(downloadedSize, -1);
            }
            return tempFile.getAbsolutePath();
         } catch (Exception e) {
            Log.e(TAG, "Error while downloading event from url: " + params[0], e);
            return null;
         } finally {
            if (inputStream != null) {
               try {
                  inputStream.close();
               } catch (IOException e) {
               }
            }
            if (fileOutput != null) {
               try {
                  fileOutput.close();
               } catch (IOException e) {
               }
            }
         }
      }

      @Override
      protected void onProgressUpdate(Integer... values) {
         downloadProgressDialog.setProgress(values[0]);
         if (values[1] != -1) {
            downloadProgressDialog.setMax(values[1]);
         }
      }

      @Override
      protected void onPostExecute(String result) {
         downloadProgressDialog.dismiss();
         downloadProgressDialog = null;
         if (result != null) {
            Log.i(TAG, "Downloaded url to file " + result);
         }
         if (app.addDownloadedEvent(result)) {
            updateList();
         }
      }

   }

}
