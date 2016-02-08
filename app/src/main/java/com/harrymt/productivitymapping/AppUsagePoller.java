package com.harrymt.productivitymapping;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;


/**
 *
 * USEFUL LINKS:
 * ---
 * Start the Settings Activity, so they can toggle this
 * Home Screen > System Settings > Accessibility > Accessibility Services > Toast2 > Change from Off to On,
 *
 *
 * Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
 * startActivityForResult(intent, 0);
 *
 *
 */

public class AppUsagePoller extends AccessibilityService
{
    public static String TAG = "g53ids";

    private String packageLastOpened = "";
    private long timeLastOpened = 0;

    /**
     * This method is a part of the {@link AccessibilityService} lifecycle and is
     * called after the system has successfully bound to the service. If is
     * convenient to use this method for setting the {@link AccessibilityServiceInfo}.
     *
     * @see AccessibilityServiceInfo
     * @see #setServiceInfo(AccessibilityServiceInfo)
     */
    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "onServiceConnected");
        Toast.makeText(getApplicationContext(), "Starting service", Toast.LENGTH_SHORT).show();
        packageLastOpened = "com.started.service"; // Set initial package so we know
        timeLastOpened = System.nanoTime() / 1000000; // Set now as the time
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent newEvent)
    {
        if(newEvent.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)
        {
            // Calculate time spent in app/area of phone, e.g. lock screen, app drawer
            long timeSpentInAppMilliseconds = (newEvent.getEventTime() - timeLastOpened);
            long timeSpentInSeconds = timeSpentInAppMilliseconds / 1000;

            // Broadcast app usage
            Intent intent = new Intent(ProjectSettings.Broadcasts.APP_USAGE);
            intent.putExtra("app_package", packageLastOpened);
            intent.putExtra("app_time_seconds", timeSpentInSeconds);
            LocalBroadcastManager.getInstance(AppUsagePoller.this).sendBroadcast(intent);

            Log.d(TAG, "Time spent app (" + packageLastOpened + "): " + timeSpentInSeconds + " seconds");

            // Set new values for current app
            timeLastOpened = newEvent.getEventTime();
            packageLastOpened = newEvent.getPackageName() + "";
        }
    }

    /**
     * Callback for interrupting the accessibility feedback.
     */
    @Override
    public void onInterrupt()
    {
        Log.d(TAG, "onInterrupt");
        Toast.makeText(getApplicationContext(), "onInterrupt", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        Log.d(TAG, "onUnbind of service");
        return super.onUnbind(intent);
    }
}