package com.harrymt.productivitymapping;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
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

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import java.util.ArrayList;
import java.util.Map;
import java.util.*;

public class MainActivity extends FragmentActivity implements ActionBar.TabListener {
    private static final String TAG = "g53ids-MainActivity";


    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the
     * three primary sections of the app. We use a {@link android.support.v4.app.FragmentPagerAdapter}
     * derivative, which will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    AppSectionsPagerAdapter mAppSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will display the three primary sections of the app, one at a
     * time.
     */
    ViewPager mViewPager;


    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        // Create the adapter that will return a fragment for each of the three primary sections
        // of the app.
        mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();

        // Specify that the Home/Up button should not be enabled, since there is no hierarchical
        // parent.
        actionBar.setHomeButtonEnabled(false);

        // Specify that we will be displaying tabs in the action bar.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Set up the ViewPager, attaching the adapter and setting up a listener for when the
        // user swipes between sections.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mAppSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // When swiping between different app sections, select the corresponding tab.
                // We can also use ActionBar.Tab#select() to do this if we have a reference to the
                // Tab.
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mAppSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by the adapter.
            // Also specify this Activity object, which implements the TabListener interface, as the
            // listener for when this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mAppSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }





        // START SERVICES
        intentNotificationHandlerService = new Intent(this, NotificationHandlerService.class);
        startService(intentNotificationHandlerService);
        bindService(intentNotificationHandlerService, notificationHandlerConnection, Context.BIND_AUTO_CREATE);
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
        // UNBIND SERVICE ON DESTORY
        unbindService(this.notificationHandlerConnection);
    }






    // Notification Handler Service intent
    private Intent intentNotificationHandlerService;
    private NotificationHandlerService.NotificationBinder binder;


    /* ---- NON-DESIGN CODE below ----- */

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