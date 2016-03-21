package com.harrymt.productivitymapping.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.harrymt.productivitymapping.PROJECT_GLOBALS;
import com.harrymt.productivitymapping.utility.MapUtil;
import com.harrymt.productivitymapping.database.DatabaseAdapter;
import com.harrymt.productivitymapping.R;
import com.harrymt.productivitymapping.coredata.Zone;
import java.util.ArrayList;

/**
 * Track fragment showing the users current location with a zone they're in,
 * the main place to create new zones and start a study session.
 */
public class TrackFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = PROJECT_GLOBALS.LOG_NAME + "TrackFragment";

    // The map displaying the zone the user is in.
    public GoogleMap mMap;
    // Google API reference to load the map.
    protected GoogleApiClient mGoogleApiClient;


    /**
     * Creates the view by setting up the Google Map.
     *
     * @param inflater The LayoutInflater object that is used to inflate the view in the fragment.
     * @param container The parent view that the fragment's UI should be attached to.
     * @param savedInstanceState Fragments saved state.
     * @return Return the View for the fragment's UI.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_track, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_track);
        mapFragment.getMapAsync(this);

        // Remove the map popup that occurs on map click
        View view = mapFragment.getView();
        if(view != null) { view.setClickable(false); }

        buildGoogleApiClient();

        // Check to see if there has been a session before
        DatabaseAdapter dbAdapter = new DatabaseAdapter(getContext()); // Prepare the database
        // If there hasn't been a session before, don't enable the button
        v.findViewById(R.id.btnLastSession).setEnabled(dbAdapter.hasASessionEverStartedYet());
        dbAdapter.close();

        return v;
    }

    /**
     * OnStart Activity lifecycle.
     *
     * Connect to the Google Map API and draw the zones onto it.
     */
    @Override
    public void onStart() {
        super.onStart(); mGoogleApiClient.connect();

        if (mMap != null) {
            drawExistingZonesToMap();
        }
    }

    /**
     * OnStop Activity lifecycle.
     *
     * Disconnect from the Google Map API.
     */
    @Override
    public void onStop() {
        mGoogleApiClient.disconnect(); super.onStop();
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     * @param connectionHint Description of the connection.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "onConnected location permission error with google maps.");
            Toast.makeText(getContext(), "Please enable location!", Toast.LENGTH_SHORT).show();
            return;
        }

        Location loc = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(loc != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(loc.getLatitude(), loc.getLongitude()), 18.0f));
        }
        mMap.setMyLocationEnabled(true);
    }

    /**
     * Runs when the google maps api gets suspended.
     * @param cause of suspension.
     */
    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "onConnectionSuspended cause:" + cause + ".");
        mGoogleApiClient.connect();
    }

    /**
     * Runs when the connection to google api fails.
     * @param result what happened.
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.e(TAG, "onConnectionFailed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    /**
     * Callback for when the map is ready to be drawn upon.
     *
     * @param googleMap reference to map.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        drawExistingZonesToMap();
    }

    /**
     * Setup the Google API client.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }


    /**
     * Draw all the existing zones to the map.
     */
    private void drawExistingZonesToMap() {
        mMap.clear();

        DatabaseAdapter dbAdapter = new DatabaseAdapter(getContext()); // Prepare the database
        ArrayList<Zone> zones = dbAdapter.getAllZones();

        for (Zone zone : zones) {
            MapUtil.drawCircleWithWindow(getContext(), mMap, zone);
        }
        dbAdapter.close();
    }

    /**
     * Refresh the data in this fragment.
     */
    public void refresh() {
        drawExistingZonesToMap();
    }
}