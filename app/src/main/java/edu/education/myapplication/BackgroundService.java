package edu.education.myapplication;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class BackgroundService extends Service {

    //private static final String UPLOAD_LOCATION_INTO_SERVER_DATABASE = "https://tltms.tce.edu/tracker/locationtracker/index.php/welcome/updateLocation";
    private static final String UPLOAD_LOCATION_INTO_SERVER_DATABASE = "http://192.168.43.89/locationtracker/index.php/welcome/updateLocation";

    //private static final String UPLOAD_IMAGE_INTO_SERVER_URL = "https://tltms.tce.edu/tracker/locationtracker/index.php/welcome/uploadImage";
    private static final String UPLOAD_IMAGE_INTO_SERVER_URL = "http://192.168.43.89/locationtracker/index.php/welcome/uploadImage";

    protected static final int NOTIFICATION_ID = 1337;

    protected static int DELAY_RUNNABLE_TIMER = 30000;

    private static String SYSTEM_UNIQUE_IDENTIFICATION_ID;

    private Date date;

    private static Service currentService;

    private static boolean isUploaded = false;

    private FindMe findMe;

    private String lastUploadedTime = "";


    /*----------------------------------------------------------------------------------------------
                            GEO-LOCATION OF BLOCKS IN CAMPUS
    ----------------------------------------------------------------------------------------------*/
    private double[] CSE_BLOCK = new double[] {9.88282, 78.08374};
    private double[] FOOD_COURT = new double[] {9.88338, 78.08324};
    private double[] MAIN_BLOCK = new double[] {9.88282, 78.08254};
    //----------------------------------------------------------------------------------------------

    /*----------------------------------------------------------------------------------------------
                    minimum distance between User and Block - 30meters
    ----------------------------------------------------------------------------------------------*/
    private static final int MIN_USER_BLOCK_LOCATIOn_DISTANCE = 30;
    //----------------------------------------------------------------------------------------------

    //----------------------------- Local database and Update location -----------------------------
    private DatabaseHandler databaseHandler;

    public BackgroundService() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            restartForeground();
        }

        currentService = this;

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        Toast.makeText(getApplicationContext(),"Session restarting",Toast.LENGTH_LONG).show();

        if (intent == null) {
            ProcessMainClass processMainClass = new ProcessMainClass();
            processMainClass.launchService(this);
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            restartForeground();
        }

        startLocationService();

        return START_STICKY;

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void restartForeground() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Toast.makeText(getApplicationContext(),"Restarting Foreground Setvice",Toast.LENGTH_LONG).show();

            try {
                BackgroundNotification notification = new BackgroundNotification();
                startForeground(NOTIFICATION_ID, notification.sendNotification(this,"Service Notification","App is running in background",R.drawable.ic_gps_location));
                Toast.makeText(getApplicationContext(),"Restarted Successfully",Toast.LENGTH_LONG).show();
                startLocationService();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(),"Error in Notification",Toast.LENGTH_LONG).show();
            }

        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Intent broadcastIntent = new Intent(Globals.RESTART_INTENT);
        sendBroadcast(broadcastIntent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        Intent broadcastIntent = new Intent(Globals.RESTART_INTENT);
        sendBroadcast(broadcastIntent);
    }

    private static Handler handler;
    private static Runnable runnable;

    private GPSTracker gpsTracker;

    private static RequestQueue requestQueue;
    private static StringRequest stringRequest;
    private static JSONObject jsonObject;

    public void startLocationService() {
        Toast.makeText(getApplicationContext(),"Location Service started",Toast.LENGTH_LONG).show();

        SYSTEM_UNIQUE_IDENTIFICATION_ID = getSystemUniqueId();

        if (handler != null && runnable != null) {
              handler.removeCallbacks(runnable);
        }

        initializeLocationService();

        handler.postDelayed(runnable, DELAY_RUNNABLE_TIMER);
    }


    //---------------------------- GET LOCATION AND OTHER OPERATIONS -------------------------------
    public void initializeLocationService() {

        Toast.makeText(getApplicationContext(),"Initialising Location Service",Toast.LENGTH_LONG).show();

        gpsTracker = new GPSTracker(getApplicationContext());
        databaseHandler = new DatabaseHandler(this);
        findMe = new FindMe(this);
        date = new Date();

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                //Toast.makeText(getApplicationContext(),"Running Service",Toast.LENGTH_LONG).show();

                //gpsTracker = new GPSTracker(getApplicationContext());

                int currentTimeInHours = getCurrentTimeInHours();

                if (currentTimeInHours >= 7 && currentTimeInHours < 8) {
                    //change the runner timer
                    //upload the un-uploaded content
                    DELAY_RUNNABLE_TIMER = 60 * 10 * 1000;  //-- Runs every 30 Minutes
                    Toast.makeText(getApplicationContext(),String.valueOf(databaseHandler.getLocationCount()),Toast.LENGTH_SHORT).show();
                    if (databaseHandler.getLocationCount() > 0) {
                        //---------------- send unloaded location into server ----------------------
                        uploadFailedLocations(databaseHandler.getLocation());
                    }

                    //upload the un-uploaded images
                    if (databaseHandler.getImageCount() > 0) {
                        uploadFailedImage(databaseHandler.getImage());
                    }
                }
                //------------------- Executes when the time is >4pm and <9am ----------------------
                else {

                    DELAY_RUNNABLE_TIMER = 30 * 1000;       //-- Runs Every 20 Seconds

                    if (gpsTracker.canGetLocation) {

                        Location location = gpsTracker.getLocation();

                        if (location != null) {

                            double latitude = gpsTracker.getLatitude();
                            double longitude = gpsTracker.getLongitude();
                            String status = "";
                            String dateTimeStamp = "";

                            status = findMe.getLocationStatus(latitude, longitude);

                            dateTimeStamp = getCurrentTime();

                            if (!isMobileDataEnabled() && !isWiFiEnabled()) {
                                updateLocalDatabase(latitude, longitude, status, dateTimeStamp);
                                Toast.makeText(getApplicationContext(),"Mobile Data Swirched off",Toast.LENGTH_LONG).show();
                            } else {
                                //Code to upload data into Server
                                updateServerDatabase(latitude, longitude, status, dateTimeStamp);
                                //updateLocalDatabase(latitude, longitude, status, dateTimeStamp);
                            }

                        } else {
                            //------------------- SEND ERROR MESSAGE TO SERVER -------------------------
                            Toast.makeText(getApplicationContext(),"Location error",Toast.LENGTH_LONG).show();
                        }

                        //databaseHandler.uploadLocation("currentTime",location.getLatitude(), location.getLongitude(),"Position");

                    } else {
                        Toast.makeText(getApplicationContext(),"Cannot Get Location",Toast.LENGTH_LONG).show();
                    }
                }

                handler.postDelayed(this, DELAY_RUNNABLE_TIMER);
            }
        };
        System.gc();
    }
    //----------------------------------------------------------------------------------------------

    //------------------- get exact time at the time of upload location ----------------------------
    private String getCurrentTime() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(date);
    }


    /*----------------------------------------------------------------------------------------------
                    upload the device local database on
                        - No Internet
                        - Server upload failed
    ----------------------------------------------------------------------------------------------*/
    private void updateLocalDatabase(double latitude, double longitude, String status, String dateTimeStamp) {
        databaseHandler.uploadLocation(dateTimeStamp, latitude, longitude, status);
    }
    //----------------------------------------------------------------------------------------------

    /*----------------------------------------------------------------------------------------------
                        Upload data into server database on Connection
    ----------------------------------------------------------------------------------------------*/
    private void updateServerDatabase(final double latitude, final double longitude, final String status, final String dataTimeStamp) {

        /*------------------------------------------------------------------------------------------
                  Volley Instance and Connection to Upload location in server database
        ------------------------------------------------------------------------------------------*/
        //Toast.makeText(getApplicationContext(),"response",Toast.LENGTH_SHORT).show();
        requestQueue = Volley.newRequestQueue(this);

        if (!lastUploadedTime.equals(dataTimeStamp)) {

            stringRequest = new StringRequest(Request.Method.POST, UPLOAD_LOCATION_INTO_SERVER_DATABASE, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Toast.makeText(getApplicationContext(),response,Toast.LENGTH_SHORT).show();
                    System.out.println(response);
                    if (response.equals("ok")) {
                        Toast.makeText(getApplicationContext(),"Successfull", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(),"Failed", Toast.LENGTH_LONG).show();
                        updateLocalDatabase(latitude, longitude, status, dataTimeStamp);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    updateLocalDatabase(latitude, longitude, status, dataTimeStamp);
                }
            })
            {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String,String> params = new HashMap<>();
                    params.put("id",getSystemUniqueId());
                    params.put("latitude", Double.toString(latitude));
                    params.put("longitude", Double.toString(longitude));
                    params.put("status",status);
                    params.put("dataTimeStamp",dataTimeStamp);
                    return params;
                }
            };
            requestQueue.add(stringRequest);

            lastUploadedTime = dataTimeStamp;

            System.gc();
        }
        //stringRequest.cancel();
    }
    //----------------------------------------------------------------------------------------------

    /*----------------------------------------------------------------------------------------------
        Get location details from local database and getting ready to upload into server database
    ----------------------------------------------------------------------------------------------*/
    private void uploadFailedLocations(Cursor cursor) {
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
                Toast.makeText(getApplicationContext(),"Success to Delete",Toast.LENGTH_SHORT).show();
            }

            /*if (uploadFailedLocationIntoServer(upDateTime, latitude, longitude, position)) {
                Toast.makeText(getApplicationContext(),"Deletion",Toast.LENGTH_SHORT).show();
                databaseHandler.deleteLocationOnUpdate(upDateTime);
            }*/
        }

    }
    //----------------------------------------------------------------------------------------------

    /*----------------------------------------------------------------------------------------------
                        Upload local database location into Server database
    ----------------------------------------------------------------------------------------------*/
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

        requestQueue.add(stringRequest);

        return false;
    }
    //----------------------------------------------------------------------------------------------


    /*----------------------------------------------------------------------------------------------
                        Upload local database Image into Server database
    ----------------------------------------------------------------------------------------------*/
    private void uploadFailedImage (Cursor imagesData) {
        String upTime;
        double latitude;
        double longitude;
        String position;
        String image;

        while (imagesData.moveToNext()) {
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
    }

    //----------------------------------------------------------------------------------------------

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

    //---------------------- get system time to change Location reciever ---------------------------
    private int getCurrentTimeInHours() {
        return date.getHours();
    }

    //------------------------ get system unique id for Identification -----------------------------
    private String getSystemUniqueId() {
        String systemUniqueId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        return systemUniqueId;
    }

}
