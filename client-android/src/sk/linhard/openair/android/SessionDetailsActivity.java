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

import sk.linhard.openair.eventmodel.Session;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * 
 * Displays details about a session (performance/concert/talk).
 * 
 * @author Michal Linhard <michal@linhard.sk>
 */
public class SessionDetailsActivity extends Activity {
   private OpenAirApplication app;
   private Session session;
   private TextView textSessionDetails;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.sessiondetails);
      app = (OpenAirApplication) getApplication();
      session = (Session) getIntent().getExtras().get("session");
      textSessionDetails = (TextView) findViewById(R.id.textSessionDetails);
      StringBuffer s = new StringBuffer(session.getName());
      s.append("\n\nStart: ");
      s.append(session.getFormattedStartTime());
      s.append(" ");
      s.append(app.getDayString(session.getStart()));
      s.append("\nLocation: ");
      s.append(session.getLocation().getName());
      s.append("\n\n");
      if (session.getMetadata() != null && session.getMetadata().getDescription() != null) {
         s.append(session.getMetadata().getDescription());
      } else {
         s.append("No description available");
      }

      textSessionDetails.setText(s.toString());
   }
}
