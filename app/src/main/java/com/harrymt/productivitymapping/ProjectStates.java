package com.harrymt.productivitymapping;

import android.content.Context;
import android.location.Location;

/**
 * Created by harrymt on 23/11/15.
 */
public class ProjectStates
{
    public static boolean STUDYING = false;

    public static String base_url = "http://horizab1.miniserver.com/~harry/server/ProductivityMapping-Server/api/v1";

    public static Integer SESSION_ID = 0;
    static boolean IS_DEBUG = false;

    // App packages to block, e.g. calendar reminders, or google mail
    // "com.google.android.gm"; // google mail (gm), (calendar)
//
//    static String[] PACKAGES_TO_BLOCK = {}; // = "com.harrymt.sendnotification";
//    static String[] KEYWORDS_TO_LET_THROUGH = {}; // "IMPORTANT";

    static Zone CURRENT_ZONE;

    static Location LAST_LOCATION;

    public static String getUniqueDeviceId(Context c) {
        return android.provider.Settings.Secure.getString(c.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
    }

    class Broadcasts
    {
        static final String NOTIFICATION_POSTED = "NOTIFICATION_POSTED";
        static final String APP_USAGE = "APP_USAGE";
    }
}