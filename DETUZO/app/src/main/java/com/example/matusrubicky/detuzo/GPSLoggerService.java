package com.example.matusrubicky.detuzo;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.os.ResultReceiver;

public class GPSLoggerService extends IntentService implements LocationListener {

    private static final long TIME = 1000; //1 seconds
    private LocationManager locationManager;
    Location loc;
    Intent intent;
    ResultReceiver rec;
    Bundle b;

    public GPSLoggerService() {
        super(GPSLoggerService.class.getSimpleName());
    }

    public GPSLoggerService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        rec = intent.getParcelableExtra("receiverTag");
        b = new Bundle();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, TIME, 0, this);
    }

    @Override
    public void onLocationChanged(Location loc) {
        if (loc != null) {
            double lat = loc.getLatitude();
            double lng = loc.getLongitude();
            b.putDouble("lat", lat);
            b.putDouble("lng", lng);

            double time = loc.getTime();
            double ele = loc.getAltitude();
            b.putDouble("time", time);
            b.putDouble("ele", ele);
            rec.send(0, b);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
