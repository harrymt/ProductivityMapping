package com.harrymt.productivitymapping;

import android.app.Notification;
import android.content.Intent;
import android.service.notification.StatusBarNotification;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
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

            // Broadcast that we received a block notification
            Intent intent = new Intent(ProjectSettings.Broadcasts.NOTIFICATION_POSTED);
            intent.putExtra("notification", notification);
            LocalBroadcastManager.getInstance(CustomNotificationListener.this).sendBroadcast(intent);

            Log.d(TAG, "Blocked notification " + notification.getNotification().extras.getString("android.title"));
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.d(TAG, "Notification Removed");
    }

    // The parts of the notification we actually want to compare
    class NotificationParts {
        String title;
        String text;
        String bigText;
        String subText;
        String packageName;

        public NotificationParts(Notification n, String pack) {
            this.title = "";
            this.text = "";
            this.bigText = "";
            this.subText = "";
            this.packageName = "";
            CharSequence titleCS = n.extras.getCharSequence(Notification.EXTRA_TITLE); // e.g. Name of sender
            CharSequence textCS = n.extras.getCharSequence(Notification.EXTRA_TEXT);
            CharSequence bigTextCS = n.extras.getCharSequence(Notification.EXTRA_BIG_TEXT); // Content of email
            CharSequence subTextCS = n.extras.getCharSequence(Notification.EXTRA_SUB_TEXT); // Email address

            if (titleCS != null) this.title = titleCS.toString();
            if (textCS != null) this.text = textCS.toString();
            if (bigTextCS != null) this.bigText = bigTextCS.toString();
            if (subTextCS != null) this.subText = subTextCS.toString();

            this.packageName = pack;
        }

        public boolean containsPackage(String[] packagesToBlock) {
            for (String aPackagesToBlock : packagesToBlock) {
                if (aPackagesToBlock.equals(this.packageName)) {
                    return true;
                }
            }
            return false;
        }

        public boolean containsKeywords(String[] keywordsToBlock) {
            for (String aKeywordsToBlock : keywordsToBlock) {
                if (this.title.contains(aKeywordsToBlock) ||
                        this.bigText.contains(aKeywordsToBlock) ||
                        this.subText.contains(aKeywordsToBlock) ||
                        this.text.contains(aKeywordsToBlock)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Decides if we should block the notification.
     *
     * @param sbn Notificiation
     * @return True if we should block the notification, false if not.
     */
    public boolean shouldWeBlockThisNotification(StatusBarNotification sbn) {
        if (!ProjectSettings.STUDYING) {
            return false;
        }

        NotificationParts notification = new NotificationParts(sbn.getNotification(), sbn.getPackageName());

        // If a keyword matches, let it through
        if (notification.containsKeywords(ProjectSettings.KEYWORDS_TO_LET_THROUGH)) {
            return false;
        }

        // If it matches the package, block it
        return notification.containsPackage(ProjectSettings.PACKAGES_TO_BLOCK);
    }
}