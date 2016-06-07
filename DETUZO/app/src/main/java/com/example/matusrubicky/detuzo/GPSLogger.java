package com.example.matusrubicky.detuzo;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import org.joda.time.DateTime;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.joda.time.Seconds;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.ticofab.androidgpxparser.parser.domain.TrackPoint;

public class GPSLogger extends FragmentActivity implements OnMapReadyCallback, GPSResultReceiver.Receiver {

    GoogleMap mMap;
    DecimalFormat f = new DecimalFormat("0.00");
    GPSResultReceiver resultReceiver;
    Intent intent;
    List<TrackPoint> list;

    String timeToSend;

    DateTime time;
    TextView cas;
    TextView vzd;
    double distance = 0d;
    private double lat;
    private double lng;
    private double ele;
    boolean recording = false;
    String bikeType = "road"; // road, mtb
    String hours;
    String minutes;
    String seconds;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapGPS);
        mapFragment.getMapAsync(this);
        list = new ArrayList<>();

        cas = (TextView) findViewById(R.id.cas);
        vzd = (TextView) findViewById(R.id.vzd);
        resultReceiver = new GPSResultReceiver(new Handler());

        resultReceiver.setReceiver(this);

        final FloatingActionButton start = (FloatingActionButton) findViewById(R.id.start);
        start.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!recording) {
                    recording = true;
                    onStartService();
                    time = DateTime.now();
                    start.setIcon(R.drawable.stop);
                } else {
                    onStopService();
                    start.setIcon(R.drawable.play);
                    showDialog();
                }
            }
        });
    }

    public void onStartService() {
        intent = new Intent(this, GPSLoggerService.class);
        intent.putExtra("receiverTag", resultReceiver);
        time = DateTime.now();
        startService(intent);
    }

    public void onStopService(){
        recording = false;
        stopService(intent);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {

        if (Hours.hoursBetween(time, DateTime.now()).getHours() < 10) {
            hours = "0" + Hours.hoursBetween(time, DateTime.now()).getHours();
        } else {
            hours = "" + Hours.hoursBetween(time, DateTime.now()).getHours();
        }
        if (Minutes.minutesBetween(time, DateTime.now()).getMinutes() % 60 < 10) {
            minutes = "0" + Minutes.minutesBetween(time, DateTime.now()).getMinutes() % 60;
        } else {
            minutes = "" + Minutes.minutesBetween(time, DateTime.now()).getMinutes() % 60;
        }
        if (Seconds.secondsBetween(time, DateTime.now()).getSeconds() % 60 < 10) {
            seconds = "0" + Seconds.secondsBetween(time, DateTime.now()).getSeconds() % 60;
        } else {
            seconds = "" + Seconds.secondsBetween(time, DateTime.now()).getSeconds() % 60;
        }

        cas.setText(hours + ":" + minutes + ":" + seconds);

        if(Integer.parseInt(seconds)%3 == 0) {
            lat = resultData.getDouble("lat");
            lng = resultData.getDouble("lng");
            ele = resultData.getDouble("ele");

            list.add(new TrackPoint.Builder().setLatitude(lat).
                    setLongitude(lng).setTime(DateTime.now()).setElevation(ele).build());

            if (list.size() > 1)
                distance += Logic.distance(lat, lng, list.get(list.size() - 2).getLatitude(), list.get(list.size() - 2).getLongitude(), "K");

            vzd.setText(f.format(distance) + " km");

            final LatLngBounds.Builder builder = new LatLngBounds.Builder();

            TrackPoint point = list.get(list.size() - 1);
            LatLng ll = new LatLng(point.getLatitude(), point.getLongitude());

            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(ll).icon(BitmapDescriptorFactory.fromResource(R.drawable.bikemarker)));
            //options.add(ll);
            builder.include(ll);

            //Polyline line = mMap.addPolyline(options);
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
            // Remove listener to prevent position reset on camera move.
            mMap.setOnCameraChangeListener(null);
        }

        timeToSend = String.valueOf(Minutes.minutesBetween(time, DateTime.now()).getMinutes());

        NotificationCompat.Builder notifBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.record)
                        .setContentTitle("Recording ride...")
                        .setContentText(hours + ":" + minutes + ":" + seconds + ", " + f.format(distance) + " km");

        Intent targetIntent = new Intent(this, GPSLogger.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notifBuilder.setContentIntent(contentIntent);
        notifBuilder.addAction(R.drawable.stopmini, "Stop", contentIntent);
        NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nManager.notify(7777, notifBuilder.build());
    }

    public void showDialog(){
        final EditText editTextName = new EditText(this);

        new AlertDialog.Builder(this)
                .setTitle("Zadajte názov")
                .setView(editTextName)
                .setPositiveButton("Uložiť", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        try {
                            String subor = Logic.saveGPX(list, new Date().toString());
                            Intent myIntent = new Intent(GPSLogger.this, ScrollingActivity.class);
                            myIntent.putExtra("cestaKSuboru", subor);
                            myIntent.putExtra("name", String.valueOf(editTextName.getText()));
                            myIntent.putExtra("cas", timeToSend+ " min");
                            myIntent.putExtra("type", bikeType);
                            startActivity(myIntent);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .setNegativeButton("Storno", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Intent myIntent = new Intent(GPSLogger.this, ScrollingActivity.class);
                        startActivity(myIntent);
                    }
                })
                .show();

    }
}