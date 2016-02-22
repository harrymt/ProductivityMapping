package com.harrymt.productivitymapping;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
 * sections of the app.
 */
public class AppSectionsPagerAdapter extends FragmentPagerAdapter {

    public AppSectionsPagerAdapter(FragmentManager fm) {
        super(fm);
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
                return new TrackFragment();
            case 1:
                return new StatFragment();
            case 2:
                return new MapFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Track";
            case 1:
                return "Stats";
            case 2:
                return "Map";
        }
        return null;
    }
}