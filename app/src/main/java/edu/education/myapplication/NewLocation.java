package edu.education.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class NewLocation extends AppCompatActivity {

    private LinearLayout postLocation;
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

                String newLatitude = latitude.getText().toString();
                String newLongitude = longitude.getText().toString();
                String newLocationStatus = locationStatus.getText().toString();
                String newAccuracy = accuracy.getText().toString();

                if (newLatitude.isEmpty() || newLongitude.isEmpty() || newLocationStatus.isEmpty() || newAccuracy.isEmpty()) {
                    statusText.setText("Fields are empty");
                    statusText.setTextColor(getResources().getColor(R.color.white));
                    status.setBackgroundColor(getResources().getColor(R.color.red));
                } else {
                    //Code to upload the details into the database
                }

            }
        });

    }
}
