package com.harrymt.productivitymapping;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.harrymt.productivitymapping.coredata.Zone;

import java.util.ArrayList;

/**
 * List of global project variables and settings that can be easily accessed
 * and changed in one place.
 */
public class PROJECT_GLOBALS {
    private static final String TAG = PROJECT_GLOBALS.LOG_NAME + "PROJECT_GLOBALS";

    // Set a standard name for each log message, for easier debugging.
    public static final String LOG_NAME = "g53ids-";

    // Request codes for the Activity lifecycle.
    public static int REQUEST_CODE_SET_ZONE_PREFS = 3212;
    public static int REQUEST_CODE_EDIT_ZONE = 3;
    public static int REQUEST_CODE_SET_ZONE = 4;

    // Current state information
    public static boolean STUDYING = false;
    public static Integer SESSION_ID = 0;
    public static boolean IS_DEBUG = false;
    public static Zone CURRENT_ZONE;
    public static ArrayList<String> TOP_APPS_BLOCKED = new ArrayList<>();

    // Broadcast codes for the receivers.
    public class Broadcasts {
        public static final String NOTIFICATION_POSTED = "NOTIFICATION_POSTED";
        public static final String APP_USAGE = "APP_USAGE";
    }

    /**
     * Get the API key for our API calls.
     * @param c Context of app.
     * @return The API key.
     */
    public static String apiKey(Context c) {
        String secret_key = getEnvironmentVariableMetaData(c, "api_key");
        return "?apikey=" + secret_key;
    }

    /**
     * Get a unique id for this device.
     * @param c Context of app.
     * @return A unique device id as a string.
     */
    public static String getUniqueDeviceId(Context c) {
        return android.provider.Settings.Secure.getString(c.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
    }

    /**
     * Get the base server API url.
     * @param c Context of app.
     * @return The server url.
     */
    public static String base_url(Context c) {
        return getEnvironmentVariableMetaData(c, "api_server_url");
    }

    /**
     * Gets variables from the XML Environment variables file
     * @param context this.
     * @param resourceID String of variable.
     * @return The resource string if found, null if doesn't exist.
     */
    private static String getEnvironmentVariableMetaData(Context context, String resourceID) {
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            return bundle.getString(resourceID);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Failed to load meta-data, NameNotFound: " + e.getMessage());
        } catch (NullPointerException e) {
            Log.e(TAG, "Failed to load meta-data, NullPointer: " + e.getMessage());
        }
        return null;
    }
}