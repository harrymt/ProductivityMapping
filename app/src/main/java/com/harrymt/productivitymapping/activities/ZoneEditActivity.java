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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.harrymt.productivitymapping.DraggableCircle;
import com.harrymt.productivitymapping.PROJECT_GLOBALS;
import com.harrymt.productivitymapping.database.DatabaseAdapter;
import com.harrymt.productivitymapping.R;
import com.harrymt.productivitymapping.coredata.Zone;
import com.harrymt.productivitymapping.utility.MapUtil;
import java.util.ArrayList;

/**
 * Editing the radius and longitude and latitude of the zone we are either creating
 * or editing.
 */
public class ZoneEditActivity extends FragmentActivity implements GoogleMap.OnMarkerDragListener, OnMapReadyCallback {
    private static final String TAG = PROJECT_GLOBALS.LOG_NAME + "ZoneEditActivity";

    // The zone that we are editing.
    private Zone zoneToEdit = null;

    // Circle that represents the zone.
    private DraggableCircle currentCircle;

    // Reference to the map we are using.
    private GoogleMap mMap;

    /**
     * OnCreate of ZoneEditActivity, setup map handler and get the zone information
     * passed in here.
     *
     * @param savedInstanceState saved state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zone_edit);

        // Setup the map on ready handlers.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Get the zone that is passed in here.
        zoneToEdit = getIntent().getExtras().getParcelable("zone");

        // If its a new zone
        if (zoneToEdit != null && zoneToEdit.zoneID == -1) {
            this.setTitle(R.string.activity_zone_create);
        }
    }


    /**
     * When receiving information from the ZonePreferenceEditActivity Activity.
     * We add this info and send the result back to the MainActivity.
     *
     * @param requestCode Request code.
     * @param resultCode Result Code, if success.
     * @param data Data that we have recieved.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to & if its successful
        if (requestCode == PROJECT_GLOBALS.REQUEST_CODE_SET_ZONE_PREFS && resultCode == RESULT_OK) {
            // Pass the Zone object back to the MainActivity
            zoneToEdit.keywords = data.getStringArrayExtra("keywords");
            zoneToEdit.blockingApps = data.getStringArrayExtra("packages");
            zoneToEdit.name = data.getStringExtra("name");

            Intent newZoneDetails = new Intent(); newZoneDetails.putExtra("zone", zoneToEdit);
            setResult(RESULT_OK, newZoneDetails);
            finish(); // Leave
        }
    }

    /**
     * Google Maps map handler, when the map is ready to
     * be interacted with.
     *
     * @param map Map that is read.
     */
    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.setOnMarkerDragListener(this);

        // Create a interactive circle
        final LatLng zoneEditing = new LatLng(zoneToEdit.lat, zoneToEdit.lng);
        currentCircle = new DraggableCircle(this, map, zoneEditing, zoneToEdit.radiusInMeters);

        // Set a multiplier based on the radius, so we know how much to zoom.
        float radiusMultiplier = zoneToEdit.radiusInMeters / 18;

        // Move the map so that it is centered on the initial circle
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(zoneEditing, 20.0f - radiusMultiplier));

        // Draw all other zones to map
        drawExistingZonesToMap();

        // Setup the users current location.
        enableCurrentLocation();
    }

    /**
     * Google Maps marker handler.
     *
     * @param marker Marker started dragged.
     */
    @Override
    public void onMarkerDragStart(Marker marker) {
        currentCircle.onMarkerMoved(marker);
    }

    /**
     * Google Maps marker handler.
     *
     * @param marker Marker finished dragged.
     */
    @Override
    public void onMarkerDragEnd(Marker marker) {
        currentCircle.onMarkerMoved(marker);
    }

    /**
     * Google Maps marker handler.
     *
     * @param marker Marker dragged.
     */
    @Override
    public void onMarkerDrag(Marker marker) {
        currentCircle.onMarkerMoved(marker);
    }


    /**
     * OnClick btnOpenZonePreferenceActivity
     *
     * Open the ZonePreferenceEditActivity Activity and pass in zone attributes we gathered
     * at this screen.
     *
     * @param view Button: btnOpenZonePreferenceActivity
     */
    public void moveToTheNextScreen(View view) {
        Intent zoneIntent = new Intent(this, ZonePreferenceEditActivity.class);
        LatLng pos = currentCircle.centerMarker.getPosition();
        zoneToEdit.lat = pos.latitude;
        zoneToEdit.lng = pos.longitude;
        zoneToEdit.radiusInMeters = (float) DraggableCircle.toRadiusMeters(currentCircle.centerMarker.getPosition(), currentCircle.radiusMarker.getPosition());

        zoneIntent.putExtra("zone", zoneToEdit);
        startActivityForResult(zoneIntent, PROJECT_GLOBALS.REQUEST_CODE_SET_ZONE_PREFS);
    }

    /**
     * Enables current location for the map.
     */
    private void enableCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
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
                MapUtil.drawCircleWithWindow(this, mMap, zone);
            }
        }
        dbAdapter.close();
    }
}