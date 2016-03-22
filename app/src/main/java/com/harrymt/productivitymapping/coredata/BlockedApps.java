package com.harrymt.productivitymapping.coredata;

import android.content.Context;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import com.harrymt.productivitymapping.PROJECT_GLOBALS;
import com.harrymt.productivitymapping.utility.Util;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Wrapper for an app that will be used in the List View to display
 * blocked apps for a zone.
 */
public class BlockedApps implements Comparable<BlockedApps> {
    private static final String TAG = PROJECT_GLOBALS.LOG_NAME + "BlockedApps";

    // Details about the app.
    public String name;
    public String package_name;
    public Drawable icon;
    // If the app is popular
    public boolean isPopular;

    /**
     * Constructor for a Blocked App.
     * @param n Name of app.
     * @param pn Package Name of app.
     * @param i Icon of app.
     * @param p Is the app a popular app.
     */
    public BlockedApps(String n, String pn, Drawable i, boolean p) {
        name = n; package_name = pn; icon = i; isPopular = p;
    }

    /**
     * Gets a list of apps on the phone, sorted by package name,
     * with the popular apps (from the Global object) at the front.
     *
     * @param c Context of app.
     * @return List of apps, popular ones at the front of the list.
     */
    static public ArrayList<BlockedApps> getListOfApps(Context c) {
        final List<ResolveInfo> allAppsOnPhone = Util.getListOfAppsOnPhone(c);

        final List<ResolveInfo> apps = Util.filterUnusedSystemApps(allAppsOnPhone);

        // Strip the information from the ResolveInfo object putting the popular apps at the top.
        ArrayList<BlockedApps> o = new ArrayList<>();
        boolean popularApp;
        ArrayList<BlockedApps> popularApps = new ArrayList<>();
        for (ResolveInfo info: apps) {
            popularApp = false;

            Drawable icon = c.getPackageManager().getApplicationIcon(info.activityInfo.applicationInfo);
            final String title 	= c.getPackageManager().getApplicationLabel(info.activityInfo.applicationInfo).toString();

            for(String app : PROJECT_GLOBALS.TOP_APPS_BLOCKED) {
                if(app.equals(info.activityInfo.packageName)) {
                    popularApp = true;
                }
            }

            if(popularApp) {
                popularApps.add(new BlockedApps(title, info.activityInfo.packageName, icon, true));
            } else {
                o.add(new BlockedApps(title, info.activityInfo.packageName, icon, false));
            }
        }

        // Sort the list
        Collections.sort(o);

        // Put popular apps at the start
        o.addAll(0, popularApps);

        return o;
    }

    /**
     * Compare the name of a blocked app with another.
     *
     * @param another The other blocked app.
     * @return True if name is same.
     */
    @Override
    public int compareTo(@NonNull BlockedApps another) {
        return this.name.compareTo(another.name);
    }
}