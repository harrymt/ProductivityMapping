package com.harrymt.productivitymapping;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

//import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.api.GoogleApiClient;
//import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Map;
import java.util.*;

public class MainActivity extends Activity {// implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener  {
    private static final String TAG = "g53ids-MainActivity";

    // Notification Handler Service intent
    private Intent intentNotificationHandlerService;
    private NotificationHandlerService.NotificationBinder binder;


//    protected synchronized void buildGoogleApiClient() {
//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
//                .addApiIfAvailable(LocationServices.API)
//                .build();
//
//        mGoogleApiClient.connect();
    //   }
//    private GoogleApiClient mGoogleApiClient;
//
//    private Location mLastLocation;
//
//    @Override
//    public void onConnected(Bundle bundle)
//    {
//        Log.d(TAG, "Google API Connected");
//
//        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//        if (mLastLocation != null)
//        {
//            Toast.makeText(MainActivity.this, "Location lat,long " + mLastLocation.getLatitude() + ", " + mLastLocation.getLongitude(), Toast.LENGTH_SHORT).show();
//        }
//    }

//    @Override
//    public void onConnectionSuspended(int i)
//    {
//        Log.d(TAG, "Google API Connection suspended");
//    }
//
//
//    @Override
//    public void onConnectionFailed(ConnectionResult connectionResult)
//    {
//        Log.d(TAG, "Google API Connection failed");
//    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        intentNotificationHandlerService = new Intent(this, NotificationHandlerService.class);
        startService(intentNotificationHandlerService);
        bindService(intentNotificationHandlerService, notificationHandlerConnection, Context.BIND_AUTO_CREATE);

        //buildGoogleApiClient(); // Setup the google APIs
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        // Show notification listener settings if not set
        if (Settings.Secure.getString(this.getContentResolver(), "enabled_notification_listeners") != null &&
                Settings.Secure.getString(this.getContentResolver(),"enabled_notification_listeners").contains(getApplicationContext().getPackageName()))
        {
            //service is enabled do something
            Toast.makeText(MainActivity.this, "Can listen to notifications", Toast.LENGTH_SHORT).show();
        } else {
            //service is not enabled try to enabled by calling...
            startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
        }

    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        unbindService(this.notificationHandlerConnection);
    }

    public void sendNotification(View v) {
        Notification n = buildNotification("Main App", "Sent from the main app", "Some subtext...");
        postNewNotification(n);
        Log.d(TAG, "Notification sent.");
    }

    private ListView lv;

    public void listBlockedNotifications(View v)
    {
        ArrayList<StatusBarNotification> notifications = binder.getBlockedNotifications();

        // Should use a custom adapter, but we are just gonna make another array for now
        ArrayList<String> notificationDescriptions = new ArrayList<>();
        for (StatusBarNotification n : notifications) {
            notificationDescriptions.add(n.getNotification().extras.getString("android.title") + " - " + n.getPackageName());
        }

        lv = (ListView) findViewById(R.id.listView);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                notificationDescriptions );

        lv.setAdapter(arrayAdapter);
    }

    public void sendBlockedNotifications(View v)
    {
        ArrayList<StatusBarNotification> notifications = binder.getBlockedNotifications();
        Toast.makeText(MainActivity.this, "Sending " + notifications.size() + " notifications...", Toast.LENGTH_SHORT).show();
        for (StatusBarNotification sbn : notifications)
        {
            Log.d(TAG, sbn.toString());
            postStatusBarNotification(sbn);
            // This breaks it // postNewNotification(sbn.getNotification());
        }
    }

    // What we need todo, is copy over as much of the notification as we can, atm, we are only copying over title and content
    // Cant just use notification e.g. postNewNotifiation because we get Bad notification poster .. couldnt create icon
    private void postStatusBarNotification(StatusBarNotification sbn)
    {
        Notification sbnNotification = sbn.getNotification();

        Notification n = new Notification.Builder(MainActivity.this)
                .setWhen(sbnNotification.when)
                .setContentIntent(sbnNotification.contentIntent)
                .setSubText(sbnNotification.extras.getCharSequence(Notification.EXTRA_SUB_TEXT))
                .setColor(sbnNotification.color)
                .setCategory(sbnNotification.category)
                .setContent(sbnNotification.contentView)
                .setContentInfo(sbnNotification.extras.getCharSequence(Notification.EXTRA_INFO_TEXT))
                .setContentTitle(sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TITLE))
                .setContentText(sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TEXT))
                .setSmallIcon(R.drawable.ic_standard_notification).build();

        NotificationManager nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nManager.notify((int) System.nanoTime(), n);
    }

    public Notification buildNotification(String title, String text, String subText) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder.setContentTitle(title);
        notificationBuilder.setContentText(text);
        notificationBuilder.setSubText(subText);
        notificationBuilder.setSmallIcon(R.drawable.ic_standard_notification);
        notificationBuilder.setAutoCancel(true);
        return notificationBuilder.build();
    }

    public void postNewNotification(Notification notification) {
        NotificationManager nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nManager.notify((int) System.currentTimeMillis(), notification);
    }

    private final ServiceConnection notificationHandlerConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            Log.d(TAG, "Service " + name + " connected");
            binder = (NotificationHandlerService.NotificationBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            Log.d(TAG, "Service " + name + " disconnected.");
            binder = null;
        }
    };

    public void startStudy(View view)
    {
        EditText keywords = (EditText) findViewById(R.id.etKeywords);
        EditText packages = (EditText) findViewById(R.id.etPackage);

        if(ProjectSettings.STUDYING) {
            // Stop study
            Button btnStudy = (Button) view;
            btnStudy.setText("Start study");

            // Re-Enable Text views and buttons
            findViewById(R.id.btnSendNotifications).setEnabled(true);
            findViewById(R.id.btnListBlockedNotifications).setEnabled(true);
            findViewById(R.id.etPackage).setEnabled(true);
            findViewById(R.id.etKeywords).setEnabled(true);




            ProjectSettings.STUDYING = false;
        }
        else
        {
            // Reset service stored data e.g. app usage
            binder.resetAppUsage();
            binder.resetBlockedNotifications();

            // Start study!
            Button btnStudy = (Button) view;
            btnStudy.setText("Stop study");

            // Assign text view values to project settings
            String words = keywords.getText().toString();
            ProjectSettings.KEYWORDS_TO_LET_THROUGH = words.split(",");

            String pack = packages.getText().toString();
            ProjectSettings.PACKAGES_TO_BLOCK = pack.split(",");

            // Disable Text views and buttons
            findViewById(R.id.btnSendNotifications).setEnabled(false);
            findViewById(R.id.btnListBlockedNotifications).setEnabled(false);
            findViewById(R.id.etPackage).setEnabled(false);
            findViewById(R.id.etKeywords).setEnabled(false);


            ProjectSettings.STUDYING = true;
        }

    }

    public void showAppUsage(View view)
    {

        ArrayList<String> appDescriptions = new ArrayList<>();
        Map<String, Long> apps = binder.getAllAppUsage();
        apps = MapUtil.sortByValue(apps);
        for (Map.Entry<String, Long> entry : apps.entrySet()) {
            appDescriptions.add(entry.getKey() + " - " + entry.getValue() + "s");
        }

        lv = (ListView) findViewById(R.id.listView);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                appDescriptions );

        lv.setAdapter(arrayAdapter);

        Log.d(TAG, "Showing app usage for " + apps.size());
    }

    public void displayAppLocation(View view)
    {

//        // Get location
//        mLastLocation.getLongitude();
//        mLastLocation.getLatitude();
//
//        // Toast it
//        Toast.makeText(MainActivity.this, "Lat Lon " + mLastLocation.getLatitude() + ", " + mLastLocation.getLongitude(), Toast.LENGTH_SHORT).show();

    }


    // Sort Hashmaps
    public static class MapUtil
    {
        public static <K, V extends Comparable<? super V>> Map<K, V>
        sortByValue( Map<K, V> map )
        {
            List<Map.Entry<K, V>> list =
                    new LinkedList<Map.Entry<K, V>>( map.entrySet() );
            Collections.sort( list, new Comparator<Map.Entry<K, V>>()
            {
                public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
                {
                    return (o1.getValue()).compareTo( o2.getValue() );
                }
            } );

            Map<K, V> result = new LinkedHashMap<K, V>();
            for (Map.Entry<K, V> entry : list)
            {
                result.put( entry.getKey(), entry.getValue() );
            }
            return result;
        }
    }
}
