package com.harrymt.productivitymapping;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
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
import com.google.maps.android.ui.BubbleIconFactory;
import com.google.maps.android.ui.IconGenerator;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;


public class ZoneEditActivity extends FragmentActivity implements GoogleMap.OnMarkerDragListener,
        OnMapReadyCallback {

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
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    int REQUEST_CODE_SET_ZONE_PREFS = 3212;

    public void setZoneCoords(View view) {
        Intent setZonePrefsActivityIntent = new Intent(this, ZonePreferenceEdit.class);
        startActivityForResult(setZonePrefsActivityIntent, REQUEST_CODE_SET_ZONE_PREFS);
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

                // Create new Zone
                String keywords = data.getStringExtra("keywords");
                String packages = data.getStringExtra("packages");
                String name = data.getStringExtra("name");
                LatLng pos = currentCircle.centerMarker.getPosition();
                Zone z = new Zone(pos.latitude, pos.longitude, currentCircle.radius, name, 0, sqlConvertStringToArray(keywords), sqlConvertStringToArray(packages));

                Bundle zone = new Bundle();
                zone.putParcelable("zone", z);

                Intent newZoneDetails = new Intent();
                newZoneDetails.putExtras(zone);
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

        final LatLng CURRENT_LOCATION = getCurrLocation();

        currentCircle = new DraggableCircle(CURRENT_LOCATION, DEFAULT_RADIUS);

        drawExistingZonesToMap();

        enableCurrentLocation();

        // Move the map so that it is centered on the initial circle
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(CURRENT_LOCATION, 20.0f));
    }

    private LatLng getCurrLocation() {
        // TODO get current location
        return new LatLng(52.9532976, -1.187156);
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
            // Move to curr location // mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
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
            drawCircle(zone);
        }
        dbAdapter.close();
    }

    private void drawCircle(Zone zone) {
        int strokeColor = 0xffff0000; //red outline
        int shadeColor = 0x44ff0000; //opaque red fill

        CircleOptions circleOptions = new CircleOptions()
                .center(new LatLng(zone.lat, zone.lng))
                .radius(zone.radiusInMeters)
                .fillColor(shadeColor)
                .strokeColor(strokeColor)
                .strokeWidth(8);
        mMap.addCircle(circleOptions);

        // Add icon with percentage and name
        IconGenerator ic = new IconGenerator(this);
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

    private static final double DEFAULT_RADIUS = 5;

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

    private class DraggableCircle {

        private final Marker centerMarker;

        private final Marker radiusMarker;

        private final Circle circle;

        private double radius;

        public DraggableCircle(LatLng center, double radius) {
            this.radius = radius;
            centerMarker = mMap.addMarker(new MarkerOptions()
                    .position(center)
                    .draggable(true));
            radiusMarker = mMap.addMarker(new MarkerOptions()
                    .position(toRadiusLatLng(center, radius))
                    .draggable(true)
                    .icon(BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_AZURE)));

            circle = mMap.addCircle(new CircleOptions()
                    .center(center)
                    .radius(radius)
                    .strokeWidth(2f)
                    .strokeColor(Color.BLUE)
                    .fillColor(Color.HSVToColor(100, new float[]{10, 1, 1})));
        }

        public DraggableCircle(LatLng center, LatLng radiusLatLng) {
            this.radius = toRadiusMeters(center, radiusLatLng);
            centerMarker = mMap.addMarker(new MarkerOptions()
                    .position(center)
                    .draggable(true));
            radiusMarker = mMap.addMarker(new MarkerOptions()
                    .position(radiusLatLng)
                    .draggable(true)
                    .icon(BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_AZURE)));
            circle = mMap.addCircle(new CircleOptions()
                    .center(center)
                    .radius(radius)
                    .strokeWidth(2f)
                    .strokeColor(Color.BLUE)
                    .fillColor(Color.HSVToColor(100, new float[]{10, 1, 1})));
            radiusMarker.showInfoWindow();
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
