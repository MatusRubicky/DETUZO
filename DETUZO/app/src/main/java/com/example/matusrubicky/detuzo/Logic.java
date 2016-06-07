package com.example.matusrubicky.detuzo;

import android.util.Log;

import org.alternativevision.gpx.beans.GPX;
import org.alternativevision.gpx.beans.Waypoint;
import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import io.ticofab.androidgpxparser.parser.GPXParser;
import io.ticofab.androidgpxparser.parser.domain.Gpx;
import io.ticofab.androidgpxparser.parser.domain.TrackPoint;


/**
 * Created by matusrubicky on 27.5.16.
 */
public class Logic {

    static DecimalFormat f = new DecimalFormat("0.00");
    static GPXParser parser = new GPXParser();
    static String path = "/sdcard/Android/data/" + DETUZO.route.PACKAGE + "/";

    public static String parseNameFromGPX(String url) {
        try {
            File inputFile = new File(url);
            DocumentBuilderFactory dbFactory
                    = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            NodeList nList = doc.getElementsByTagName("name");
            return nList.item(0).getTextContent();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return DateTime.now().toString();
    }

    public static List<TrackPoint> parseFromXML(String name) throws ParserConfigurationException, IOException, SAXException {
        List<TrackPoint> list = new ArrayList<>();
        Log.d("LLLL", name);

            File inputFile = new File(name);
            DocumentBuilderFactory dbFactory
                    = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);

            NodeList nodes = doc.getElementsByTagName("wpt");
            for (int i = 0; i < nodes.getLength(); i++) {
                Element element = (Element) nodes.item(i);

                TrackPoint point = new TrackPoint.Builder().setLatitude(Double.valueOf(element.getAttribute("lat"))).
                        setLongitude(Double.valueOf(element.getAttribute("lon"))).build();

                list.add(point);
            }
        return list;
    }

    public static String calculateTime(String subor) {
        GPXParser parser = new GPXParser();

        Gpx parsedGpx = null;
        try {
            InputStream in = new FileInputStream(subor);
            parsedGpx = parser.parse(in);
        } catch (IOException | XmlPullParserException e) {
            return "--";
        }
        if (parsedGpx == null) {
            return "--";
        } else {
            List<TrackPoint> list = parsedGpx.getTracks().get(0).getTrackSegments().get(0).getTrackPoints();
            Minutes minutes = Minutes.minutesBetween(list.get(0).getTime(), list.get(list.size() - 1).getTime());

            return minutes.getMinutes() + " min";
        }
    }

    public static String calculateElevation(String subor) throws IOException, XmlPullParserException {
        List<TrackPoint> list = new ArrayList<>();
        InputStream in = null;
        double elevation = 0;
        if (subor.endsWith(".gpx")) {
            try {
                in = new FileInputStream(subor);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Gpx parsedGpx = null;
            if (in != null) {
                parsedGpx = parser.parse(in);
            }

            if (parsedGpx != null) {
                list = parsedGpx.getTracks().get(0).getTrackSegments().get(0).getTrackPoints();
            }
        /*else {
            try {
                list = parseFromXML(subor);
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
            for (int z = 1; z < list.size(); z++) {
                TrackPoint point2 = list.get(z);
                TrackPoint point1 = list.get(z - 1);

                if ((point2.getElevation() - point1.getElevation()) > 0)
                    elevation += point2.getElevation() - point1.getElevation();
            }
        }
            return String.valueOf(f.format(elevation)) + " m";
    }

    public static String calculateDistance(String subor) throws IOException, XmlPullParserException {
        List<TrackPoint> list = new ArrayList<>();
        InputStream in = null;
        double distance = 0;
        Log.d("SUBOR", subor);
        if (subor.endsWith(".gpx")) {
            try {
                in = new FileInputStream(subor);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Gpx parsedGpx = null;
            if (in != null) {
                parsedGpx = parser.parse(in);
            }

            if (parsedGpx != null) {
                list = parsedGpx.getTracks().get(0).getTrackSegments().get(0).getTrackPoints();
            }
        } else {
            try {
                list = Logic.parseFromXML(subor);
            } catch (ParserConfigurationException e) {
                return String.valueOf(Double.NaN);
            } catch (SAXException e) {
                return String.valueOf(Double.NaN);
            }
        }
        for (int i = 1; i < list.size(); i++) {
            TrackPoint pointFirst = list.get(i);
            TrackPoint pointSecond = list.get(i - 1);

            distance += distance(pointFirst.getLatitude(), pointFirst.getLongitude(),
                    pointSecond.getLatitude(), pointSecond.getLongitude(), "K");
        }

        return String.valueOf(f.format(distance)) + " km";
    }

    public static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if (unit.equals("K")) {
            dist = dist * 1.609344;
        } else if (unit.equals("N")) {
            dist = dist * 0.8684;
        }
        if (Double.isNaN(dist))
            return 0;
        return dist;
    }

    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private static double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    public static HashSet<Waypoint> transformTrackPoints(List<TrackPoint> list) {
        HashSet<Waypoint> newSet = new HashSet<>();

        for (int i = 0; i < list.size(); i++) {
            TrackPoint point = list.get(i);
            Waypoint newPoint = new Waypoint();
            newPoint.setLatitude(point.getLatitude());
            newPoint.setLongitude(point.getLongitude());
            newPoint.setElevation(point.getElevation());
            newSet.add(newPoint);
        }
        return newSet;
    }

    public static String saveGPX(List<TrackPoint> list, String name) throws FileNotFoundException {
        GPX gpx = new GPX();
        gpx.setWaypoints(transformTrackPoints(list));
        org.alternativevision.gpx.GPXParser parser;
        parser = new org.alternativevision.gpx.GPXParser();

        String nazov = path + new Date().toString() + ".xml";
        FileOutputStream out = new FileOutputStream(nazov);

        try {
            parser.writeGPX(gpx, out);
            // parser.addExtensionParser(new DummyExtensionParser().writeGPXExtensionData("name"));
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return nazov;
    }

    public static void moveFile(File file, File dir) throws IOException {
        File newFile = new File(dir, file.getName());
        FileChannel outputChannel = null;
        FileChannel inputChannel = null;
        try {
            outputChannel = new FileOutputStream(newFile).getChannel();
            inputChannel = new FileInputStream(file).getChannel();
            inputChannel.transferTo(0, inputChannel.size(), outputChannel);
            inputChannel.close();
        } finally {
            if (inputChannel != null) inputChannel.close();
            if (outputChannel != null) outputChannel.close();
        }

    }
}

