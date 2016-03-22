package com.harrymt.productivitymapping;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Button;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.harrymt.productivitymapping.coredata.Zone;
import com.harrymt.productivitymapping.database.DatabaseAdapter;

/**
 * Handles the polling for the users current location using the Google API.
 *
 * Based on the google demo class:
 * https://github.com/googlesamples/android-play-location/blob/master/LocationUpdates/app/src/main/java/com/google/android/gms/location/sample/locationupdates/MainActivity.java
 */
public class LocationPoller implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
    private static final String TAG = PROJECT_GLOBALS.LOG_NAME + "LocationPoller";

    // The desired interval for location updates. Inexact. Updates may be more or less frequent.
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;


    // The fastest rate for active location updates. Exact. Updates will never be more frequent than this value.
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    // Keys for storing activity state in the Bundle.
    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    protected final static String LOCATION_KEY = "location-key";

    // Provides the entry point to Google Play services.
    public GoogleApiClient mGoogleApiClient;

    // Stores parameters for requests to the FusedLocationProviderApi.
    protected LocationRequest mLocationRequest;

    // Represents the users current location.
    public Location mCurrentLocation;

    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    protected Boolean mRequestingLocationUpdates = true;

    // Reference to the activity.
    private FragmentActivity activityReference;

    /**
     * Constructor.
     *
     * @param ref Activity reference.
     * @param state Saved state.
     */
    public LocationPoller(FragmentActivity ref, Bundle state) {
        activityReference = ref;

        mRequestingLocationUpdates = false;

        // Update values using data stored in the Bundle.
        updateValuesFromBundle(state);

        // Building a GoogleApiClient and requesting the LocationServices API
        buildGoogleApiClient();
    }

    /**
     * Runs when Google API client successfully connects.
     * Start polling for location.
     *
     * @param connectionHint Description of connection.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Connected to GoogleApiClient");

        if (mCurrentLocation == null) {
            if (ActivityCompat.checkSelfPermission(activityReference, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(activityReference, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Permission ERROR");
                return;
            }
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            updateUI();
        }

        startLocationUpdates();
    }

    /**
     * Callback that fires when location changes.
     * @param location New location.
     */
    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        updateUI();
    }

    /**
     * Callback is called when connection is suspended.
     * Try to connect again.
     *
     * @param cause Cause of suspension.
     */
    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "onConnectionSuspended, cause: " + cause);
        mGoogleApiClient.connect();
    }

    /**
     * When connection fails.
     *
     * @param result Result of failure.
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(activityReference)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

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

            // Update the value of mCurrentLocation from the Bundle and update the ActionBarHandler to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that mCurrentLocation
                // is not null.
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }

            updateUI();
        }
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
        if (ActivityCompat.checkSelfPermission(activityReference, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(activityReference, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission ERROR");
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }
    /**
     * Removes location updates from the FusedLocationApi.
     */
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    /**
     * Starts polling for location updates.
     */
    public void startPolling() {
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    /**
     * Stop polling for location updates.
     */
    public void stopPolling() {
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    /**
     * Get the users current location.
     *
     * @return Users location in lat lng.
     */
    public LatLng getCurrLocation() {
        if (mCurrentLocation != null) {
            return new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        } else {
            // Return a random fallback as a location
            return new LatLng(52.9532976, -1.187156);
        }
    }

    /**
     * Checks to see if a user is inside of a zone or not!
     */
    private void setUsersCurrentZone() {
        DatabaseAdapter dbAdapter;
        dbAdapter = new DatabaseAdapter(activityReference); // Open and prepare the database
        Zone zone = dbAdapter.getZoneInLocation(mCurrentLocation);
        dbAdapter.close();

        // Update current zone, could be null if no zone is found;
        PROJECT_GLOBALS.CURRENT_ZONE = zone;
    }

    /**
     * Updates the latitude, the longitude, and the last location time in the ActionBarHandler.
     */
    private void updateUI() {
        if(!PROJECT_GLOBALS.STUDYING) {
            setUsersCurrentZone();

            Button currentZone = (Button) activityReference.findViewById(R.id.btnCurrentZone);

            // Set the current zone button to be enabled if we are in a current zone
            currentZone.setEnabled(PROJECT_GLOBALS.CURRENT_ZONE != null);
        }
    }

    /**
     * Create a bundle for saved state.
     *
     * @return Bundle with info needing to be saved.
     */
    public Bundle getBundleForSavedState() {
        Bundle state = new Bundle();
        state.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        state.putParcelable(LOCATION_KEY, mCurrentLocation);
        return state;
    }
}