package edu.education.myapplication;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
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

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    //private static final String ACCESS_POINT_LOCATIONS_DATA_URL = "http://192.168.43.89/locationtracker/index.php/welcome/getAccessPoints";
    private static final String ACCESS_POINT_LOCATIONS_DATA_URL = "https://tltms.tce.edu/tracker/locationtracker/index.php/welcome/getAccessPoints";

    private static final String UPLOAD_LOCATION_INTO_SERVER_DATABASE = "https://tltms.tce.edu/tracker/locationtracker/index.php/welcome/updateLocation";
    //private static final String UPLOAD_LOCATION_INTO_SERVER_DATABASE = "http://192.168.43.89/locationtracker/index.php/welcome/updateLocation";

    private static final String UPLOAD_IMAGE_INTO_SERVER_URL = "https://tltms.tce.edu/tracker/locationtracker/index.php/welcome/uploadImage";
    //private static final String UPLOAD_IMAGE_INTO_SERVER_URL = "http://192.168.43.89/locationtracker/index.php/welcome/uploadImage";

    private static final int TIMEOUT_DURATION = 40000;


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
    private LinearLayout reUploadLocation;
    private LinearLayout reUploadImages;

    private GPSTracker gpsTracker;
    private LastUpdated lastUpdate;
    private InternetDetails internetDetails;
    private DatabaseHandler databaseHandler;

    private RequestQueue requestQueue;
    private StringRequest stringRequest;
    private boolean isUploaded;

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

        reUploadLocation = findViewById(R.id.reUploadLocation);
        reUploadLocation.setVisibility(View.GONE);
        reUploadImages = findViewById(R.id.reUploadImages);
        reUploadImages.setVisibility(View.GONE);
        reUploadLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //function to upload the unloaded files into server
                DatabaseHandler dbHandler = new DatabaseHandler(MainActivity.this);
                localStoredLocations(dbHandler.getLocation());
            }
        });

        reUploadImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseHandler dbHandler = new DatabaseHandler(MainActivity.this);
                localStoredImages(dbHandler.getImage());
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

                int numberOfLocalImages = databaseHandler.getImageCount();
                int numberOfLocalLocation = databaseHandler.getLocationCount();

                if (numberOfLocalLocation > 0) {
                    reUploadLocation.setVisibility(View.VISIBLE);
                } else {
                    reUploadLocation.setVisibility(View.GONE);
                }

                if (numberOfLocalImages > 0) {
                    reUploadImages.setVisibility(View.VISIBLE);
                } else {
                    reUploadImages.setVisibility(View.GONE);
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


            /*FindMe findMe = new FindMe(MainActivity.this);
            findMe.getAccessPointLocations(MainActivity.this);*/

            locationUploaderStatus.setText("fetching");
            locationUploaderStatus.setTextColor(getResources().getColor(R.color.black));

            RequestQueue requestQueue;
            JsonObjectRequest objectRequest;

            databaseHandler = new DatabaseHandler(MainActivity.this);

            System.out.println("Location Count : " + databaseHandler.getAccessPointCount());
            if (databaseHandler.getAccessPointCount() > 0) {
                databaseHandler.deleteAccessPointTable();
            }


            requestQueue = Volley.newRequestQueue(MainActivity.this);
            objectRequest = new JsonObjectRequest(Request.Method.POST, ACCESS_POINT_LOCATIONS_DATA_URL, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        System.out.println(response);
                        JSONArray jsonArray = response.getJSONArray("data");
                        for (int i=0; i<jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            //complete the code
                            double latitude = Double.parseDouble(jsonObject.getString("latitude"));
                            double longitude = Double.parseDouble(jsonObject.getString("longitude"));
                            String locationName = jsonObject.getString("name");
                            double minimumDistance = Double.parseDouble(jsonObject.getString("min"));
                            databaseHandler.uploadAccessPoints(latitude, longitude, locationName, minimumDistance);
                        }
                        System.out.println("Location Counter : " + databaseHandler.getAccessPointCount());
                        if (databaseHandler.getAccessPointCount() > 0) {
                            locationUploaderStatus.setText("Location points Updated successfully. Please Confirm this in admin panel");
                            locationUploaderStatus.setTextColor(getResources().getColor(R.color.darkGreen));
                        } else {
                            locationUploaderStatus.setText("Couldn't Load locations. Please retry or Check local database");
                            locationUploaderStatus.setTextColor(getResources().getColor(R.color.darkred));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        locationUploaderStatus.setText("Couldn't Load locations. Please retry or Check local database");
                        locationUploaderStatus.setTextColor(getResources().getColor(R.color.darkred));
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(), "Access point Error", Toast.LENGTH_LONG).show();
                    locationUploaderStatus.setText("Couldn't Load locations. Please retry or Check local database");
                    locationUploaderStatus.setTextColor(getResources().getColor(R.color.darkred));
                }
            });
            objectRequest.setRetryPolicy(new DefaultRetryPolicy(TIMEOUT_DURATION, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(objectRequest);

            /*databaseHandlerTemp = new DatabaseHandler(MainActivity.this);
            Toast.makeText(getApplicationContext(),String.valueOf(databaseHandler.getAccessPointCount()),Toast.LENGTH_LONG).show();
            System.out.println("Counter : " + databaseHandlerTemp.getAccessPointCount());/

            /*if (databaseHandlerTemp.getAccessPointCount() > 0) {
                locationUploaderStatus.setText("Location points Updated successfully. Please Confirm this in admin panel");
                locationUploaderStatus.setTextColor(getResources().getColor(R.color.darkGreen));
            } else {
                locationUploaderStatus.setText("Couldn't Load locations. Please retry or Check local database");
                locationUploaderStatus.setTextColor(getResources().getColor(R.color.darkred));
            }*/
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

    public class LoadAccessLocation extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            return null;
        }
    }

    //----------------------------------------------------------------------------------------------
    public void localStoredImages(Cursor imagesData) {
        String upTime;
        double latitude;
        double longitude;
        String position;
        String image;

        System.out.println("Called Image Uploader");
        while (imagesData.moveToNext()) {
            System.out.println("Running loop");
            upTime = imagesData.getString(0);
            latitude = imagesData.getDouble(1);
            longitude = imagesData.getDouble(2);
            position = imagesData.getString(3);
            image = imagesData.getString(4);
            uploadFailedImageIntoServer(upTime, latitude, longitude, position, image);
        }
    }

    private void uploadFailedImageIntoServer (final String upTime, final double latitude, final double longitude, final String position, final String image) {
        requestQueue = Volley.newRequestQueue(this);
        stringRequest = new StringRequest(Request.Method.POST, UPLOAD_IMAGE_INTO_SERVER_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                System.out.println("Failed Image Uploading response : " + response);
                if (response.equals("ok")) {
                    //function to delete the image from local database
                    databaseHandler.deleteImageOnUpdate(upTime);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("systemId", getSystemUniqueId());
                params.put("exactTime", upTime);
                params.put("latitude", Double.toString(latitude));
                params.put("longitude", Double.toString(longitude));
                params.put("status", position);
                params.put("image", image);
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(TIMEOUT_DURATION, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(stringRequest);
    }

    public void localStoredLocations(Cursor cursor) {
        String upDateTime;
        Double latitude;
        Double longitude;
        String position;

        while (cursor.moveToNext()) {
            upDateTime = cursor.getString(0);
            latitude = Double.parseDouble(cursor.getString(1));
            longitude = Double.parseDouble(cursor.getString(2));
            position = cursor.getString(3);

            if (uploadFailedLocationIntoServer(upDateTime, latitude, longitude, position)) {
                //Toast.makeText(getApplicationContext(),"Success to Delete",Toast.LENGTH_SHORT).show();
                System.out.println("Success to Delete");
            }

            /*if (uploadFailedLocationIntoServer(upDateTime, latitude, longitude, position)) {
                Toast.makeText(getApplicationContext(),"Deletion",Toast.LENGTH_SHORT).show();
                databaseHandler.deleteLocationOnUpdate(upDateTime);
            }*/
        }
    }

    private boolean uploadFailedLocationIntoServer(final String upDateTime, final double latitude, final double longitude, final String position) {

        isUploaded = false;

        requestQueue = Volley.newRequestQueue(this);
        stringRequest = new StringRequest(Request.Method.POST, UPLOAD_LOCATION_INTO_SERVER_DATABASE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response.equals("ok")) {
                    isUploaded = true;
                    databaseHandler.deleteLocationOnUpdate(upDateTime);
                } else {
                    isUploaded = false;
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                isUploaded = false;
            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("id",getSystemUniqueId());
                params.put("latitude", Double.toString(latitude));
                params.put("longitude", Double.toString(longitude));
                params.put("status",position);
                params.put("dataTimeStamp",upDateTime);
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(TIMEOUT_DURATION, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(stringRequest);

        return false;
    }

}
