package com.harrymt.productivitymapping.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.support.v4.app.FragmentActivity;

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
import com.harrymt.productivitymapping.DraggableCircle;
import com.harrymt.productivitymapping.database.DatabaseAdapter;
import com.harrymt.productivitymapping.R;
import com.harrymt.productivitymapping.Zone;

import java.util.ArrayList;


public class ZoneEditActivity extends FragmentActivity implements GoogleMap.OnMarkerDragListener,
        OnMapReadyCallback {

    // The zone that we are editing.
    private Zone zoneToEdit = null;

    private DraggableCircle currentCircle;

    private GoogleMap mMap;

    int REQUEST_CODE_SET_ZONE_PREFS = 3212;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_zone_edit);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Get the zone that is passed in here.
        zoneToEdit = getIntent().getExtras().getParcelable("zone");

        // If its a new zone
        if(zoneToEdit != null) {
            if (zoneToEdit.zoneID == -1) {
                this.setTitle("Create new zone");
            } else {
                this.setTitle("Edit zone");
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == REQUEST_CODE_SET_ZONE_PREFS) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // Pass the Zone object back to the MainActivity
                zoneToEdit.keywords = data.getStringArrayExtra("keywords");
                zoneToEdit.blockingApps = data.getStringArrayExtra("packages");
                zoneToEdit.name = data.getStringExtra("name");
                zoneToEdit.autoStartStop = data.getBooleanExtra("autoStartStop", false) ? 1 : 0;

                Intent newZoneDetails = new Intent(); newZoneDetails.putExtra("zone", zoneToEdit);
                setResult(RESULT_OK, newZoneDetails);
                finish(); // Leave
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.setOnMarkerDragListener(this);

        final LatLng CURRENT_ZONE = new LatLng(zoneToEdit.lat, zoneToEdit.lng);
        currentCircle = new DraggableCircle(this, map, CURRENT_ZONE, zoneToEdit.radiusInMeters);

        float radiusMultiplier = zoneToEdit.radiusInMeters / 18;

        // Move the map so that it is centered on the initial circle
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(CURRENT_ZONE, 20.0f - radiusMultiplier));

        drawExistingZonesToMap();
        enableCurrentLocation();
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        onMarkerMoved(marker);
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        onMarkerMoved(marker);
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        onMarkerMoved(marker);
    }

    private void onMarkerMoved(Marker marker) {
        currentCircle.onMarkerMoved(marker);
    }

    public void moveToTheNextScreen(View view) {
        Intent zoneIntent = new Intent(this, ZonePreferenceEdit.class);
        LatLng pos = currentCircle.centerMarker.getPosition();
        zoneToEdit.lat = pos.latitude;
        zoneToEdit.lng = pos.longitude;
        zoneToEdit.radiusInMeters = (float) DraggableCircle.toRadiusMeters(currentCircle.centerMarker.getPosition(), currentCircle.radiusMarker.getPosition());

        zoneIntent.putExtra("zone", zoneToEdit);
        startActivityForResult(zoneIntent, REQUEST_CODE_SET_ZONE_PREFS);
    }

    /**
     * Enables current location for the map.
     */
    private void enableCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            // mMap.moveCamera(CameraUpdateFactory.newLatLng(location)); // Move to curr location
        } else {
            // Show rationale and request permission.
        }
    }

    /**
     * Draw all the existing zones to the map.
     */
    private void drawExistingZonesToMap() {
        DatabaseAdapter dbAdapter = new DatabaseAdapter(this); // Prepare the database

        ArrayList<Zone> zones = dbAdapter.getAllZones();
        for (Zone zone : zones) {
            if (zone.zoneID != zoneToEdit.zoneID) { // if it isnt our zone we are editing!
                int strokeColor = 0xffff0000; // red outline
                int shadeColor = 0x44ff0000; // 0x44ff0000; // opaque red fill
                drawCircle(zone, strokeColor, shadeColor);
            }
        }
        dbAdapter.close();
    }

    /**
     * Draw a circle at the zone and display the zone name in a
     * info window.
     *
     * @param zone to draw
     * @param stroke color
     * @param shade color
     */
    private void drawCircle(Zone zone, int stroke, int shade) {
        CircleOptions circleOptions = new CircleOptions()
                .center(new LatLng(zone.lat, zone.lng))
                .radius(zone.radiusInMeters)
                .fillColor(shade)
                .strokeColor(stroke)
                .strokeWidth(8);
        mMap.addCircle(circleOptions);

        // Add icon with percentage and name
        IconGenerator ic = new IconGenerator(this);
        Marker m = mMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromBitmap(ic.makeIcon(zone.name)))
                        .position(new LatLng(zone.lat, zone.lng))
        );

        m.showInfoWindow();
    }
}