package com.example.edreichua.myruns2;

/**
 * Created by edreichua on 4/22/16.
 */
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;

import java.util.HashMap;
import java.util.Map;

public class StartFragment extends Fragment {

    private Button startButton, syncButton;
    private Spinner inputSpinner, activitySpinner;
    private Intent mIntent;

    // Different types of input
    public static final String INPUT_TYPE = "input_type";
    private static final String MANUAL_ENTRY = "Manual Entry";
    private static final String AUTOMATIC = "Automatic";
    private static final String GPS = "GPS";

    public static final Map<String,Integer> INPUT_TO_ID_MAP;
    static{
        INPUT_TO_ID_MAP = new HashMap<>();
        INPUT_TO_ID_MAP.put(MANUAL_ENTRY,0);
        INPUT_TO_ID_MAP.put(AUTOMATIC,1);
        INPUT_TO_ID_MAP.put(GPS,2);
    }
    public static final String[] ID_TO_INPUT = {MANUAL_ENTRY, AUTOMATIC, GPS};


    // Different types of Activities
    public static final String ACTIVITY_TYPE = "activity_type";

    public static final Map<String,Integer> ACTIVITY_TO_ID_MAP;
    static{
        ACTIVITY_TO_ID_MAP = new HashMap<>();
        ACTIVITY_TO_ID_MAP.put("Running", 0);
        ACTIVITY_TO_ID_MAP.put("Walking", 1);
        ACTIVITY_TO_ID_MAP.put("Standing", 2);
        ACTIVITY_TO_ID_MAP.put("Cycling", 3);
        ACTIVITY_TO_ID_MAP.put("Hiking", 4);
        ACTIVITY_TO_ID_MAP.put("Downhill Skiing", 5);
        ACTIVITY_TO_ID_MAP.put("Cross-Country Skiing", 6);
        ACTIVITY_TO_ID_MAP.put("Snowboarding", 7);
        ACTIVITY_TO_ID_MAP.put("Skating", 8);
        ACTIVITY_TO_ID_MAP.put("Swimming", 9);
        ACTIVITY_TO_ID_MAP.put("Mountain Biking", 10);
        ACTIVITY_TO_ID_MAP.put("Wheelchair", 11);
        ACTIVITY_TO_ID_MAP.put("Elliptical", 12);
        ACTIVITY_TO_ID_MAP.put("Other", 13);
    }

    public static final String[] ID_TO_ACTIVITY = {"Running", "Walking", "Standing",
        "Cycling", "Hiking", "Downhill Skiing", "Cross-Country Skiing", "Snowboarding",
        "Skating", "Swimming", "Mountain Biking", "Wheelchair", "Elliptical", "Other"};


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_start, container, false);

        // Get the id for the buttons
        startButton = (Button) rootView.findViewById(R.id.button_start);
        syncButton = (Button) rootView.findViewById(R.id.button_sync);

        // Get the id for input type
        inputSpinner=(Spinner) rootView.findViewById(R.id.spinnerInputType);
        activitySpinner=(Spinner) rootView.findViewById(R.id.spinnerActivityType);

        // Trigger activity on selecting start button
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg) {

                final String inputText = inputSpinner.getSelectedItem().toString();
                final String activityText = activitySpinner.getSelectedItem().toString();

                switch(inputText){
                    case MANUAL_ENTRY:
                        mIntent = new Intent(getActivity(), ManualEntryActivity.class);
                        break;
                    case AUTOMATIC:
                        mIntent = new Intent(getActivity(),  MapDisplayActivity.class);
                        break;
                    case GPS:
                        mIntent = new Intent(getActivity(), MapDisplayActivity.class);
                        break;
                    default:
                        break;
                }

                mIntent.putExtra(INPUT_TYPE,INPUT_TO_ID_MAP.get(inputText));
                mIntent.putExtra(ACTIVITY_TYPE,ACTIVITY_TO_ID_MAP.get(activityText));
                getActivity().startActivity(mIntent);
            }
        });

        // Trigger activity on selecting sync button
        syncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg) {

            }
        });

        return rootView;
    }
}