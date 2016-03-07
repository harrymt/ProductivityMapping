package com.harrymt.productivitymapping;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by harrymt on 23/11/15.
 */
public class PROJECT_GLOBALS
{
    public static boolean STUDYING = false;

    public static String base_url = "http://horizab1.miniserver.com/~harry/server/ProductivityMapping-Server/api/v1";

    public static Integer SESSION_ID = 0;
    static boolean IS_DEBUG = false;

    static Zone CURRENT_ZONE;

    public static String apiKey(Context c) {
        String secret_key = getMetaData(c, "api_key");
        return "?apikey=" + secret_key;
    }

    private static String getMetaData(Context context, String resourceID) {
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

    class Broadcasts
    {
        static final String NOTIFICATION_POSTED = "NOTIFICATION_POSTED";
        static final String APP_USAGE = "APP_USAGE";
    }
}