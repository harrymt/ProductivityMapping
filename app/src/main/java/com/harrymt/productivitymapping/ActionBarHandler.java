package com.harrymt.productivitymapping;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import com.harrymt.productivitymapping.fragments.AppSectionsPagerAdapter;

/**
 * Handles the setup of the action bar and listeners.
 */
public class ActionBarHandler implements ActionBar.TabListener {
    private static final String TAG = PROJECT_GLOBALS.LOG_NAME + "ActionBarHandler";

    // Page adapter that provides fragments for each 3 sections.
    public static AppSectionsPagerAdapter pagerAdapter;

    // The view pager that displays the 3 parts of the app.
    static ViewPager mViewPager;

    /**
     * Setup the actionbar by populating the given FragmentActivity with the fragments
     * from the page adapter.
     *
     * @param base Base fragment.
     */
    public void setup(FragmentActivity base) {

        // Create the adapter that will return a fragment for each of the three primary sections
        // of the app.
        pagerAdapter = new AppSectionsPagerAdapter(base.getSupportFragmentManager());

        // Set up the action bar.
        final ActionBar actionBar = base.getActionBar();

        if(actionBar == null) {
            Log.e(TAG, "Action bar setup failed!");
            return;
        }

        // Specify that the Home/Up button should not be enabled, since there is no hierarchical
        // parent.
        actionBar.setHomeButtonEnabled(false);

        // Specify that we will be displaying tabs in the action bar.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Set up the ViewPager, attaching the adapter and setting up a listener for when the
        // user swipes between sections.
        mViewPager = (ViewPager) base.findViewById(R.id.pager);
        mViewPager.setAdapter(pagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // When swiping between different app sections, select the corresponding tab.
                // We can also use ActionBar.Tab#select() to do this if we have a reference to the
                // Tab.
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < pagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by the adapter.
            // Also specify this Activity object, which implements the TabListener interface, as the
            // listener for when this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(pagerAdapter.getPageTitle(i))
                            .setTabListener(this)
            );

        }

    }

    /**
     * When the given tab is selected, switch to the corresponding page in the ViewPager.
     *
     * @param tab The tab.
     * @param fragmentTransaction What happened to the fragment.
     */
    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    /**
     * Do nothing with these callbacks.
     */
    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {}
    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {}
}