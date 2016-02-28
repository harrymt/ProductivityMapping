package com.harrymt.productivitymapping;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;

import java.util.ArrayList;
import java.util.Random;


public class TrackFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    public GoogleMap mMap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_track, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_track);
        mapFragment.getMapAsync(this);
        buildGoogleApiClient();

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();mGoogleApiClient.connect();
    }
    @Override
    public void onStop() {
        mGoogleApiClient.disconnect(); super.onStop();
    }

    protected GoogleApiClient mGoogleApiClient;

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
            Log.d("g53ids", "Permission ERROR");
            return;
        }
        Location loc = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(loc.getLatitude(), loc.getLongitude()), 18.0f));
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        mGoogleApiClient.connect();
    }
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i("g53ids", "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
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
        dbAdapter.open(); // Open it for writing

        ArrayList<Zone> zones = dbAdapter.getAllZones();
        // Move to first zone
        if(zones.size() > 0) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(zones.get(0).lat, zones.get(0).lng), 18.0f));
        }

        for (Zone zone : zones) {
            int strokeColor = 0xffff0000; //red outline
            int shadeColor = 0x44faaaaa; //0x44ff0000; //opaque red fill
            drawCircle(zone, strokeColor, shadeColor);
        }
        dbAdapter.close();
    }

    private void drawCircle(Zone zone, int stroke, int shade) {

        CircleOptions circleOptions = new CircleOptions()
                .center(new LatLng(zone.lat, zone.lng))
                .radius(zone.radiusInMeters)
                .fillColor(stroke)
                .strokeColor(shade)
                .strokeWidth(8);
        mMap.addCircle(circleOptions);

        // Add icon with percentage and name
        IconGenerator ic = new IconGenerator(getContext());
        // Choose colour
        double productivityPercentage = (double) Math.round(new Random().nextDouble() * 100d) / 100d; // TODO get P%
        int iconColor = IconGenerator.STYLE_DEFAULT;
        if(productivityPercentage > 0.7) {
            iconColor = IconGenerator.STYLE_GREEN;
        } else if (productivityPercentage < 0.3) {
            iconColor = IconGenerator.STYLE_RED;
        }
        ic.setStyle(iconColor);
        Marker m = mMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromBitmap(ic.makeIcon(productivityPercentage + "% : " + zone.name)))
                        .position(new LatLng(zone.lat, zone.lng))
        );

        m.showInfoWindow();
    }
}