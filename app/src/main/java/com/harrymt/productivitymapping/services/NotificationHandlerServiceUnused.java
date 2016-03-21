package com.harrymt.productivitymapping.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.service.notification.StatusBarNotification;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.harrymt.productivitymapping.PROJECT_GLOBALS;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NotificationHandlerServiceUnused extends Service {
    private static final String TAG = PROJECT_GLOBALS.LOG_NAME + "NHServiceUnused";

    // Setup broadcasters
    private IntentFilter notificationPostedIntentFilter = new IntentFilter(PROJECT_GLOBALS.Broadcasts.NOTIFICATION_POSTED);
    private IntentFilter appUsageIntentFilter = new IntentFilter(PROJECT_GLOBALS.Broadcasts.APP_USAGE);
    final private NotificationReceiver notificationReceiver = new NotificationReceiver();
    final private AppUsageReceiver appUsageReceiver = new AppUsageReceiver();

    private final IBinder notificationBinder = new NotificationBinder();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        // Register the notification broadcast
        LocalBroadcastManager.getInstance(this).registerReceiver(notificationReceiver, notificationPostedIntentFilter);
        // Register app usage broadcast
        LocalBroadcastManager.getInstance(this).registerReceiver(appUsageReceiver, appUsageIntentFilter);

        Log.d(TAG, "Registered the Notification posted and app usage broadcast recievers.");

        return super.onStartCommand(intent, flags, startId);
    }

    public ArrayList<StatusBarNotification> blocked_notifications = new ArrayList<>();


    private class NotificationReceiver extends BroadcastReceiver
    {
        public void onReceive(Context context, Intent intent)
        {
            StatusBarNotification notification = intent.getParcelableExtra("notification");
            blocked_notifications.add(notification);

            Log.d(TAG, "Received a notification package " + notification.getPackageName());
        }
    }

    private class AppUsageReceiver extends BroadcastReceiver
    {
        public void onReceive(Context context, Intent intent)
        {
            String appPackage = intent.getStringExtra("app_package");
            long appTimeSeconds = intent.getLongExtra("app_time_seconds", -1); // -1 is default


            if(used_apps.get(appPackage) != null) {
                // Append to existing value
                used_apps.put(appPackage, used_apps.get(appPackage) + appTimeSeconds);
            } else {
                // Create new entry
                used_apps.put(appPackage, appTimeSeconds);
            }

            Log.d(TAG, "Received app usage " + appPackage + ", " + appTimeSeconds);
        }
    }

    Map<String, Long> used_apps = new HashMap<String, Long>();

    @Override
    public IBinder onBind(Intent intent)
    {
        return notificationBinder;
    }

    public class NotificationBinder extends Binder {

        public Map<String, Long> getAllAppUsage()
        {
            return used_apps;
        }

        public ArrayList<StatusBarNotification> getBlockedNotifications()
        {
            return blocked_notifications;
        }

        public void resetBlockedNotifications()
        {
            blocked_notifications = null;
            blocked_notifications = new ArrayList<>();
        }

        public void resetAppUsage()
        {
            used_apps = null;
            used_apps = new HashMap<>();
        }
    }
}