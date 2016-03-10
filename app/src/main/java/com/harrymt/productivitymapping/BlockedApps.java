package com.harrymt.productivitymapping;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by harrymt on 09/03/16.
 */
public class BlockedApps {
    public String name;
    public String package_name;
    public Drawable icon;
    public boolean isPopular;

    public BlockedApps(String n, String pn, Drawable i, boolean p) {
        name = n; package_name = pn; icon = i; isPopular = p;
    }

    static public ArrayList<BlockedApps> getListOfApps(Context c) {
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        final List<ResolveInfo> pkgAppsList = c.getPackageManager().queryIntentActivities( mainIntent, 0);
        ArrayList<BlockedApps> o = new ArrayList<>();
        boolean popularApp;

        ArrayList<BlockedApps> popularApps = new ArrayList<>();

        for (ResolveInfo info: pkgAppsList) {
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

        // Put popular apps at the start
        o.addAll(0, popularApps);

        return o;
    }
}
