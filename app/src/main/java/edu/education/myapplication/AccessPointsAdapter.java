package edu.education.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AccessPointsAdapter extends RecyclerView.Adapter<AccessPointsAdapter.ViewHolder> {

    private ArrayList<AccessPointsDetails> details;
    private Context context;

    public AccessPointsAdapter(Context context, ArrayList<AccessPointsDetails> details) {
        this.context = context;
        this.details = details;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.access_points_lister, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AccessPointsDetails pointsDetails = details.get(position);
        holder.latitude.setText(String.valueOf(pointsDetails.getLatitude()));
        holder.longitude.setText(String.valueOf(pointsDetails.getLongitude()));
        holder.accessPointName.setText(pointsDetails.getLocationName());
        holder.accuracy.setText(String.valueOf(pointsDetails.getMinimumAccuracy()));
    }

    @Override
    public int getItemCount() {
        return details.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView latitude;
        public TextView longitude;
        public TextView accessPointName;
        public TextView accuracy;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            this.latitude = itemView.findViewById(R.id.latitude);
            this.longitude = itemView.findViewById(R.id.longitude);
            this.accessPointName = itemView.findViewById(R.id.locationName);
            this.accuracy = itemView.findViewById(R.id.accuracy);
        }
    }

}
