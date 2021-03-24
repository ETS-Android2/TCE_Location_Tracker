package edu.education.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ImageListerAdapter extends RecyclerView.Adapter<ImageListerAdapter.ViewHolder> {

    private Context context;
    private ArrayList<ImageDetails> details;

    public ImageListerAdapter(Context context, ArrayList<ImageDetails> details) {
        this.context = context;
        this.details = details;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.unloaded_image_lister, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ImageDetails imageDetails = details.get(position);

        holder.uploadedImage.setImageBitmap(getImage(imageDetails.getEncodedImage()));
        holder.timeStamp.setText(imageDetails.getTimeStamp());
        holder.latitude.setText(String.valueOf(imageDetails.getLatitude()));
        holder.longitude.setText(String.valueOf(imageDetails.getLongitude()));
        holder.locationStatus.setText(imageDetails.getPosition());
    }

    @Override
    public int getItemCount() {
        return details.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView uploadedImage;
        private TextView timeStamp;
        private TextView latitude;
        private TextView longitude;
        private TextView locationStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            uploadedImage = itemView.findViewById(R.id.uploadedImage);
            timeStamp = itemView.findViewById(R.id.dateTimeStamp);
            latitude = itemView.findViewById(R.id.latitude);
            longitude = itemView.findViewById(R.id.longitude);
            locationStatus = itemView.findViewById(R.id.locationStatus);

        }
    }

    public Bitmap getImage(String imageString) {
        byte[] encodeByte = Base64.decode(imageString, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
        return bitmap;
    }

}
