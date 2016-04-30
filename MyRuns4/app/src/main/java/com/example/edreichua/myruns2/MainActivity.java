package com.example.edreichua.myruns2;


import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.example.edreichua.myruns2.view.SlidingTabLayout;

import java.util.ArrayList;

public class MainActivity extends Activity {

    private SlidingTabLayout mainSlidingTabs;
    private ViewPager mainViewPage;
    private ArrayList<Fragment> frags;
    private TabsViewPageAdapter mainViewPageAdapter;
    public static Fragment startFragment, historyFragment, settingFragment;
    public static ExerciseEntryDbHelper DBhelper;

    /////////////////////// Override core functionality ///////////////////////

    /**
     * Handle creating of activity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Create main layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create fragment list
        startFragment = new StartFragment();
        historyFragment = new HistoryFragment();
        settingFragment = new SettingsFragment();

        frags = new ArrayList<Fragment>();
        frags.add(startFragment);
        frags.add(historyFragment);
        frags.add(settingFragment);

        if(savedInstanceState == null){
            DBhelper = new ExerciseEntryDbHelper(this);
        }

        // Bind the tabs with the fragment
        mainViewPage = (ViewPager) findViewById(R.id.viewpager);
        mainViewPageAdapter =new TabsViewPageAdapter(getFragmentManager(), frags);
        mainViewPage.setAdapter(mainViewPageAdapter);

        mainViewPage.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Empty method body
            }

            @Override
            public void onPageSelected(int position) {
                if(position == 1) {
                    Bundle param = new Bundle();
                    param.putBoolean("refresh",true);
                    historyFragment.onCreate(param);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // Empty method body
            }
        });

        // Distribute the tabs evenly and bind it to the layout
        mainSlidingTabs = (SlidingTabLayout) findViewById(R.id.tab);
        mainSlidingTabs.setDistributeEvenly(true);
        mainSlidingTabs.setViewPager(mainViewPage);

    }
}