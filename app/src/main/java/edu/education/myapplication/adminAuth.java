package edu.education.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class adminAuth extends AppCompatActivity {

    private Button loginButton;
    private TextView warningMessage;
    private LinearLayout warning;
    private EditText username;
    private EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_auth);

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        warning = findViewById(R.id.warning);
        warningMessage = findViewById(R.id.message);

        /*FindMe findMe = new FindMe();
        findMe.getAccessPointLocations(this);*/

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                warning.setVisibility(View.GONE);

                String Username = username.getText().toString();
                String Password = password.getText().toString();

                if (Username.equals("") && Password.equals("")) {
                    warningMessage.setText("Missing Credentials");
                    warning.setVisibility(View.VISIBLE);
                } else {
                    if (Username.equals("admin") && Password.equals("admin@tracker")) {
                        Intent intent = new Intent(adminAuth.this, Dashboard.class);
                        ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(getApplicationContext(),R.anim.fade_out,R.anim.fade_in);
                        startActivity(intent,activityOptions.toBundle());
                    } else {
                        warningMessage.setText("Invalid Credentials");
                        warning.setVisibility(View.VISIBLE);
                    }
                }

            }
        });

    }
}
