package edu.education.myapplication;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

public class GPSTracker extends Service implements LocationListener {

    private Context context;
    private boolean isGPSEnabled = false;
    boolean canGetLocation = false;

    Location location;
    double latitude;
    double longitude;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1;
    private static final long MIN_TIME_BTW_UPDATES = 1000 * 60 * 1;

    protected LocationManager locationManager;

    public GPSTracker(Context context) {
        this.context = context;
        getLocation();
    }

    public Location getLocation() {
        try {
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if (!isGPSEnabled) {
                Toast.makeText(context.getApplicationContext(), "No GPS ", Toast.LENGTH_LONG).show();
            } else {
                this.canGetLocation = true;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(context,"Permission is still denied",Toast.LENGTH_LONG).show();
                    } else {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BTW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            } else {
                                Toast.makeText(context,"Error in location",Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                } else {
                    Toast.makeText(context,"Lower API",Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return location;
    }

    public void stopUsingGPS() {
        if (locationManager != null) {
            locationManager.removeUpdates(GPSTracker.this);
        }
    }

    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }
        return latitude;
    }

    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }
        return longitude;
    }

    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {

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
}




/*************************************************************************************************************************************************************************
 *************************************************************************************************************************************************************************
                COPYRIGHTS RESERVED @ DEPARTMENT OF COMPUTER SCIENCE AND ENGINEERING, Thiagarjar College of Engineering- Madurai
* ************************************************************************************************************************************************************************
 *************************************************************************************************************************************************************************
                              @@@@@@@@@@@@@@@@@@@@@@@@         @@@@@@@@@@@@@@@@@@@@@@@@@@          @@@@@@@@@@@@@@@@@@@@@@
                              @@@@@@@@@@@@@@@@@@@@@@@@         @@@@@@@@@@@@@@@@@@@@@@@@@@          @@@@@@@@@@@@@@@@@@@@@@
                                       @@@@                    @@@@@@                              @@@@
                                       @@@@                    @@@@@@                              @@@@
                                       @@@@                    @@@@@@                              @@@@@@@@@@@@@@@@@@@@@@
                                       @@@@                    @@@@@@                              @@@@@@@@@@@@@@@@@@@@@@
                                       @@@@                    @@@@@@                              @@@@
                                       @@@@                    @@@@@@@@@@@@@@@@@@@@@@@@@           @@@@@@@@@@@@@@@@@@@@@@
                                       @@@@                    @@@@@@@@@@@@@@@@@@@@@@@@@           @@@@@@@@@@@@@@@@@@@@@@

 * ***********************************************************************************************************************************************************************
 *************************************************************************************************************************************************************************
 * ************************************************************************************************************************************************************************
* */