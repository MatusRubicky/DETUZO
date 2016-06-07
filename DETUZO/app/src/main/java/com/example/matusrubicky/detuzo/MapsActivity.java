package com.example.matusrubicky.detuzo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import io.ticofab.androidgpxparser.parser.GPXParser;
import io.ticofab.androidgpxparser.parser.domain.Gpx;
import io.ticofab.androidgpxparser.parser.domain.TrackPoint;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private File subor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Bundle extras = getIntent().getExtras();
        subor = new File(extras.getString("route"));
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        List<TrackPoint> list = null;
        if (subor.getAbsolutePath().endsWith(".gpx")){
            list = makeRouteFromGpx(subor.getAbsolutePath());
        }else{
            list = makeRouteFromXml(subor.getAbsolutePath());
        }

        final LatLngBounds.Builder builder = new LatLngBounds.Builder();
        PolylineOptions options = new PolylineOptions().width(5).color(Color.parseColor("#FF7711")).geodesic(true);
        for (int z = 0; z < list.size(); z++) {
            TrackPoint point = list.get(z);
            LatLng ll = new LatLng(point.getLatitude(), point.getLongitude());
            options.add(ll);
            builder.include(ll);
        }
        if (list.size() >1) {
            mMap.addPolyline(options);

            mMap.addMarker(new MarkerOptions().
                    position(new LatLng(list.get(0).getLatitude(), list.get(0).getLongitude())).icon(BitmapDescriptorFactory.fromResource(R.drawable.start)));
            mMap.addMarker(new MarkerOptions().
                    position(new LatLng(list.get(list.size() - 1).getLatitude(), list.get(list.size() - 1).getLongitude())).icon(BitmapDescriptorFactory.fromResource(R.drawable.finish)));

            mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {

                @Override
                public void onCameraChange(CameraPosition arg0) {
                    // Move camera.
                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 50));
                    // Remove listener to prevent position reset on camera move.
                    mMap.setOnCameraChangeListener(null);
                }
            });
        }
    }

    public static List<TrackPoint> makeRouteFromGpx(String subor) {
        GPXParser parser = new GPXParser();
        Gpx parsedGpx = null;
        List<TrackPoint> list = null;
        try {
            InputStream in = new FileInputStream(subor);
            parsedGpx = parser.parse(in);
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }
        if (parsedGpx == null) {
            // error parsing track
        } else {
            list = parsedGpx.getTracks().get(0).getTrackSegments().get(0).getTrackPoints();
        }
        return list;
    }

    public static List<TrackPoint> makeRouteFromXml(String subor) {
        try {
            return Logic.parseFromXML(subor);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
        return null;
    }
}