package edu.education.myapplication;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class InternetDetails {

    public Context context;

    public InternetDetails(Context context) {
        this.context = context;
    }

    public boolean getConnectionDetails() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo == null) {
            return  false;
        }
        return true;
    }

}
