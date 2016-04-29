package com.example.edreichua.myruns2;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

import java.util.ArrayList;

/**
 * Created by edreichua on 4/22/16.
 */
public class TabsViewPageAdapter extends FragmentPagerAdapter{

    private ArrayList<Fragment> frag;

    // IDs
    public static final int TAB_START_ID = 0;
    public static final int TAB_HISTORY_ID = 1;
    public static final int TAB_SETTING_ID = 2;

    // String names
    public static final String TAB_START_STRING = "START";
    public static final String TAB_HISTORY_STRING = "HISTORY";
    public static final String TAB_SETTING_STRING = "SETTINGS";

    public TabsViewPageAdapter(FragmentManager fm, ArrayList<Fragment> frag){
        super(fm);
        this.frag = frag;
    }

    public Fragment getItem(int pos){
        return frag.get(pos);
    }

    public int getCount(){
        return frag.size();
    }

    public CharSequence getPageTitle(int position) {

        switch (position) {

            case TAB_START_ID:
                return TAB_START_STRING;

            case TAB_HISTORY_ID:
                return TAB_HISTORY_STRING;

            case TAB_SETTING_ID:
                return TAB_SETTING_STRING;

            default:
                break;
        }
        return null;
    }
}
