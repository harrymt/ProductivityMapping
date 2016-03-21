package com.harrymt.productivitymapping.activities;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import android.support.v4.app.FragmentActivity;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.harrymt.productivitymapping.API;
import com.harrymt.productivitymapping.ActionBarHandler;
import com.harrymt.productivitymapping.LocationPoller;
import com.harrymt.productivitymapping.fragments.StatFragment;
import com.harrymt.productivitymapping.fragments.TrackFragment;
import com.harrymt.productivitymapping.fragments.ZonesFragment;
import com.harrymt.productivitymapping.utility.NotificationBuilderUtil;
import com.harrymt.productivitymapping.utility.Util;
import com.harrymt.productivitymapping.database.DatabaseAdapter;
import com.harrymt.productivitymapping.PROJECT_GLOBALS;
import com.harrymt.productivitymapping.R;
import com.harrymt.productivitymapping.Zone;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends FragmentActivity {

    private static final String TAG = "g53ids-MainActivity";

    // Polls for the users location
    private LocationPoller locationPoller;
    // Access fragments TODO delete this? Make it local in oncreate
    public ActionBarHandler tabFragments;

    int REQUEST_CODE_SET_ZONE = 4;
    int REQUEST_CODE_EDIT_ZONE = 3;


// TODO delete me
    public void sendNotification(View v) {
        NotificationBuilderUtil b = new NotificationBuilderUtil(this);
        b.postNewNotification(b.buildNotification("Main App", "Sent from the main app", "Some subtext..."));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup Action Bar
        tabFragments = new ActionBarHandler();
        tabFragments.setup(this);

        // Setup Location Polling
        locationPoller = new LocationPoller(this, savedInstanceState);

        // Cache the most commonly used keywords and packages blocked
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(API.makeRequest(this, "/apps/3"));
    }

    @Override
    protected void onStart() {
        super.onStart();

        locationPoller.mGoogleApiClient.connect();

        // Show notification listener settings if not set
        if (Util.weCanListenToNotifications(this)) {
            // Service is enabled do something
            if (PROJECT_GLOBALS.IS_DEBUG) Toast.makeText(MainActivity.this, "Can listen to notifications", Toast.LENGTH_SHORT).show();
        } else {
            // Accessibility service is not enabled, try to get the user to enable it by showing the settings
            startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        locationPoller.startPolling();
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationPoller.stopPolling();
    }

    @Override
    protected void onStop() {
        locationPoller.mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Check which request we're responding to && successful
        if ((requestCode == REQUEST_CODE_SET_ZONE || requestCode == REQUEST_CODE_EDIT_ZONE) && resultCode == RESULT_OK) {
            Zone z = data.getExtras().getParcelable("zone");

            // Add the zone to a database.
            DatabaseAdapter dbAdapter = new DatabaseAdapter(this); // Open and prepare the database
            if (requestCode == REQUEST_CODE_EDIT_ZONE) {
                dbAdapter.editZone(z);
            } else {
                dbAdapter.writeZone(z);
            }
            dbAdapter.close();

            if (PROJECT_GLOBALS.IS_DEBUG)  {
                Toast.makeText(MainActivity.this,
                        "Zone data: packages(" + z.blockingAppsAsStr()
                                + "), keywords(" + z.keywordsAsStr()
                                + "), r(" + z.radiusInMeters
                                + "), LatLng(" + z.lat
                                + "," + z.lng
                                + ")", Toast.LENGTH_SHORT).show();
            }

            // Update all fragments
            TrackFragment tf = (TrackFragment) ActionBarHandler.pagerAdapter.getItem(0);
            ZonesFragment zf = (ZonesFragment) ActionBarHandler.pagerAdapter.getItem(1);
            StatFragment sf = (StatFragment) ActionBarHandler.pagerAdapter.getItem(2);
            tf.refresh();
            sf.refresh();
            zf.refresh();
        }
    }

    /**
     * Stores activity data in the Bundle.
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putAll(locationPoller.getBundleForSavedState());
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * OnClick btnCurrentZone
     *
     * Start a study session from the current zone.
     *
     * @param view Button: btnCurrentZone
     */
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

        // Enable the Last Session Button
        findViewById(R.id.btnLastSession).setEnabled(dbAdapter.hasASessionEverStartedYet());

        dbAdapter.close();

        // Reset service stored data e.g. app usage
        // TODO dont do this
        // binder.resetAppUsage();
        // binder.resetBlockedNotifications();

        changeUI(PROJECT_GLOBALS.STUDYING);
    }


    /**
     * OnClick btnForceStopStudy
     *
     * Stop a study session even if the user is inside of the zone.
     *
     * @param view Button: btnForceStopStudy
     */
    public void forceStopStudy(View view) {
        // Disable study state
        PROJECT_GLOBALS.STUDYING = false;

        // Save current end time to database
        DatabaseAdapter dbAdapter = new DatabaseAdapter(getApplicationContext()); // Open and prepare the database
        dbAdapter.finishSession(PROJECT_GLOBALS.SESSION_ID);
        dbAdapter.close();

        // Change the ui
        changeUI(PROJECT_GLOBALS.STUDYING);
    }

    /**
     * OnClick btnCurrentZone
     *
     * Create a new zone, bringing up the new zone creation activity.
     *
     * @param view Button: btnCurrentZone
     */
    public void createNewZone(View view) {
        // start set zone activity.
        Intent editZoneActivityIntent = new Intent(this, ZoneEditActivity.class);
        // Create a new Zone with default parameters
        editZoneActivityIntent.putExtra("zone", new Zone(locationPoller.getCurrLocation()));
        startActivityForResult(editZoneActivityIntent, REQUEST_CODE_SET_ZONE);
    }

    /**
     * OnClick btnEditZonePreferences
     *
     * Edit the current zone that the user is on.
     *
     * @param view Button: btnEditZonePreferences
     */
    public void editCurrentZone(View view) {
        // start edit zone activity.
        // TODO get the current zone we are in and set it here
        Intent editZoneActivityIntent = new Intent(this, ZoneEditActivity.class);
        editZoneActivityIntent.putExtra("zone", new Zone(locationPoller.getCurrLocation().latitude, locationPoller.getCurrLocation().longitude));
        startActivityForResult(editZoneActivityIntent, REQUEST_CODE_SET_ZONE);
    }

    /**
     * OnClick btnLastSession
     *
     * Show user their latest session by opening up the Activity.
     *
     * @param view Button: btnLastSession
     */
    public void showLastSession(View view) {
        // start last session activity
        startActivity(new Intent(this, LastSession.class));
    }

    /**
     * OnClick btnSync
     *
     * Send the zone data to the central server.
     *
     * @param view Button: btnSync
     */
    public void syncWithServer(View view) throws JSONException {

        DatabaseAdapter dbAdapter = new DatabaseAdapter(getApplicationContext()); // Open and prepare the database
        ArrayList<Zone> zones = dbAdapter.getAllZonesThatNeedToBeSynced();
        dbAdapter.close();
        Toast.makeText(getApplicationContext(), "Found " + zones.size() + " zones to sync", Toast.LENGTH_SHORT).show();

        RequestQueue queue = Volley.newRequestQueue(this);

        // Send all the zones!
        for (Zone z : zones) {
            JSONObject payload = z.getJSONObject(this);
            queue.add(API.makeRequestZone(this, "/zone/", payload, z.zoneID));
        }
    }

    /**
     * Update the UI to react to if a user is studying or not.
     *
     * @param studying True if studying, false if not.
     */
    private void changeUI(Boolean studying) {
        TextView study = (TextView) findViewById(R.id.tvStudyStateText);

        if(studying) {
            study.setText(R.string.track_study_state_studying);
        } else {
            study.setText(R.string.track_study_state_not_studying);
        }

        Button createNewZone = (Button) findViewById(R.id.btnCreateNewZone);
        Button currentZone = (Button) findViewById(R.id.btnCurrentZone);
        Button editZone = (Button) findViewById(R.id.btnEditZonePreferences);
        Button forceStopStudy = (Button) findViewById(R.id.btnForceStopStudy);

        createNewZone.setEnabled(!studying);
        currentZone.setEnabled(!studying);
        editZone.setEnabled(studying);
        forceStopStudy.setEnabled(studying);
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
}