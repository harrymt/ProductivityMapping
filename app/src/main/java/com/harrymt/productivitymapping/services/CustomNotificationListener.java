package com.harrymt.productivitymapping.services;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.harrymt.productivitymapping.NotificationParts;
import com.harrymt.productivitymapping.database.DatabaseAdapter;
import com.harrymt.productivitymapping.PROJECT_GLOBALS;

/**
 *
 * Listens for notifications using the notification service.
 *
 */
public class CustomNotificationListener extends NotificationListenerService {
    private static final String TAG = "NotificationListener";

    @Override
    public void onNotificationPosted(StatusBarNotification notification) {
        Log.d(TAG, "Notification Found: " + notification.getNotification().extras.getString("android.title"));

        if (shouldWeBlockThisNotification(notification)) {

            // Block notification from being posted to the phone
            cancelNotification(notification.getKey());

            // Save notification
            saveNotification(notification);

            Log.d(TAG, "Blocked notification " + notification.getNotification().extras.getString("android.title"));
        }
    }

    /**
     * Decides if we should block the notification.
     *
     * @param sbn notification
     * @return True if we should block the notification, false if not.
     */
    public boolean shouldWeBlockThisNotification(StatusBarNotification sbn) {
        if (!PROJECT_GLOBALS.STUDYING) {
            return false;
        }

        NotificationParts notification = new NotificationParts(sbn.getNotification(), sbn.getPackageName());

        // If a keyword matches, let it through
        if (notification.containsKeywords(PROJECT_GLOBALS.CURRENT_ZONE.keywords)) {
            // TODO keep track of # notifications received but not blocked based on keywords
            return false;
        }

        // If it matches the package, block it
        return notification.containsPackage(PROJECT_GLOBALS.CURRENT_ZONE.blockingApps);
    }

    /**
     * Save the notification to the database.
     *
     * @param n notification to save.
     */
    private void saveNotification(StatusBarNotification n) {
        DatabaseAdapter dbAdapter = new DatabaseAdapter(this); // Open and prepare the database
        dbAdapter.writeNotification(n);
        dbAdapter.close();
    }
}