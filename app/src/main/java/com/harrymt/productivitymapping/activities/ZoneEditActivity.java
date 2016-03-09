package com.harrymt.productivitymapping.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;
import com.harrymt.productivitymapping.database.DatabaseAdapter;
import com.harrymt.productivitymapping.R;
import com.harrymt.productivitymapping.Zone;

import java.util.ArrayList;


public class ZoneEditActivity extends FragmentActivity implements GoogleMap.OnMarkerDragListener,
        OnMapReadyCallback {

    // The zone that we are editing.
    private Zone zoneToEdit = null;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zone_edit);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this)
                .addApi(AppIndex.API)
                .build();

        // Get the zone that is passed in here.
        Intent dataSentHere = getIntent(); Bundle data = dataSentHere.getExtras();
        zoneToEdit = data.getParcelable("zone");

        // If its a new zone
        if(zoneToEdit != null) {
            if (zoneToEdit.zoneID == -1) {
                this.setTitle("Create new zone");
            } else {
                this.setTitle("Edit zone");
            }
        }
    }


    int REQUEST_CODE_SET_ZONE_PREFS = 3212;

    public void moveToTheNextScreen(View view) {
        Intent zoneIntent = new Intent(this, ZonePreferenceEdit.class);
        LatLng pos = currentCircle.centerMarker.getPosition();
        zoneToEdit.lat = pos.latitude;
        zoneToEdit.lng = pos.longitude;
        zoneToEdit.radiusInMeters = (float) currentCircle.radius;

        zoneIntent.putExtra("zone", zoneToEdit);
        startActivityForResult(zoneIntent, REQUEST_CODE_SET_ZONE_PREFS);
    }

    private String uniqueDelimiter = "_%@%_";

    /**
     * Utility function to convert a String separated by the unqiue delimited back into a String.
     * @param str
     * @return String[]
     */
    public String[] sqlConvertStringToArray(String str)
    {
        if (str.length() == 0) return new String[] {};
        return str.split(uniqueDelimiter, -1);
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

    private GoogleMap mMap;

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.setOnMarkerDragListener(this);

        final LatLng CURRENT_ZONE = new LatLng(zoneToEdit.lat, zoneToEdit.lng);
        currentCircle = new DraggableCircle(CURRENT_ZONE, zoneToEdit.radiusInMeters);

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
        dbAdapter.open(); // Open it for writing

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

    public static final double RADIUS_OF_EARTH_METERS = 6371009;

    private DraggableCircle currentCircle;

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "ZoneEdit Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.harrymt.productivitymapping/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "ZoneEdit Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.harrymt.productivitymapping/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    private BitmapDescriptor getLargeMarker(){
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_marker_black);
        return BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(largeIcon, 250, 250, false));
    }

    private class DraggableCircle {

        private final Marker centerMarker;

        private final Marker radiusMarker;

        private final Circle circle;

        private double radius;

        public DraggableCircle(LatLng center, double radius) {
            this.radius = radius;
            centerMarker = mMap.addMarker(new MarkerOptions()
                    .position(center)
                    .draggable(true)
                    .icon(getLargeMarker()));
            radiusMarker = mMap.addMarker(new MarkerOptions()
                    .position(toRadiusLatLng(center, radius))
                    .draggable(true)
                    .icon(getLargeMarker()));

            circle = mMap.addCircle(new CircleOptions()
                    .center(center)
                    .radius(radius)
                    .strokeWidth(2f)
                    .strokeColor(Color.BLUE)
                    .fillColor(Color.HSVToColor(100, new float[]{10, 1, 1})));
        }

        public boolean onMarkerMoved(Marker marker) {
            if (marker.equals(centerMarker)) {
                circle.setCenter(marker.getPosition());
                radiusMarker.setPosition(toRadiusLatLng(marker.getPosition(), radius));
                return true;
            }
            if (marker.equals(radiusMarker)) {
                radius = toRadiusMeters(centerMarker.getPosition(), radiusMarker.getPosition());
                circle.setRadius(radius);
                return true;
            }
            return false;
        }
    }

    /**
     * Generate LatLng of radius marker
     */
    private static LatLng toRadiusLatLng(LatLng center, double radius) {
        double radiusAngle = Math.toDegrees(radius / RADIUS_OF_EARTH_METERS) /
                Math.cos(Math.toRadians(center.latitude));
        return new LatLng(center.latitude, center.longitude + radiusAngle);
    }

    private static double toRadiusMeters(LatLng center, LatLng radius) {
        float[] result = new float[1];
        Location.distanceBetween(center.latitude, center.longitude,
                radius.latitude, radius.longitude, result);
        return result[0];
    }
}