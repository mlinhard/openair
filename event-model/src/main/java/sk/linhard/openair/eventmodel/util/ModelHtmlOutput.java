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
package sk.linhard.openair.eventmodel.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.List;

import org.joda.time.DateTime;

import sk.linhard.openair.eventmodel.DayProgram;
import sk.linhard.openair.eventmodel.Event;
import sk.linhard.openair.eventmodel.Session;
import sk.linhard.openair.eventmodel.Location;

/**
 * Provides HTML output of the whole program.
 * 
 * @author Michal Linhard <michal@linhard.sk>
 */
public class ModelHtmlOutput {
   /**
    * Creates html files in the specified directory
    * 
    * /data/htmloutput.css /event.html
    * 
    * @param e
    *           Event to dump
    * @param aDirectory
    *           Directory to dump to.
    * @throws IOException
    */
   public static void htmpOutput(Event e, String aDirectory) throws IOException {
      File outDir = new File(aDirectory);
      File dataDir = new File(outDir, "data");
      dataDir.mkdirs();
      InputStream is = ModelHtmlOutput.class.getResourceAsStream("/htmloutput.css");
      write(is, new File(dataDir, "htmloutput.css"));
      write(new ByteArrayInputStream(toHtml(e).getBytes("UTF-8")), new File(outDir, "event.html"));
   }

   private static void write(InputStream is, File outFile) throws IOException {
      outFile.createNewFile();
      FileOutputStream fos = new FileOutputStream(outFile);
      byte[] buffer = new byte[1024];
      int len;
      while ((len = is.read(buffer)) != -1) {
         fos.write(buffer, 0, len);
      }

      is.close();
      fos.close();
   }

   /**
    * Produces string containing the HTML.
    */
   private static String toHtml(Event e) {
      StringBuffer sb = new StringBuffer(
            "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
      sb.append("<html lang=\"en-US\" xml:lang=\"en-US\" xmlns=\"http://www.w3.org/1999/xhtml\">");
      sb.append("<head>");
      sb.append("<title>");
      sb.append(e.getName());
      sb.append("</title>");
      sb.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />");
      sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"data/htmloutput.css\" />");
      sb.append("</head>");
      sb.append("<body>");
      sb.append("<h1>");
      sb.append(e.getName());
      sb.append("</h1>");

      List<DateTime> dates = e.getDates();
      String[] locationNames = e.getLocationNames();

      SimpleDateFormat dayFormat = new SimpleDateFormat("EEE, MMM dd. yyyy");
      SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
      for (DateTime eachDate : dates) {
         sb.append("<h2>");
         sb.append(dayFormat.format(eachDate.toDate()));
         sb.append("</h2>");
         sb.append("<table>");
         sb.append("<thead>");
         sb.append("<tr>");
         for (String locationName : locationNames) {
            sb.append("<th>");
            sb.append(locationName);
            sb.append("</th>");
         }
         sb.append("</tr>");
         sb.append("</thead>");
         sb.append("<tbody>");
         sb.append("<tr>");
         for (String locationName : locationNames) {
            Location location = e.findLocation(locationName);
            DayProgram day = location.findDayProgram(eachDate);
            sb.append("<td>");
            if (day != null) {
               sb.append("<table>");
               for (Session session : day) {
                  if (!session.isOldVersion()) {
                     sb.append("<tr");
                     if (session.isCancelled()) {
                        sb.append(" class=\"cancelled\"");
                     }
                     if (session.isMoved()) {
                        sb.append(" class=\"moved\"");
                     }
                     sb.append(">");
                     sb.append("<td class=\"time\">");
                     sb.append(timeFormat.format(session.getStart().toDate()));
                     sb.append(" - ");
                     sb.append(timeFormat.format(session.getEnd().toDate()));
                     sb.append("</td>");
                     sb.append("<td>");
                     sb.append(session.getShortName() == null ? session.getName() : session.getShortName());
                     sb.append("</td>");
                     sb.append("</tr>");
                  }
               }
               sb.append("</table>");
               sb.append("</td>");
            }
         }
         sb.append("</tr>");
         sb.append("</tbody>");
         sb.append("</table>");
      }
      sb.append("<p class=\"footer\">Generated by <a href=\"http://openair-client.sourceforge.net\">OpenAir</a></p>");
      sb.append("</body>");
      sb.append("</html>");
      return sb.toString();
   }

   public static void main(String[] args) throws Exception {
      if (args.length != 2) {
         System.out.println("Please specify input file and output directory!");
         System.exit(0);
         return;
      }
      File inFile = new File(args[0]);
      String ext = args[0].substring(args[0].lastIndexOf("."), args[0].length());
      Event e = null;
      if (".zip".equals(ext)) {
         e = ModelMarshaller.unmarshallZip(new FileInputStream(inFile));
      } else {
         e = ModelMarshaller.unmarshall(new FileInputStream(inFile));
      }
      ModelHtmlOutput.htmpOutput(e, args[1]);
   }

}
