package edu.education.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CameraActivity extends AppCompatActivity {

    //private static final String UPLOAD_IMAGE_INTO_SERVER_URL = "https://tltms.tce.edu/tracker/locationtracker/index.php/welcome/uploadImage";
    private static final String UPLOAD_IMAGE_INTO_SERVER_URL = "http://192.168.43.89/locationtracker/index.php/welcome/uploadImage";

    private LinearLayout goBackButton;
    private LinearLayout success;
    private LinearLayout failed;
    private LinearLayout noImage;
    private TextView uploading;

    private LinearLayout openCameraButton;
    private LinearLayout uploadImageButton;
    private ImageView viewUserImage;

    private Bitmap imageBitmap;

    private Connections connections;
    private GPSTracker gpsTracker;
    private FindMe findMe;
    private DatabaseHandler databaseHandler;

    private LastUpdated lastUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        viewUserImage = findViewById(R.id.userImage);

        lastUpdate = new LastUpdated(this);

        success = findViewById(R.id.success);
        noImage = findViewById(R.id.noImage);
        failed = findViewById(R.id.failed);
        uploading = findViewById(R.id.uploading);

        goBackButton = findViewById(R.id.goBack);
        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        openCameraButton = findViewById(R.id.openCameraBtn);
        openCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickUserImage();
            }
        });

        uploadImageButton = findViewById(R.id.uploadImageBtn);
        uploadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //UploadImageIntoServer(imageBitmap);
                success.setVisibility(View.GONE);
                failed.setVisibility(View.GONE);
                noImage.setVisibility(View.GONE);
                uploading.setVisibility(View.INVISIBLE);
                if (imageBitmap == null) {
                    noImage.setVisibility(View.VISIBLE);
                } else {
                    uploading.setVisibility(View.VISIBLE);
                    UploadImageIntoServer(imageBitmap);
                }
            }
        });

        connections = new Connections(this);
        gpsTracker = new GPSTracker(this);
        findMe = new FindMe(this);
        databaseHandler = new DatabaseHandler(this);

    }

    private void UploadImageIntoServer(final Bitmap bitmap) {



        if (connections.isMobileDataEnabled() || connections.isWiFiEnabled()) {

            RequestQueue requestQueue = Volley.newRequestQueue(this);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, UPLOAD_IMAGE_INTO_SERVER_URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    System.out.println(response);
                    uploading.setVisibility(View.INVISIBLE);
                    if (response.equals("ok")) {
                        success.setVisibility(View.VISIBLE);
                        lastUpdate.editSharedPreference(encodeImage(bitmap), getCurrentDateAndTime(), gpsTracker.getLatitude(), gpsTracker.getLongitude(), findMe.getLocationStatus(gpsTracker.getLatitude(),gpsTracker.getLongitude()),"Server");
                    } else {
                        //Push the image data into
                        failed.setVisibility(View.VISIBLE);
                        lastUpdate.editSharedPreference(encodeImage(bitmap), getCurrentDateAndTime(), gpsTracker.getLatitude(), gpsTracker.getLongitude(), findMe.getLocationStatus(gpsTracker.getLatitude(),gpsTracker.getLongitude()),"local");
                        databaseHandler.uploadImage(getCurrentDateAndTime(), gpsTracker.getLatitude(), gpsTracker.getLongitude(), findMe.getLocationStatus(gpsTracker.getLatitude(), gpsTracker.getLongitude()), encodeImage(bitmap));
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
                    Map<String,String> params = new HashMap<>();

                    Location location = gpsTracker.getLocation();
                    double latitude = gpsTracker.getLatitude();
                    double longitude = gpsTracker.getLongitude();
                    String locationStatus = findMe.getLocationStatus(latitude,longitude);

                    params.put("image",encodeImage(bitmap));
                    params.put("systemId",getSystemUniqueId());
                    params.put("exactTime",getCurrentDateAndTime());
                    params.put("longitude",String.valueOf(longitude));
                    params.put("latitude",String.valueOf(latitude));
                    params.put("status", locationStatus);

                    return params;
                }
            };

            requestQueue.add(stringRequest);

        } else {
            //upload into local database
            databaseHandler.uploadImage(getCurrentDateAndTime(), gpsTracker.getLatitude(), gpsTracker.getLongitude(), findMe.getLocationStatus(gpsTracker.getLatitude(), gpsTracker.getLongitude()), encodeImage(bitmap));
        }
    }

    private void clickUserImage() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permission,1000);
            } else {
                openCamera();
            }
        }

    }

    private void openCamera() {

        Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(openCameraIntent,1001);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1000:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera();
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            imageBitmap = (Bitmap) data.getExtras().get("data");
            viewUserImage.setImageBitmap(imageBitmap);
        }
    }

    private String encodeImage(Bitmap bitmap) {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);

        byte[] imageByte = byteArrayOutputStream.toByteArray();

        return Base64.encodeToString(imageByte, Base64.DEFAULT);

    }

    public String getSystemUniqueId() {
        String systemUniqueId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        return systemUniqueId;
    }

    private String getCurrentDateAndTime() {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(date);
    }
}
