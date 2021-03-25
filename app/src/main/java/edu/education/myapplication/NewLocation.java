package edu.education.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class NewLocation extends AppCompatActivity {

    //private final static String ADD_NEW_LOCATION_URL = "http://192.168.43.225/locationtracker/index.php/welcome/addAccessPoint";
    private final static String ADD_NEW_LOCATION_URL = "https://tltms.tce.edu/tracker/locationtracker/index.php/welcome/addAccessPoint";

    private Button postLocation;
    private LinearLayout status;
    private TextView statusText;

    private EditText latitude;
    private EditText longitude;
    private EditText locationStatus;
    private EditText accuracy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_location);

        postLocation = findViewById(R.id.addLocation);
        status = findViewById(R.id.status);
        statusText = findViewById(R.id.statusText);

        latitude = findViewById(R.id.latitude);
        longitude = findViewById(R.id.longitude);
        locationStatus = findViewById(R.id.locationStatus);
        accuracy = findViewById(R.id.accuracy);

        postLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                status.setVisibility(View.INVISIBLE);

                final String newLatitude = latitude.getText().toString();
                final String newLongitude = longitude.getText().toString();
                final String newLocationStatus = locationStatus.getText().toString();
                final String newAccuracy = accuracy.getText().toString();

                if (newLatitude.isEmpty() || newLongitude.isEmpty() || newLocationStatus.isEmpty() || newAccuracy.isEmpty()) {
                    statusText.setText("Fields are empty");
                    statusText.setTextColor(getResources().getColor(R.color.white));
                    status.setBackgroundColor(getResources().getColor(R.color.red));
                } else {
                    //Code to upload the details into the database
                    RequestQueue requestQueue = Volley.newRequestQueue(NewLocation.this);
                    StringRequest stringRequest = new StringRequest(Request.Method.POST, ADD_NEW_LOCATION_URL, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            if (response.equals("ok")) {
                                status.setBackgroundColor(getResources().getColor(R.color.green));
                                statusText.setTextColor(getResources().getColor(R.color.black));
                                statusText.setText("Location Uploaded : Successful");
                            } else if (response.equals("error")) {
                                status.setBackgroundColor(getResources().getColor(R.color.red));
                                statusText.setTextColor(getResources().getColor(R.color.white));
                                statusText.setText("Location Upload : Failedd");
                            } else {
                                status.setBackgroundColor(getResources().getColor(R.color.red));
                                statusText.setTextColor(getResources().getColor(R.color.white));
                                statusText.setText(response);
                                System.out.println(response);
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            status.setBackgroundColor(getResources().getColor(R.color.paleYellow));
                            statusText.setTextColor(getResources().getColor(R.color.black));
                            statusText.setText("Error Occurred");
                        }
                    })
                    {
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String, String> params = new HashMap<>();
                            params.put("latitude", newLatitude);
                            params.put("longitude", newLongitude);
                            params.put("locationName", newLocationStatus);
                            params.put("distance", newAccuracy);
                            return params;
                        }
                    };
                    requestQueue.add(stringRequest);
                }
                status.setVisibility(View.VISIBLE);
            }
        });

    }
}
