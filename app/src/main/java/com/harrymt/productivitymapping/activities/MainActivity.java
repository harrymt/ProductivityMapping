package com.harrymt.productivitymapping.activities;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.harrymt.productivitymapping.NotificationBuilderUtil;
import com.harrymt.productivitymapping.fragments.AppSectionsPagerAdapter;
import com.harrymt.productivitymapping.database.DatabaseAdapter;
import com.harrymt.productivitymapping.PROJECT_GLOBALS;
import com.harrymt.productivitymapping.R;
import com.harrymt.productivitymapping.Zone;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;

public class MainActivity extends FragmentActivity implements ActionBar.TabListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private static final String TAG = "g53ids-MainActivity";

// TODO delete me
    public void sendNotification(View v) {
        NotificationBuilderUtil b = new NotificationBuilderUtil(this);
        b.postNewNotification(b.buildNotification("Main App", "Sent from the main app", "Some subtext..."));
    }


    public void startCurrentZone(View view) {
        // Enable study state
        PROJECT_GLOBALS.STUDYING = true;

        // Get the current Zone ID we are in!
        Integer zoneID = PROJECT_GLOBALS.CURRENT_ZONE.zoneID;
        long startTime = System.currentTimeMillis() / 1000; // get current EPOCH time

        // Start a new session
        DatabaseAdapter dbAdapter;
        dbAdapter = new DatabaseAdapter(this); // Prepare the database
        dbAdapter.startNewSession(zoneID, startTime); // Start new session with this zone
        dbAdapter.close();

        // Reset service stored data e.g. app usage
        // TODO dont do this
        // binder.resetAppUsage();
        // binder.resetBlockedNotifications();

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
        PROJECT_GLOBALS.STUDYING = false;

        // Save current end time to database
        DatabaseAdapter dbAdapter;
        dbAdapter = new DatabaseAdapter(getApplicationContext()); // Open and prepare the database
        dbAdapter.finishSession(PROJECT_GLOBALS.SESSION_ID);
        dbAdapter.close();

        // Update UI
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


        // Cache the most commonly used keywords and packages blocked
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(makeAPIRequest("/apps/3"));
    }

    private JsonObjectRequest makeAPIRequest(String endpoint) {

        String url = PROJECT_GLOBALS.base_url(this) + endpoint + PROJECT_GLOBALS.apiKey(this);

        return new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                JSONObject response_value;
                try {
                    response_value = (JSONObject) response.get("response");

                    PROJECT_GLOBALS.TOP_APPS_BLOCKED = new ArrayList<>();
                    for (Iterator<String> iter = response_value.keys(); iter.hasNext(); ) {
                        // Get the name of the top apps blocked
                        PROJECT_GLOBALS.TOP_APPS_BLOCKED.add(iter.next());
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d(TAG, "JSON exception with stats: " + e.getMessage());
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Getting stats internet error: " + error.getMessage());
            }
        });
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
            if (PROJECT_GLOBALS.IS_DEBUG) Toast.makeText(MainActivity.this, "Can listen to notifications", Toast.LENGTH_SHORT).show();
        } else {
            // Accessibility service is not enabled, try to get the user to enable it by showing the settings
            startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
        }
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
                if (requestCode == REQUEST_CODE_EDIT_ZONE) {
                    dbAdapter.editZone(z);
                } else {
                    dbAdapter.writeZone(z);
                }
                dbAdapter.close();

                if (PROJECT_GLOBALS.IS_DEBUG) Toast.makeText(MainActivity.this, "Zone data: packages(" + z.blockingApps.toString() + "), keywords(" + z.keywords.toString() + "), r(" + z.radiusInMeters + "), LatLng(" + z.lat + "," + z.lng + ")", Toast.LENGTH_SHORT).show();
            }
        }
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
        if (PROJECT_GLOBALS.IS_DEBUG)  Toast.makeText(this, mLastUpdateTime + ". " + mCurrentLocation.getLatitude() + "," + mCurrentLocation.getLongitude(), Toast.LENGTH_SHORT).show();

       setUsersCurrentZone(mCurrentLocation);

        if(!PROJECT_GLOBALS.STUDYING) {
            Button currentZone = (Button) findViewById(R.id.btnCurrentZone);

            // Set the current zone button to be enabled if we are in a current zone
            currentZone.setEnabled(PROJECT_GLOBALS.CURRENT_ZONE != null);
        }
    }

    /**
     * Checks to see if a user is inside of a zone or not!
     *
     * @param loc users current location.
     */
    private void setUsersCurrentZone(Location loc) {

        DatabaseAdapter dbAdapter;
        dbAdapter = new DatabaseAdapter(getApplicationContext()); // Open and prepare the database
        Zone zone = dbAdapter.getZoneInLocation(loc);
        dbAdapter.close();

        // Update current zone, could be null if no zone is found;
        PROJECT_GLOBALS.CURRENT_ZONE = zone;
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
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
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

    public void syncWithServer(View view) throws JSONException {

        DatabaseAdapter dbAdapter;
        dbAdapter = new DatabaseAdapter(getApplicationContext()); // Open and prepare the database
        ArrayList<Zone> zones = dbAdapter.getAllZonesThatNeedToBeSynced();
        dbAdapter.close();
        Toast.makeText(getApplicationContext(), "Found " + zones.size() + " zones to sync!", Toast.LENGTH_SHORT).show();

        RequestQueue queue = Volley.newRequestQueue(this);

        // Send all the zones!
        for (Zone z : zones) {
            JSONObject payload = new JSONObject();
            payload.put("user_id", PROJECT_GLOBALS.getUniqueDeviceId(this));
            payload.put("id", z.zoneID);
            payload.put("name", z.name);
            payload.put("lat", z.lat);
            payload.put("lng", z.lng);
            payload.put("radius", z.radiusInMeters);
            payload.put("blockingApps", new JSONArray(new ArrayList<>(Arrays.asList(z.blockingApps))));
            payload.put("keywords", new JSONArray(new ArrayList<>(Arrays.asList(z.keywords))));

            queue.add(makeJSONRequest(PROJECT_GLOBALS.base_url(this) + "/zone/" + PROJECT_GLOBALS.apiKey(this), payload, z.zoneID));
        }
    }

    private JsonObjectRequest makeJSONRequest(String url, final JSONObject payload, final int zone_id) {
        return new JsonObjectRequest(Request.Method.POST, url, payload, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                String friendlyResponse = "";

                try {
                    friendlyResponse = response.get("response").toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Toast.makeText(getApplicationContext(), "Sync successful " + friendlyResponse, Toast.LENGTH_SHORT).show();

                // Mark that zone as sync'd
                DatabaseAdapter dbAdapter;
                dbAdapter = new DatabaseAdapter(getApplicationContext()); // Open and prepare the database
                dbAdapter.setZoneAsSynced(zone_id);
                dbAdapter.close();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Failed to sync " + error.networkResponse.statusCode , Toast.LENGTH_SHORT).show();
            }
        });
    }
}