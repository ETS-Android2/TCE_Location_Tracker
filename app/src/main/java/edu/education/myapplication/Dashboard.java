package edu.education.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

public class Dashboard extends AppCompatActivity {

    private LinearLayout newLocation;
    private LinearLayout locationLister;
    private LinearLayout imageLister;
    private LinearLayout accessPointsLister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        newLocation = findViewById(R.id.newLocation);
        locationLister = findViewById(R.id.location);
        imageLister = findViewById(R.id.image);
        accessPointsLister = findViewById(R.id.accessDatabase);

        newLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        locationLister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Dashboard.this, LocationLister.class);
                ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(getApplicationContext(),R.anim.fade_out,R.anim.fade_in);
                startActivity(intent,activityOptions.toBundle());
            }
        });

        imageLister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Dashboard.this, ImageLister.class);
                ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(getApplicationContext(),R.anim.fade_out,R.anim.fade_in);
                startActivity(intent,activityOptions.toBundle());
            }
        });

        accessPointsLister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Dashboard.this, AccessPoints.class);
                ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(getApplicationContext(),R.anim.fade_out,R.anim.fade_in);
                startActivity(intent,activityOptions.toBundle());
            }
        });

    }
}
