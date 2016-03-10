package com.harrymt.productivitymapping.services;

import android.content.Intent;
import android.service.notification.StatusBarNotification;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.harrymt.productivitymapping.NotificationParts;
import com.harrymt.productivitymapping.database.DatabaseAdapter;
import com.harrymt.productivitymapping.PROJECT_GLOBALS;

/**
 *
 * Listens for notifications using the notification service.
 *
 */
public class CustomNotificationListener extends android.service.notification.NotificationListenerService {
    private static final String TAG = "NotificationListener";

    @Override
    public void onNotificationPosted(StatusBarNotification notification) {
        Log.d(TAG, "onNotificationPosted() " + notification.getNotification().extras.toString());

        if (shouldWeBlockThisNotification(notification)) {

            // Block notification from being posted to the phone
            cancelNotification(notification.getKey());


            saveNotification(notification);

//            // Save app usage to database
//            DatabaseAdapter dbAdapter2;
//            dbAdapter2 = new DatabaseAdapter(this);
//            dbAdapter2.open(); // Open and prepare the database, first time call means you create db
//            dbAdapter2.writeAppUsage("test", 1);
//            dbAdapter2.close();

            // Dont actually *NEED* to broadcast the notification posted!
            // Broadcast that we received a block notification
            Intent intent = new Intent(PROJECT_GLOBALS.Broadcasts.NOTIFICATION_POSTED);
            intent.putExtra("notification", notification);
            LocalBroadcastManager.getInstance(CustomNotificationListener.this).sendBroadcast(intent);

            Log.d(TAG, "Blocked notification " + notification.getNotification().extras.getString("android.title"));
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.d(TAG, "Notification Removed");
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
     * @param n
     */
    private void saveNotification(StatusBarNotification n) {
        DatabaseAdapter dbAdapter;
        dbAdapter = new DatabaseAdapter(this); // Open and prepare the database
        dbAdapter.writeNotification(n);
        dbAdapter.close();
    }
}