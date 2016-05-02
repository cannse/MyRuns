package com.example.edreichua.myruns2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.sql.Blob;
import java.util.ArrayList;

/**
 * Created by edreichua on 4/22/16.
 */
public class ExerciseEntryDbHelper extends SQLiteOpenHelper {

    // Important table information
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "entry.db";
    public static final String TABLE_NAME_ENTRIES = "entry";
    private static final String TAG = "Storing in Database";

    // Keys for table
    public static final String KEY_ROWID = "_id";
    public static final String KEY_INPUT_TYPE = "input_type";
    public static final String KEY_ACTIVITY_TYPE = "activity_type";
    public static final String KEY_DATE_TIME = "date_time";
    public static final String KEY_DURATION = "duration";
    public static final String KEY_DISTANCE = "distance";
    public static final String KEY_AVG_PACE = "avg_pace";
    public static final String KEY_AVG_SPEED = "avg_speed";
    public static final String KEY_CALORIES = "calories";
    public static final String KEY_CLIMB = "climb";
    public static final String KEY_HEARTRATE = "heartrate";
    public static final String KEY_COMMENT = "comment";
    public static final String KEY_PRIVACY = "privacy";
    public static final String KEY_GPS_DATA = "gps";

    // Database creation sql statement
    public static final String CREATE_TABLE_ENTRIES = "CREATE TABLE IF NOT EXISTS "
            + TABLE_NAME_ENTRIES + " ("
            + KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + KEY_INPUT_TYPE + " INTEGER NOT NULL, "
            + KEY_ACTIVITY_TYPE + " INTEGER NOT NULL, "
            + KEY_DATE_TIME + " DATETIME NOT NULL, "
            + KEY_DURATION + " INTEGER NOT NULL, "
            + KEY_DISTANCE + " FLOAT, "
            + KEY_AVG_PACE + " FLOAT, "
            + KEY_AVG_SPEED + " FLOAT,"
            + KEY_CALORIES + " INTEGER, "
            + KEY_CLIMB + " FLOAT, "
            + KEY_HEARTRATE + " INTEGER, "
            + KEY_COMMENT + " TEXT, "
            + KEY_PRIVACY + " INTEGER, "
            + KEY_GPS_DATA + " BLOB " + ");";

    // Constructor
    public ExerciseEntryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Create table schema if not exists
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_ENTRIES);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(ExerciseEntryDbHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_ENTRIES);
        onCreate(db);
    }


    // Insert a item given each column value
    public long insertEntry(ExerciseEntry entry) {

        // Insert all the values
        ContentValues values = new ContentValues();
        values.put(KEY_INPUT_TYPE,entry.getmInputType());
        values.put(KEY_ACTIVITY_TYPE,entry.getmActivityType());
        values.put(KEY_DATE_TIME, entry.getmDateTime());
        values.put(KEY_DURATION,entry.getmDuration());
        values.put(KEY_DISTANCE,entry.getmDistance());
        //values.put(KEY_AVG_PACE,entry.getmAvgPace());
        values.put(KEY_AVG_SPEED,entry.getmAvgSpeed());
        values.put(KEY_CALORIES,entry.getmCalorie());
        values.put(KEY_CLIMB,entry.getmClimb());
        values.put(KEY_HEARTRATE,entry.getmHeartRate());
        values.put(KEY_COMMENT,entry.getmComment());

        Gson gson = new Gson();
        values.put(KEY_GPS_DATA,gson.toJson(entry.getmLocationList()).getBytes());

        // Insert to database
        SQLiteDatabase database = getWritableDatabase();
        long insertId = database.insert(TABLE_NAME_ENTRIES, null, values);
        database.close();
        return insertId;
    }


    // Remove an entry by giving its index
    public void removeEntry(long rowIndex) {

        SQLiteDatabase database = getWritableDatabase();
        database.delete(TABLE_NAME_ENTRIES, KEY_ROWID
                + " = " + rowIndex, null);
        database.close();
    }


    // Query a specific entry by its index.

    public ExerciseEntry fetchEntryByIndex(long rowId) {
        SQLiteDatabase database = getReadableDatabase();

        Cursor cursor = database.query(TABLE_NAME_ENTRIES, null,
                KEY_ROWID + " = " + rowId, null, null, null, null);
        cursor.moveToFirst();
        ExerciseEntry entry = cursorToExerciseEntry(cursor);
        Log.d(TAG, cursorToExerciseEntry(cursor).toString());

        cursor.close();
        database.close();

        return entry;
    }



    // Query the entire table, return all rows
    public ArrayList<ExerciseEntry> fetchEntries() {

        ArrayList<ExerciseEntry> entries = new ArrayList<>();
        SQLiteDatabase database = getReadableDatabase();

        Cursor cursor = database.query(TABLE_NAME_ENTRIES,
                null, null, null, null, null, null);

        cursor.moveToFirst();

        // fetch entry one by one until cursor reaches the end
        while (!cursor.isAfterLast()) {
            ExerciseEntry entry = cursorToExerciseEntry(cursor);
            Log.d(TAG, cursorToExerciseEntry(cursor).toString());
            entries.add(entry);
            cursor.moveToNext();
        }

        cursor.close();
        database.close();

        return entries;
    }

    private ExerciseEntry cursorToExerciseEntry(Cursor cursor) {

        ExerciseEntry entry = new ExerciseEntry();
        entry.setmId(cursor.getLong(0));
        entry.setmInputType(cursor.getInt(1));
        entry.setmActivityType(cursor.getInt(2));
        entry.setmDateTime(cursor.getLong(3));
        entry.setmDuration(cursor.getInt(4));
        entry.setmDistance(cursor.getDouble(5));
        entry.setmAvgPace(cursor.getDouble(6));
        entry.setmAvgSpeed(cursor.getDouble(7));
        entry.setmCalorie(cursor.getInt(8));
        entry.setmClimb(cursor.getDouble(9));
        entry.setmHeartRate(cursor.getInt(10));
        entry.setmComment(cursor.getString(11));
        Gson gson = new Gson();
        String json = new String(cursor.getBlob(13));
        Type type = new TypeToken<ArrayList<LatLng>>() {}.getType();
        entry.setmLocationList((ArrayList<LatLng>)gson.fromJson(json, type));

        return entry;
    }

}
