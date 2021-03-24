package edu.education.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;

public class ImageLister extends AppCompatActivity {

    private DatabaseHandler databaseHandler;

    private LinearLayout successful;
    private LinearLayout lister;
    private RecyclerView recyclerView;

    private ArrayList<ImageDetails> details;
    private ImageListerAdapter adapter;

    private int unUploadedImageCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_lister);

        successful = findViewById(R.id.successful);
        lister = findViewById(R.id.lister);
        recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        databaseHandler = new DatabaseHandler(this);
        details = new ArrayList<>();
        adapter = new ImageListerAdapter(this, details);

        unUploadedImageCount = databaseHandler.getImageCount();

        if (unUploadedImageCount <= 0) {
            successful.setVisibility(View.VISIBLE);
            lister.setVisibility(View.GONE);
        } else {
            successful.setVisibility(View.GONE);
            lister.setVisibility(View.VISIBLE);

            Cursor cursor = databaseHandler.getImage();
            while (cursor.moveToNext()) {
                details.add(new ImageDetails(cursor.getString(0), cursor.getDouble(1), cursor.getDouble(2), cursor.getString(3), cursor.getString(4)));
                adapter.notifyDataSetChanged();
            }
        }
    }
}
