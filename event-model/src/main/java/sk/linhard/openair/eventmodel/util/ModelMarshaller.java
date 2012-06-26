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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlSerializer;

import sk.linhard.openair.eventmodel.DayProgram;
import sk.linhard.openair.eventmodel.Event;
import sk.linhard.openair.eventmodel.Session;
import sk.linhard.openair.eventmodel.SessionMetadata;
import sk.linhard.openair.eventmodel.Location;
import sk.linhard.openair.eventmodel.LocationMetadata;
import android.util.Xml;

/**
 * Lightweight XML Serializer/Deserializer for Event Program Model. Using only
 * XML tools available in Android API 7.
 * 
 * @author Michal Linhard <michal@linhard.sk>
 */
public class ModelMarshaller {
    private static final String NS = "";

    public static void marshall(Event e, OutputStream os) {
        try {
            int currentMetadataId = 1;
            HashMap<Integer, Location> locationMetadata = new HashMap<Integer, Location>();
            HashMap<Integer, Session> sessionMetadata = new HashMap<Integer, Session>();
            HashMap<Session, Integer> changeMapping = new HashMap<Session, Integer>();

            XmlSerializer ser = Xml.newSerializer();
            ser.setOutput(os, "UTF-8");
            //ser.setProperty("http://xmlpull.org/v1/doc/properties.html#serializer-indentation", "\t");
            ser.startDocument("UTF-8", true);
            ser.startTag(NS, "event");
            attribute(ser, "xmlns", "http://michal.linhard.sk/openair/event");
            attribute(ser, "uri", e.getUri());
            attribute(ser, "name", e.getName());
            attribute(ser, "shortName", e.getShortName());
            attribute(ser, "version", e.getVersion());
            attribute(ser, "versionTime", Util.formatDateTime(e.getVersionTime()));

            ser.startTag(NS, "program");
            for (Location eachLocation : e) {
                ser.startTag(NS, "location");
                attribute(ser, "name", eachLocation.getName());
                attribute(ser, "shortName", eachLocation.getShortName());
                if (eachLocation.getMetadata() != null) {
                    attribute(ser, "id", "st" + currentMetadataId);
                    locationMetadata.put(currentMetadataId, eachLocation);
                    currentMetadataId++;
                }
                for (DayProgram eachDay : eachLocation) {
                    ser.startTag(NS, "day");
                    attribute(ser, "date", Util.formatDate(eachDay.getDayStart()));
                    for (Session eachSession : eachDay) {
                        ser.startTag(NS, "session");
                        attribute(ser, "start", Util.formatDateTime(eachSession.getStart()));
                        attribute(ser, "duration", Util.formatDuration(eachSession.getDuration()));
                        attribute(ser, "name", eachSession.getName());
                        attribute(ser, "shortName", eachSession.getShortName());
                        if (eachSession.isCancelled()) {
                            attribute(ser, "cancelled", Boolean.TRUE);
                        }
                        Integer sessionID = changeMapping.get(eachSession);

                        if (sessionID == null && (eachSession.getMetadata() != null || eachSession.isMoved())) {
                            sessionID = currentMetadataId;
                            currentMetadataId++;
                        }
                        if (sessionID != null) {
                            attribute(ser, "id", "sh" + sessionID);
                            if (eachSession.getMetadata() != null) {
                                sessionMetadata.put(sessionID, eachSession);
                            }
                            if (eachSession.isMoved()) {
                                if (eachSession.isOldVersion()) {
                                    changeMapping.put(eachSession.getNewVersion(), sessionID);
                                    attribute(ser, "oldVersion", true);
                                } else {
                                    changeMapping.put(eachSession.getOldVersion(), sessionID);
                                }
                            }
                        }
                        ser.endTag(NS, "session");
                    }
                    ser.endTag(NS, "day");
                }

                ser.endTag(NS, "location");
            }
            ser.endTag(NS, "program");

            ser.startTag(NS, "metadata");
            if (e.getMetadata() != null) {
                ser.startTag(NS, "event");
                attribute(ser, "url", e.getUrl());
                attribute(ser, "description", e.getDescription());
                ser.endTag(NS, "event");
            }
            for (Entry<Integer, Location> locationEntry : locationMetadata.entrySet()) {
                ser.startTag(NS, "location");
                attribute(ser, "id", "st" + locationEntry.getKey());
                attribute(ser, "url", locationEntry.getValue().getUrl());
                attribute(ser, "description", locationEntry.getValue().getDescription());
                ser.endTag(NS, "location");
            }
            for (Entry<Integer, Session> sessionEntry : sessionMetadata.entrySet()) {
                ser.startTag(NS, "session");
                attribute(ser, "id", "sh" + sessionEntry.getKey());
                attribute(ser, "url", sessionEntry.getValue().getUrl());
                attribute(ser, "description", sessionEntry.getValue().getDescription());
                ser.endTag(NS, "session");
            }

            ser.endTag(NS, "metadata");

            ser.endTag(NS, "event");
            ser.endDocument();
            ser.flush();
        } catch (IllegalArgumentException e1) {
            throw new RuntimeException(e1);
        } catch (IllegalStateException e1) {
            throw new RuntimeException(e1);
        } catch (IOException e1) {
            throw new RuntimeException(e1);
        }
    }

    private static void attribute(XmlSerializer s, String attr, Object value) throws IllegalArgumentException, IllegalStateException, IOException {
        if (value == null)
            return;
        s.attribute(NS, attr, value.toString());
    }

    public static Event unmarshall(InputStream is) {
        try {
            // TODO: add schema checking or add simple validation
            // do not throw NPE when some required thing is missing
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document dom = builder.parse(is);
            HashMap<String, Object> metadata = null;
            HashMap<String, Session> sessionById = new HashMap<String, Session>();
            Event event = new Event();
            NodeList metadataNodes = dom.getElementsByTagName("metadata");
            if (metadataNodes.getLength() != 0) {
                metadata = new HashMap<String, Object>();
                NodeList metadataNodeKids = metadataNodes.item(0).getChildNodes();
                for (int i = 0; i < metadataNodeKids.getLength(); i++) {
                    Node metadataNodeKid = metadataNodeKids.item(i);
                    if (metadataNodeKid.getNodeType() != Node.ELEMENT_NODE) {
                        continue;
                    }
                    if (metadataNodeKid.getNodeName().equals("event")) {
                        event.setUrl(metadataNodeKid.getAttributes().getNamedItem("url").getTextContent());
                        event.setDescription(metadataNodeKid.getAttributes().getNamedItem("description").getTextContent());
                    }
                    if (metadataNodeKid.getNodeName().equals("location")) {
                        LocationMetadata locationMetadata = new LocationMetadata();
                        locationMetadata.setUrl(metadataNodeKid.getAttributes().getNamedItem("url").getTextContent());
                        locationMetadata.setDescription(metadataNodeKid.getAttributes().getNamedItem("description").getTextContent());
                        metadata.put(metadataNodeKid.getAttributes().getNamedItem("id").getTextContent(), locationMetadata);
                    }
                    if (metadataNodeKid.getNodeName().equals("session")) {
                        SessionMetadata sessionMetadata = new SessionMetadata();
                        sessionMetadata.setUrl(metadataNodeKid.getAttributes().getNamedItem("url").getTextContent());
                        sessionMetadata.setDescription(metadataNodeKid.getAttributes().getNamedItem("description").getTextContent());
                        metadata.put(metadataNodeKid.getAttributes().getNamedItem("id").getTextContent(), sessionMetadata);
                    }
                }
            }
            Node eventNode = dom.getDocumentElement();
            event.setName(eventNode.getAttributes().getNamedItem("name").getTextContent());
            if (eventNode.getAttributes().getNamedItem("shortName") != null) {
                event.setShortName(eventNode.getAttributes().getNamedItem("shortName").getTextContent());
            }
            if (eventNode.getAttributes().getNamedItem("uri") != null) {
                event.setUri(eventNode.getAttributes().getNamedItem("uri").getTextContent());
            }
            if (eventNode.getAttributes().getNamedItem("version") != null) {
                event.setVersion(new Long(eventNode.getAttributes().getNamedItem("version").getTextContent()));
            }
            if (eventNode.getAttributes().getNamedItem("versionTime") != null) {
                event.setVersionTime(Util.dateTime(eventNode.getAttributes().getNamedItem("versionTime").getTextContent()));
            }

            NodeList programKids = dom.getElementsByTagName("program").item(0).getChildNodes();
            for (int i = 0; i < programKids.getLength(); i++) {
                Node locationNode = programKids.item(i);
                if (locationNode.getNodeType() != Node.ELEMENT_NODE && !locationNode.getNodeName().equals("location")) {
                    continue;
                }
                Location location = event.addLocation(locationNode.getAttributes().getNamedItem("name").getTextContent());
                if (locationNode.getAttributes().getNamedItem("id") != null) {
                    LocationMetadata locationMetadata = (LocationMetadata) metadata.get(locationNode.getAttributes().getNamedItem("id").getTextContent());
                    location.setMetadata(locationMetadata);
                }
                if (locationNode.getAttributes().getNamedItem("shortName") != null) {
                    location.setShortName(locationNode.getAttributes().getNamedItem("shortName").getTextContent());
                }
                NodeList locationKids = locationNode.getChildNodes();
                for (int j = 0; j < locationKids.getLength(); j++) {
                    Node dayNode = locationKids.item(j);
                    if (dayNode.getNodeType() != Node.ELEMENT_NODE && !dayNode.getNodeName().equals("day")) {
                        continue;
                    }
                    DayProgram dayProgram = location.addDay(Util.date(dayNode.getAttributes().getNamedItem("date").getTextContent()));
                    NodeList dayKids = dayNode.getChildNodes();
                    for (int k = 0; k < dayKids.getLength(); k++) {
                        Node sessionNode = dayKids.item(k);
                        if (sessionNode.getNodeType() != Node.ELEMENT_NODE && !sessionNode.getNodeName().equals("session")) {
                            continue;
                        }
                        Session session = dayProgram.addSession(sessionNode.getAttributes().getNamedItem("name").getTextContent(),
                                Util.dateTime(sessionNode.getAttributes().getNamedItem("start").getTextContent()),
                                Util.duration(sessionNode.getAttributes().getNamedItem("duration").getTextContent()));
                        if (sessionNode.getAttributes().getNamedItem("shortName") != null) {
                            session.setShortName(sessionNode.getAttributes().getNamedItem("shortName").getTextContent());
                        }
                        if (sessionNode.getAttributes().getNamedItem("cancelled") != null) {
                            session.setCancelled(new Boolean(sessionNode.getAttributes().getNamedItem("cancelled").getTextContent()));
                        }
                        if (sessionNode.getAttributes().getNamedItem("id") != null) {
                            String sessionId = sessionNode.getAttributes().getNamedItem("id").getTextContent();
                            SessionMetadata sessionMetadata = (SessionMetadata) metadata.get(sessionId);
                            if (sessionMetadata != null) {
                                session.setMetadata(sessionMetadata);
                            }
                            Session theOtherSession = sessionById.get(sessionId);
                            if (theOtherSession != null) {
                                if (sessionNode.getAttributes().getNamedItem("oldVersion") == null
                                        || new Boolean(sessionNode.getAttributes().getNamedItem("oldVersion").getTextContent())) {
                                    session.setNewVersion(theOtherSession);
                                    theOtherSession.setOldVersion(session);
                                } else {
                                    session.setOldVersion(theOtherSession);
                                    theOtherSession.setNewVersion(session);
                                }
                            } else {
                                // we don't know our other session yet
                                sessionById.put(sessionId, session);
                            }
                        }
                    }
                }
            }

            return event;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (DOMException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static void marshallZip(Event e, OutputStream os) {
        try {
            ZipOutputStream zos = new ZipOutputStream(os);
            ZipEntry zentry = new ZipEntry("event.xml");
            zos.putNextEntry(zentry);
            marshall(e, zos);
            zos.closeEntry();
            zos.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Event unmarshallZip(InputStream is) {
        try {
            ZipInputStream zis = new ZipInputStream(is);
            ZipEntry zentry = zis.getNextEntry();
            while (!zentry.getName().equals("event.xml")) {
                zentry = zis.getNextEntry();
                zis.closeEntry();
            }
            Event e = unmarshall(zis);
            zis.close();
            return e;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
