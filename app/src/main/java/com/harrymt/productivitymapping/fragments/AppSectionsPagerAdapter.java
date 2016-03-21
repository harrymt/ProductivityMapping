package com.harrymt.productivitymapping.fragments;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.harrymt.productivitymapping.PROJECT_GLOBALS;

import java.util.Map;
import java.util.TreeMap;

/**
 * A FragmentPagerAdapter that returns a fragment corresponding to one of the primary
 * sections of the app.
 */
public class AppSectionsPagerAdapter extends FragmentPagerAdapter {
    private static final String TAG = PROJECT_GLOBALS.LOG_NAME + "AppSectionsPA";

    // All the fragments of the pager.
    TrackFragment t;
    ZonesFragment z;
    StatFragment s;

    /**
     * Constructor.
     *
     * @param fm Fragment manager.
     */
    public AppSectionsPagerAdapter(FragmentManager fm) {
        super(fm);
        t = new TrackFragment();
        z = new ZonesFragment();
        s = new StatFragment();
    }

    /**
     * Gets the fragment at the position of the pager.
     *
     * @param position position of fragment
     * @return fragment at the position
     */
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return t;
            case 1:
                return z;
            case 2:
                return s;
        }
        return null;
    }

    /**
     * Gets the number of fragments in the pager.
     *
     * @return number of fragments.
     */
    @Override
    public int getCount() {
        return 3;
    }

    /**
     * Get the title of each fragment.
     *
     * @param position Position of the fragment.
     * @return The title of the fragment.
     */
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Track";
            case 1:
                return "Zones";
            case 2:
                return "Stats";
        }
        return null;
    }
}