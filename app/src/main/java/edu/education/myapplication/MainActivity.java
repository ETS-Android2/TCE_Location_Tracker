package edu.education.myapplication;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Handler handler;
    private Runnable runnable;
    private Date date;

    private TextView systemUId;
    private TextView currentDateAndTime;
    private TextView latitude;
    private TextView longitude;

    private TextView dateTimeStamp;
    private TextView lastLongitude;
    private TextView lastLatitude;
    private TextView lastPosition;
    private TextView lastMode;
    private ImageView lastImage;

    private TextView download;
    private TextView locationUploaderStatus;

    private LinearLayout openCamera;
    private LinearLayout adminAdmin;
    private LinearLayout locationLoaderLayout;
    private LinearLayout reloadLocationLoader;

    private GPSTracker gpsTracker;
    private LastUpdated lastUpdate;
    private InternetDetails internetDetails;
    private DatabaseHandler databaseHandler;
    private DatabaseHandler databaseHandlerTemp;

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(runnable);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onStart() {
        super.onStart();
        loadAccessPointLoader();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        systemUId = findViewById(R.id.uniqueId);
        currentDateAndTime = findViewById(R.id.currentDateTIme);
        latitude = findViewById(R.id.latitude);
        longitude = findViewById(R.id.longitude);

        dateTimeStamp = findViewById(R.id.dateTimeStamp);
        lastLongitude = findViewById(R.id.lastLongitude);
        lastLatitude = findViewById(R.id.lastLatitude);
        lastPosition = findViewById(R.id.lastPosition);
        lastMode = findViewById(R.id.lastMode);
        lastImage = findViewById(R.id.lastUploadedImage);

        download = findViewById(R.id.downloadSpeed);
        locationUploaderStatus = findViewById(R.id.locationStatus);

        locationLoaderLayout = findViewById(R.id.locationLoaderLayout);
        reloadLocationLoader = findViewById(R.id.reloadLocationLoader);

        openCamera = findViewById(R.id.openCamera);
        openCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,CameraActivity.class);
                ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(getApplicationContext(),R.anim.fade_out,R.anim.fade_in);
                startActivity(intent,activityOptions.toBundle());
            }
        });

        adminAdmin = findViewById(R.id.admin);
        adminAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, adminAuth.class);
                ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(getApplicationContext(),R.anim.fade_out,R.anim.fade_in);
                startActivity(intent,activityOptions.toBundle());
            }
        });

        reloadLocationLoader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadAccessPointLoader();
            }
        });

        gpsTracker = new GPSTracker(this);
        lastUpdate = new LastUpdated(this);
        internetDetails = new InternetDetails(this);
        databaseHandler = new DatabaseHandler(this);

        systemUId.setText(getSystemUniqueId());

        runTimer();

    }

    @Override
    protected void onResume() {
        super.onResume();

        runTimer();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            RestartServiceBroadcasrReciever.scheduleJob(getApplicationContext());
        } else {
            ProcessMainClass processMainClass = new ProcessMainClass();
            processMainClass.launchService(getApplicationContext());
        }

    }

    private String getCurrentDateAndTime() {
        date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(date);
    }

    public String getSystemUniqueId() {
        String systemUniqueId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        return systemUniqueId;
    }

    private void runTimer() {
        handler = new Handler();
        runnable = new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void run() {

                Location location = gpsTracker.getLocation();

                if (internetDetails.getConnectionDetails()) {
                    download.setText("Online");
                    download.setTextColor(getResources().getColor(R.color.darkGreen));
                } else {
                    download.setText("Offline");
                    download.setTextColor(getResources().getColor(R.color.darkred));
                }


                currentDateAndTime.setText(getCurrentDateAndTime());
                latitude.setText(String.valueOf(gpsTracker.getLatitude()));
                longitude.setText(String.valueOf(gpsTracker.getLongitude()));

                if (lastUpdate.getSharedPreference()) {
                    dateTimeStamp.setText(lastUpdate.getDateTimeStamp());
                    lastLatitude.setText(lastUpdate.getLastLocationLatitude());
                    lastLongitude.setText(lastUpdate.getLastLocationLongitude());
                    lastPosition.setText(lastUpdate.getLastLocationStatus());
                    lastMode.setText(lastUpdate.getMode());
                    if (lastUpdate.getMode().equals("Server")) {
                        lastMode.setTextColor(getResources().getColor(R.color.darkGreen));
                    } else {
                        lastMode.setTextColor(getResources().getColor(R.color.darkred));
                    }
                    lastImage.setImageBitmap(lastUpdate.getImage());
                }

                handler.postDelayed(runnable,1000);
            }
        };

        handler.postDelayed(runnable,1000);

    }

    private void loadAccessPointLoader() {


        /*ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());

        double downloadSpeed = networkCapabilities.getLinkDownstreamBandwidthKbps();
        double uploadSpeed = networkCapabilities.getLinkUpstreamBandwidthKbps();

        System.out.println(String.valueOf(downloadSpeed) + " Upload: ");*/

        if ((isMobileDataEnabled() || isWiFiEnabled()) && internetDetails.getConnectionDetails()) {
            FindMe findMe = new FindMe(this);
            findMe.getAccessPointLocations(this);
            databaseHandlerTemp = new DatabaseHandler(this);
            if (databaseHandler.getAccessPointCount() > 0) {
                locationUploaderStatus.setText("Location points Updated successfully. Please Confirm this in admin panel");
                locationUploaderStatus.setTextColor(getResources().getColor(R.color.darkGreen));
            } else {
                locationUploaderStatus.setText("Couldn't Load locations. Please retry or Check local database");
                locationUploaderStatus.setTextColor(getResources().getColor(R.color.darkred));
            }
        } else {
            locationUploaderStatus.setText("No Internet. You are offline");
            locationUploaderStatus.setTextColor(getResources().getColor(R.color.darkred));
        }
    }

    /*----------------------------------------------------------------------------------------------
                        Check if the Mobile data is Switched ON or OFF
    ----------------------------------------------------------------------------------------------*/
    public boolean isMobileDataEnabled() {

        boolean isEnabled = false;

        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            Class mClass = Class.forName(connectivityManager.getClass().getName());
            Method method = mClass.getDeclaredMethod("getMobileDataEnabled");
            method.setAccessible(true);
            isEnabled = (boolean) method.invoke(connectivityManager);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return isEnabled;

    }
    //----------------------------------------------------------------------------------------------


    /*----------------------------------------------------------------------------------------------
                            Check if the WIFI is Switched ON or OFF
    ----------------------------------------------------------------------------------------------*/
    public boolean isWiFiEnabled() {

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (wifiManager.isWifiEnabled()) {
            return true;
        } else {
            return false;
        }

    }
    //----------------------------------------------------------------------------------------------

}
