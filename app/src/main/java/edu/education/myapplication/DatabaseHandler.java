package edu.education.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHandler extends SQLiteOpenHelper {

    public static final String DB_NAME = "local";
    public static final int CB_VERSION = 1;

    public DatabaseHandler(Context context) {
        super(context, DB_NAME, null, CB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE_QUERY = "create table user_location (up_date TEXT NOT NULL, latitude DOUBLE NOT NULL, longitude DOUBLE NOT NULL, position TEXT NOT NULL)";
        String CREATE_IMAGE_QUERY = "create table user_image (up_date TEXT NOT NULL, latitude DOUBLE NOT NULL, longitude DOUBLE NOT NULL, position TEXT NOT NULL, image TEXT NOT NULL)";
        String CREATE_LOCATION_ACCESS_POINTS_QUERY = "create table locations (latitude DOUBLE NOT NULL, longitude DOUBLE NOT NULL, name TEXT NOT NULL, minimum DOUBLE NOT NULL)";

        //------------------------------- CREATE TABLE QUERY ---------------------------------------
        db.execSQL(CREATE_TABLE_QUERY);
        db.execSQL(CREATE_IMAGE_QUERY);
        db.execSQL(CREATE_LOCATION_ACCESS_POINTS_QUERY);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /*----------------------------------------------------------------------------------------------
                    Upload and Update Location Access Points into Local Database
    ----------------------------------------------------------------------------------------------*/
    public void uploadAccessPoints(double latitude, double longitude, String locationName, double minDistance) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("latitude", latitude);
        contentValues.put("longitude", longitude);
        contentValues.put("name", locationName);
        contentValues.put("minimum", minDistance);
        long result = database.insert("locations", null, contentValues);
    }
    //----------------------------------------------------------------------------------------------

    /*----------------------------------------------------------------------------------------------
            DELETE TABLE DATA FROM LOCAL ACCESS POINT TABLE TO UPDATE NEW ACCESS POINTS
    ----------------------------------------------------------------------------------------------*/
    public void deleteAccessPointTable() {
        String deleteQuery = "DELETE FROM locations";
        SQLiteDatabase database = this.getWritableDatabase();
        database.delete("locations", null, null);
    }
    //----------------------------------------------------------------------------------------------

    /*----------------------------------------------------------------------------------------------
            GET THE NUMBER OF ACCESS POINTS LOADED ITO THE LOCAL DATABASE FROM SERVER
    ----------------------------------------------------------------------------------------------*/
    public int getAccessPointCount() {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM locations",null);
        if (cursor != null) {
            return cursor.getCount();
        } else {
            return 0;
        }
    }
    //----------------------------------------------------------------------------------------------

    /*----------------------------------------------------------------------------------------------
               Upload location into local database incase of internet or Server Failure
    ----------------------------------------------------------------------------------------------*/
    public boolean uploadLocation(String currentTime, double latitude, double longitude, String postion) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("up_date",currentTime);
        contentValues.put("latitude",latitude);
        contentValues.put("longitude",longitude);
        contentValues.put("position",postion);
        long result = database.insert("user_location",null, contentValues);

        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }
    //----------------------------------------------------------------------------------------------

    /*----------------------------------------------------------------------------------------------
               Get stored location when there is availabilty of Internet and has data
    ----------------------------------------------------------------------------------------------*/
    public Cursor getLocation() {
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM user_location",null);
        return cursor;
    }
    //----------------------------------------------------------------------------------------------

    /*----------------------------------------------------------------------------------------------
               Get updated location count to trigger getLocation in case of re-attempt
    ----------------------------------------------------------------------------------------------*/
    public int getLocationCount() {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM user_location", null);
        if (cursor != null) {
            return cursor.getCount();
        } else {
            return -1;
        }
    }
    //----------------------------------------------------------------------------------------------

    /*----------------------------------------------------------------------------------------------
                    Delete location from database on Uploading into Server Database
    ----------------------------------------------------------------------------------------------*/
    public void deleteLocationOnUpdate(String uploadDateTime) {
        SQLiteDatabase database = this.getWritableDatabase();
        database.delete("user_location","up_date='" + uploadDateTime + "'",null);
    }
    //----------------------------------------------------------------------------------------------

    /*----------------------------------------------------------------------------------------------
               Upload image and other details into local database in case of no Internet
    ----------------------------------------------------------------------------------------------*/
    public boolean uploadImage(String currentTime, double latitude, double longitude, String position, String imageString) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("up_date", currentTime);
        contentValues.put("latitude", latitude);
        contentValues.put("longitude", longitude);
        contentValues.put("position", position);
        contentValues.put("image", imageString);

        long result = database.insert("user_image", null, contentValues);

        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }
    //----------------------------------------------------------------------------------------------

    /*----------------------------------------------------------------------------------------------
                Get stored images when there is availability of Internet and has data
    ----------------------------------------------------------------------------------------------*/
    public Cursor getImage() {
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM user_image", null);
        return cursor;
    }
    //----------------------------------------------------------------------------------------------

    /*----------------------------------------------------------------------------------------------
               Upload image and other details into local database in case of no Internet
    ----------------------------------------------------------------------------------------------*/
    public int getImageCount() {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM user_image",null);
        if (cursor != null) {
            return cursor.getCount();
        } else {
            return -1;

        }
    }
    //----------------------------------------------------------------------------------------------

    /*----------------------------------------------------------------------------------------------
                    Delete location from database on Uploading into Server Database
    ----------------------------------------------------------------------------------------------*/
    public void deleteImageOnUpdate (String dateTime) {
        SQLiteDatabase database = this.getWritableDatabase();
        database.delete("user_image","up_date='" + dateTime + "'",null);
    }
    //----------------------------------------------------------------------------------------------

    /*----------------------------------------------------------------------------------------------
                        Get stored Access Point locations from Local Database
    ----------------------------------------------------------------------------------------------*/
    public Cursor getAccessPoints() {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM locations", null);
        return cursor;
    }
    //----------------------------------------------------------------------------------------------

    public int getCount() {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        sqLiteDatabase.rawQuery("SELECT COUNT(*) FROM locations",null);
        return 0;
    }

}
