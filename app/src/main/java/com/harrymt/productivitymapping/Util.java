package com.harrymt.productivitymapping;

import android.content.Context;
import android.provider.Settings;

/**
 * Created by harrymt on 20/03/16.
 */
public class Util {

    public static boolean weCanListenToNotifications(Context c) {
        String listOfEnabledNotificationListeners = Settings.Secure.getString(c.getContentResolver(), "enabled_notification_listeners");
        String ourNotificationListener = c.getApplicationContext().getPackageName();
        return listOfEnabledNotificationListeners != null &&
                listOfEnabledNotificationListeners.contains(ourNotificationListener);
    }

}
