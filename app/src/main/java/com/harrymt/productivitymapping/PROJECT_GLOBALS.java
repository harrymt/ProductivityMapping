package com.harrymt.productivitymapping;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by harrymt on 23/11/15.
 */
public class PROJECT_GLOBALS
{
    public static boolean STUDYING = false;

    public static String base_url(Context c) {
        return getEnvironmentVariableMetaData(c, "api_server_url");
    }

    public static Integer SESSION_ID = 0;
    public static boolean IS_DEBUG = false;

    public static Zone CURRENT_ZONE;

    public static ArrayList<String> TOP_APPS_BLOCKED;

    public static String apiKey(Context c) {
        String secret_key = getEnvironmentVariableMetaData(c, "api_key");
        return "?apikey=" + secret_key;
    }

    private static String getEnvironmentVariableMetaData(Context context, String resourceID) {
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            return bundle.getString(resourceID);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("g53ids", "Failed to load meta-data, NameNotFound: " + e.getMessage());
        } catch (NullPointerException e) {
            Log.e("g53ids", "Failed to load meta-data, NullPointer: " + e.getMessage());
        }
        return null;
    }

    public static String getUniqueDeviceId(Context c) {
        return android.provider.Settings.Secure.getString(c.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
    }

    public class Broadcasts
    {
        public static final String NOTIFICATION_POSTED = "NOTIFICATION_POSTED";
        public static final String APP_USAGE = "APP_USAGE";
    }
}