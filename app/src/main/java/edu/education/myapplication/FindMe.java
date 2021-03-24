package edu.education.myapplication;

import android.content.Context;
import android.database.Cursor;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FindMe {

    private DatabaseHandler databaseHandler;
    private Context context;

    public FindMe(Context context) {
        this.context = context;
    }

    private static final String ACCESS_POINT_LOCATIONS_DATA_URL = "http://192.168.43.89/locationtracker/index.php/welcome/getAccessPoints";
    //private static final String ACCESS_POINT_LOCATIONS_DATA_URL = "https://tltms.tce.edu/tracker/locationtracker/index.php/welcome/getAccessPoints";

    //Code to Upload location Access Points into Local database
    public void getAccessPointLocations(final Context context) {

        RequestQueue requestQueue;
        JsonObjectRequest objectRequest;

        databaseHandler = new DatabaseHandler(context);

        if (databaseHandler.getAccessPointCount() > 0) {
            databaseHandler.deleteAccessPointTable();
        }


        requestQueue = Volley.newRequestQueue(context);
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
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, "Access point Error", Toast.LENGTH_LONG).show();
            }
        });
        requestQueue.add(objectRequest);

    }

    public String getLocationStatus(double latitude, double longitude) {

        databaseHandler = new DatabaseHandler(context);
        Cursor cursor = databaseHandler.getAccessPoints();

        while (cursor.moveToNext()) {
            double accessLatitude = cursor.getDouble(0);
            double accessLongitude = cursor.getDouble(1);
            String accessPointName = cursor.getString(2);
            double accessAccuracy = cursor.getDouble(3);

            if (calculateDistance(accessLatitude, accessLongitude, latitude, longitude) <= accessAccuracy) {
                return accessPointName;
            }
        }

        return "Other Location";

    }

    /*----------------------------------------------------------------------------------------------
                    get location attributes and calculate the distance
    ----------------------------------------------------------------------------------------------*/
    private int calculateDistance(double blockLatitude, double blockLongitude, double userLatitude, double userLongitude) {
        double theta = blockLongitude - userLongitude;
        double distance = Math.sin(deg2rad(blockLatitude)) * Math.sin(deg2rad(userLatitude)) + Math.cos(deg2rad(blockLatitude)) * Math.cos(deg2rad(userLatitude)) * Math.cos(deg2rad(theta));
        distance = Math.acos(distance);
        distance = rad2deg(distance);
        distance = distance * 60 * 1.1515;
        distance = distance * 1.609344;
        distance = distance * 1000;
        return (int) Math.round(distance);
    }

    //------------------------------ Mathematical Calculation Methods ------------------------------
    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }
    //----------------------------------------------------------------------------------------------

}
