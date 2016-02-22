package com.harrymt.productivitymapping;

/**
 * Created by harrymt on 23/11/15.
 */
public class ProjectStates
{
    public static boolean STUDYING = false;

    public static Integer SESSION_ID = 0;

    // App packages to block, e.g. calendar reminders, or google mail
    // "com.google.android.gm"; // google mail (gm), (calendar)

    static String[] PACKAGES_TO_BLOCK = {}; // = "com.harrymt.sendnotification";
    static String[] KEYWORDS_TO_LET_THROUGH = {}; // "IMPORTANT";

    class Broadcasts
    {
        static final String NOTIFICATION_POSTED = "NOTIFICATION_POSTED";
        static final String APP_USAGE = "APP_USAGE";
    }
}