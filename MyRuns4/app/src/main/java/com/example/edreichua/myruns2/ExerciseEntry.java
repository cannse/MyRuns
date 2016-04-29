package com.example.edreichua.myruns2;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by edreichua on 4/22/16.
 */
public class ExerciseEntry {
    private Long id;
    private int mInputType;        // Manual, GPS or automatic
    private int mActivityType;     // Running, cycling etc.
    private long mDateTime;        // When does this entry happen
    private int mDuration;         // Exercise duration in seconds
    private double mDistance;      // Distance traveled. Either in meters or feet.
    private double mAvgPace;       // Average pace
    private double mAvgSpeed;      // Average speed
    private int mCalorie;          // Calories burnt
    private double mClimb;         // Climb. Either in meters or feet.
    private int mHeartRate;        // Heart rate
    private String mComment;       // Comments
    private ArrayList<LatLng> mLocationList; // Location list

    // Constructor
    public void ExerciseEntry(){
        mInputType = 0;
        mActivityType = 0;
    }
    // Setter and Getter for id
    public void setmId(long id) {
        this.id = id;
    }
    public long getmId() {
        return id;
    }

    // Setter and Getter for InputType
    public void setmInputType(int mInputType) {
        this.mInputType = mInputType;
    }
    public int getmInputType() {
        return mInputType;
    }

    // Setter and Getter for ActivityType
    public void setmActivityType(int mActivityType) {
        this.mActivityType = mActivityType;
    }
    public int getmActivityType() {
        return mActivityType;
    }

    // Setter and Getter for DateTime
    public void setmDateTime(long mDateTime) {
        this.mDateTime = mDateTime;
    }
    public long getmDateTime() {
        return mDateTime;
    }

    // Setter and Getter for Duration
    public void setmDuration(int mDuration) {
        this.mDuration = mDuration;
    }
    public int getmDuration() {
        return mDuration;
    }

    // Setter and Getter for Distance
    public void setmDistance(double mDistance) {
        this.mDistance = mDistance;
    }
    public double getmDistance() {
        return mDistance;
    }

    // Setter and Getter for Avg Pace
    public void setmAvgPace(double mAvgPace) {
        this.mAvgPace = mAvgPace;
    }
    public double getmAvgPace() {
        return mAvgPace;
    }

    // Setter and Getter for Avg Speed
    public void setmAvgSpeed(double mAvgSpeed) {
        this.mAvgSpeed = mAvgSpeed;
    }
    public double getmAvgSpeed() {
        return mAvgSpeed;
    }

    // Setter and Getter for Calories
    public void setmCalorie(int mCalorie) {
        this.mCalorie = mCalorie;
    }
    public int getmCalorie() {
        return mCalorie;
    }

    // Setter and Getter for Climb
    public void setmClimb(double mClimb) {
        this.mClimb = mClimb;
    }
    public double getmClimb() {
        return mClimb;
    }

    // Setter and Getter for Heartrate
    public void setmHeartRate(int mHeartRate) {
        this.mHeartRate = mHeartRate;
    }
    public int getmHeartRate() {
        return mHeartRate;
    }

    // Setter and Getter for Comment
    public void setmComment(String mComment) {
        this.mComment = mComment;
    }
    public String getmComment() {
        return mComment;
    }

    // Setter for Location List
    public void setmLocationList(ArrayList<LatLng> mLocationList) { this.mLocationList = mLocationList; }

    // Setter (adder) and Getter for Location List
    public void setmLocationList(LatLng mLatLng) { this.mLocationList.add(mLatLng); }
    public ArrayList<LatLng> getmLocationList() { return mLocationList; }

    // Will be used by the ArrayAdapter in the ListView
    @Override
    public String toString() {
        return mInputType+": "+mActivityType+", "+mDateTime;
    }
}
