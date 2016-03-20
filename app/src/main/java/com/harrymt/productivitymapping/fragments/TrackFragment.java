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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.harrymt.productivitymapping.utility.MapUtil;
import com.harrymt.productivitymapping.database.DatabaseAdapter;
import com.harrymt.productivitymapping.R;
import com.harrymt.productivitymapping.Zone;

import java.util.ArrayList;


public class TrackFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "TrackFragment";
    public GoogleMap mMap;

    protected GoogleApiClient mGoogleApiClient;


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

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (visible && mMap != null) {
            mMap.clear();
            drawExistingZonesToMap();
        }
    }

    @Override
    public void onStart() {
        super.onStart(); mGoogleApiClient.connect();

        if (mMap != null) {
            mMap.clear();
            drawExistingZonesToMap();
        }
    }

    @Override
    public void onStop() {
        mGoogleApiClient.disconnect(); super.onStop();
    }


    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("g53ids", "Permission ERROR");
            return;
        }

        Location loc = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(loc != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(loc.getLatitude(), loc.getLongitude()), 18.0f));
        }
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.e("g53ids", "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        drawExistingZonesToMap();
    }

    /**
     * Draw all the existing zones to the map.
     */
    private void drawExistingZonesToMap() {
        DatabaseAdapter dbAdapter = new DatabaseAdapter(getContext()); // Prepare the database

        ArrayList<Zone> zones = dbAdapter.getAllZones();

        for (Zone zone : zones) {
            MapUtil.drawCircle(getContext(), mMap, zone);
        }
        dbAdapter.close();
    }
}