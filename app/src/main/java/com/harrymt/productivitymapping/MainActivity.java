package com.harrymt.productivitymapping;

import android.Manifest;
import android.app.Activity;
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
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

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

        // UNBIND SERVICE ON DESTROY
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

//    public void listBlockedNotifications(View v)
//    {
//        ArrayList<StatusBarNotification> notifications = binder.getBlockedNotifications();
//
//        // Should use a custom adapter, but we are just gonna make another array for now
//        ArrayList<String> notificationDescriptions = new ArrayList<>();
//        for (StatusBarNotification n : notifications) {
//            notificationDescriptions.add(n.getNotification().extras.getString("android.title") + " - " + n.getPackageName());
//        }
//
//        lv = (ListView) findViewById(R.id.listView);
//        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
//                this,
//                android.R.layout.simple_list_item_1,
//                notificationDescriptions );
//
//        lv.setAdapter(arrayAdapter);
//    }

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

//
//    public void showAppUsage(View view)
//    {
//
//        ArrayList<String> appDescriptions = new ArrayList<>();
//        Map<String, Long> apps = binder.getAllAppUsage();
//        apps = MapUtil.sortByValue(apps);
//        for (Map.Entry<String, Long> entry : apps.entrySet()) {
//            appDescriptions.add(entry.getKey() + " - " + entry.getValue() + "s");
//        }
//
//        lv = (ListView) findViewById(R.id.listView);
//        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
//                this,
//                android.R.layout.simple_list_item_1,
//                appDescriptions );
//
//        lv.setAdapter(arrayAdapter);
//
//        Log.d(TAG, "Showing app usage for " + apps.size());
//    }


    int REQUEST_CODE_SET_ZONE = 4;
    int REQUEST_CODE_EDIT_ZONE = 3;

    /**
     * On 'New Zone' click.
     * @param view
     */
    public void createNewZone(View view) {
        // start set zone activity.
        Intent editZoneActivityIntent = new Intent(this, ZoneEditActivity.class);
        // Create a new Zone with default parameters
        editZoneActivityIntent.putExtra("zone", new Zone(getCurrLocation().latitude, getCurrLocation().longitude));
        startActivityForResult(editZoneActivityIntent, REQUEST_CODE_SET_ZONE);
    }

    // TODO get current location
    private LatLng getCurrLocation() {
        return new LatLng(52.9532976, -1.187156);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Check which request we're responding to
        if (requestCode == REQUEST_CODE_SET_ZONE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                Bundle b = data.getExtras();
                Zone z = b.getParcelable("zone");

                // Add the zone to a database.
                DatabaseAdapter dbAdapter;
                dbAdapter = new DatabaseAdapter(this); // Open and prepare the database
                dbAdapter.open();
                dbAdapter.writeZone(z);
                dbAdapter.close();

                Toast.makeText(MainActivity.this, "Zone data: packages(" + z.blockingApps.toString() + "), keywords(" + z.keywords.toString() + "), r(" + z.radiusInMeters + "), LatLng(" + z.lat + "," + z.lng + ")", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_CODE_EDIT_ZONE) {
            // Make sure the request was successful
            if (resultCode == Activity.RESULT_OK) {
                Bundle b = data.getExtras();
                Zone z = b.getParcelable("zone");

                // Add the zone to a database.
                DatabaseAdapter dbAdapter;
                dbAdapter = new DatabaseAdapter(this); // Open and prepare the database
                dbAdapter.open();
                dbAdapter.editZone(z);
                dbAdapter.close();
            }
        }
    }


    private String[] getPackages() {
        return new String[]{"com.harrymt.sendnotification", "com.google.android.gm"};
    }

    private String[] getKeywords() {
        return new String[]{"IMPORTANT", "FAMILY"};
    }

    public void startCurrentZone(View view) {
        // Enable study state
        ProjectStates.STUDYING = true;

        // Get the current Zone ID we are in!
        Integer zoneID = 1; // getZoneID()
        long startTime = System.currentTimeMillis() / 1000;// get current EPOCH time

        // Start a new session
        DatabaseAdapter dbAdapter;
        dbAdapter = new DatabaseAdapter(this); // Prepare the database
        dbAdapter.open(); // Open it for writing (if this is the first time its called, ten
        dbAdapter.startNewSession(zoneID, startTime); // Start new session with this zone zone
        dbAdapter.close();

        // Assign text view values to project settings
        ProjectStates.KEYWORDS_TO_LET_THROUGH = getKeywords();
        ProjectStates.PACKAGES_TO_BLOCK = getPackages();

        // Reset service stored data e.g. app usage
        // TODO dont do this
        binder.resetAppUsage();
        binder.resetBlockedNotifications();

        // Set UI
        TextView study = (TextView) findViewById(R.id.tvStudyStateText);
        study.setText("Studying...");

        Button createNewZone = (Button) findViewById(R.id.btnCreateNewZone);
        createNewZone.setEnabled(false);
        Button currentZone = (Button) findViewById(R.id.btnCurrentZone);
        currentZone.setEnabled(false);

        Button editZone = (Button) findViewById(R.id.btnEditZonePreferences);
        editZone.setEnabled(true);
        Button forceStopStudy = (Button) findViewById(R.id.btnForceStopStudy);
        forceStopStudy.setEnabled(true);
    }

    public void forceStopStudy(View view) {
        // Disable study state
        ProjectStates.STUDYING = false;

        // Reset settings
        ProjectStates.KEYWORDS_TO_LET_THROUGH = null;
        ProjectStates.PACKAGES_TO_BLOCK = null;

        // Store these!! TODO store me
        binder.getAllAppUsage();
        binder.getBlockedNotifications();

        TextView study = (TextView) findViewById(R.id.tvStudyStateText);
        study.setText("Start study with...");

        Button createNewZone = (Button) findViewById(R.id.btnCreateNewZone);
        createNewZone.setEnabled(true);
        Button currentZone = (Button) findViewById(R.id.btnCurrentZone);
        currentZone.setEnabled(true);

        Button editZone = (Button) findViewById(R.id.btnEditZonePreferences);
        editZone.setEnabled(false);
        Button forceStopStudy = (Button) findViewById(R.id.btnForceStopStudy);
        forceStopStudy.setEnabled(false);
    }

}