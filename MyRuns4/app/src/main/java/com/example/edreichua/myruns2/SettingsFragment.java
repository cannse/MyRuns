package com.example.edreichua.myruns2;

/**
 * Created by edreichua on 4/22/16.
 */

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the settings_preferences from an XML resource
        addPreferencesFromResource(R.xml.settings_preferences);

    }
}
