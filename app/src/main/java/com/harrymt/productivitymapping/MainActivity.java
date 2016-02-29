package com.harrymt.productivitymapping;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;


import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends FragmentActivity implements ActionBar.TabListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

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


        mRequestingLocationUpdates = false;
        mLastUpdateTime = "";

        // Update values using data stored in the Bundle.
        updateValuesFromBundle(savedInstanceState);

        // Kick off the process of building a GoogleApiClient and requesting the LocationServices
        // API.
        buildGoogleApiClient();


        // START SERVICES
        intentNotificationHandlerService = new Intent(this, NotificationHandlerService.class);
        startService(intentNotificationHandlerService);
        bindService(intentNotificationHandlerService, notificationHandlerConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Connect to an Instance of the Google API
        mGoogleApiClient.connect();



        // Show notification listener settings if not set
        if (Settings.Secure.getString(this.getContentResolver(), "enabled_notification_listeners") != null &&
                Settings.Secure.getString(this.getContentResolver(), "enabled_notification_listeners").contains(getApplicationContext().getPackageName())) {
            //service is enabled do something
            if (ProjectStates.IS_DEBUG) Toast.makeText(MainActivity.this, "Can listen to notifications", Toast.LENGTH_SHORT).show();
        } else {
            //service is not enabled try to enabled by calling...
            startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
        }
    }


    @Override
    protected void onDestroy() {
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

    public void sendBlockedNotifications(View v) {
        ArrayList<StatusBarNotification> notifications = binder.getBlockedNotifications();
        Toast.makeText(MainActivity.this, "Sending " + notifications.size() + " notifications...", Toast.LENGTH_SHORT).show();
        for (StatusBarNotification sbn : notifications) {
            Log.d(TAG, sbn.toString());
            postStatusBarNotification(sbn);
            // This breaks it // postNewNotification(sbn.getNotification());
        }
    }

    // What we need todo, is copy over as much of the notification as we can, atm, we are only copying over title and content
    // Cant just use notification e.g. postNewNotifiation because we get Bad notification poster .. couldnt create icon
    private void postStatusBarNotification(StatusBarNotification sbn) {
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

    private final ServiceConnection notificationHandlerConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "Service " + name + " connected");
            binder = (NotificationHandlerService.NotificationBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
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


    private LatLng getCurrLocation() {
        if (mCurrentLocation != null) {
            return new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        } else {
            // TODO change this fallback
            return new LatLng(52.9532976, -1.187156);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Check which request we're responding to
        if (requestCode == REQUEST_CODE_SET_ZONE || requestCode == REQUEST_CODE_EDIT_ZONE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                Bundle b = data.getExtras();
                Zone z = b.getParcelable("zone");

                // Add the zone to a database.
                DatabaseAdapter dbAdapter;
                dbAdapter = new DatabaseAdapter(this); // Open and prepare the database
                dbAdapter.open();
                if (requestCode == REQUEST_CODE_EDIT_ZONE) {
                    dbAdapter.editZone(z);
                } else {
                    dbAdapter.writeZone(z);
                }
                dbAdapter.close();

                if (ProjectStates.IS_DEBUG) Toast.makeText(MainActivity.this, "Zone data: packages(" + z.blockingApps.toString() + "), keywords(" + z.keywords.toString() + "), r(" + z.radiusInMeters + "), LatLng(" + z.lat + "," + z.lng + ")", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // TODO get the current zone a user is in
    private Zone getCurrentZone() {
        return new Zone(52.9536037, -1.1890631);
    }

    public void startCurrentZone(View view) {
        // Enable study state
        ProjectStates.STUDYING = true;

        ProjectStates.CURRENT_ZONE = getCurrentZone();

        // Get the current Zone ID we are in!
        Integer zoneID = ProjectStates.CURRENT_ZONE.zoneID;
        long startTime = System.currentTimeMillis() / 1000; // get current EPOCH time

        // Start a new session
        DatabaseAdapter dbAdapter;
        dbAdapter = new DatabaseAdapter(this); // Prepare the database
        dbAdapter.open(); // Open it for writing (if this is the first time its called, ten
        dbAdapter.startNewSession(zoneID, startTime); // Start new session with this zone zone
        dbAdapter.close();

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
        ProjectStates.CURRENT_ZONE = null;

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


    /**
     *
     *
     *
     *
     *
     *
     * --- Get users location every few seconds stuff below ---
     *
     *
     *
     *
     *
     *
     **/

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    // Keys for storing activity state in the Bundle.
    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    protected final static String LOCATION_KEY = "location-key";
    protected final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    protected LocationRequest mLocationRequest;

    /**
     * Represents a geographical location.
     */
    public Location mCurrentLocation;

    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    protected Boolean mRequestingLocationUpdates = true;

    /**
     * Time when the location was updated represented as a String.
     */
    protected String mLastUpdateTime;


    /**
     * Updates fields based on data stored in the bundle.
     *
     * @param savedInstanceState The activity state saved in the Bundle.
     */
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        Log.i(TAG, "Updating values from bundle");
        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and make sure that
            // the Start Updates and Stop Updates buttons are correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        REQUESTING_LOCATION_UPDATES_KEY);

            }

            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that mCurrentLocation
                // is not null.
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
                mLastUpdateTime = savedInstanceState.getString(LAST_UPDATED_TIME_STRING_KEY);
            }
            updateUI();
        }
    }

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    /**
     * Requests location updates from the FusedLocationApi.
     */
    protected void startLocationUpdates() {
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission ERROR");
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }


    /**
     * Updates the latitude, the longitude, and the last location time in the UI.
     */
    private void updateUI() {
        if (ProjectStates.IS_DEBUG)  Toast.makeText(this, mLastUpdateTime + ". " + mCurrentLocation.getLatitude() + "," + mCurrentLocation.getLongitude(), Toast.LENGTH_SHORT).show();

    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.

        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }


    @Override
    public void onResume() {
        super.onResume();
        // Within {@code onPause()}, we pause location updates, but leave the
        // connection to GoogleApiClient intact.  Here, we resume receiving
        // location updates if the user has requested them.

        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();

        super.onStop();
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Connected to GoogleApiClient");

        // If the initial location was never previously requested, we use
        // FusedLocationApi.getLastLocation() to get it. If it was previously requested, we store
        // its value in the Bundle and check for it in onCreate(). We
        // do not request it again unless the user specifically requests location updates by pressing
        // the Start Updates button.
        //
        // Because we cache the value of the initial location in the Bundle, it means that if the
        // user launches the activity,
        // moves to a new location, and then changes the device orientation, the original location
        // is displayed as the activity is re-created.
        if (mCurrentLocation == null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permission ERROR");
                return;
            }
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            updateUI();
        }

        // If the user presses the Start Updates button before GoogleApiClient connects, we set
        // mRequestingLocationUpdates to true (see startUpdatesButtonHandler()). Here, we check
        // the value of mRequestingLocationUpdates and if it is true, we start location updates.
        //if (mRequestingLocationUpdates) {
            startLocationUpdates();
        //}
    }

    /**
     * Callback that fires when the location changes.
     */
    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateUI();
        if (ProjectStates.IS_DEBUG) Toast.makeText(this, getResources().getString(R.string.location_updated_message), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    /**
     * Stores activity data in the Bundle.
     */
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);
        super.onSaveInstanceState(savedInstanceState);
    }

    public void editCurrentZone(View view) {
        // start edit zone activity.
        // TODO get the current zone we are in and set it here
        Intent editZoneActivityIntent = new Intent(this, ZoneEditActivity.class);
        editZoneActivityIntent.putExtra("zone", new Zone(getCurrLocation().latitude, getCurrLocation().longitude));
        startActivityForResult(editZoneActivityIntent, REQUEST_CODE_SET_ZONE);
    }

    public void showLastSession(View view) {
        // start last session activity
        startActivity(new Intent(this, LastSession.class));
    }

    public void syncWithServer(View view) {
        Toast.makeText(this, "Syncing with server", Toast.LENGTH_SHORT).show();
        // TODO sync
        Toast.makeText(this, "Sync successful", Toast.LENGTH_SHORT).show();
    }

}