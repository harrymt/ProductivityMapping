package com.harrymt.productivitymapping;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class ZoneEditActivity extends FragmentActivity implements GoogleMap.OnMarkerDragListener,
        OnMapReadyCallback  {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zone_edit);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    int REQUEST_CODE_SET_ZONE_PREFS = 3212;

    public void setZoneCoords(View view) {
        Intent setZonePrefsActivityIntent = new Intent(this, ZonePreferenceEdit.class);
        startActivityForResult(setZonePrefsActivityIntent, REQUEST_CODE_SET_ZONE_PREFS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == REQUEST_CODE_SET_ZONE_PREFS) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {

                String keywords = data.getStringExtra("keywords");
                String packages = data.getStringExtra("packages");

                Intent zoneCoordinates = new Intent();
                LatLng pos = currentCircle.centerMarker.getPosition();
                Zone z = new Zone(pos.latitude, pos.longitude, currentCircle.radius);
                Bundle zone = new Bundle();
                zone.putParcelable("zone", z);
                zoneCoordinates.putExtras(zone);
                zoneCoordinates.putExtra("keywords", keywords);
                zoneCoordinates.putExtra("packages", packages);
                setResult(RESULT_OK, zoneCoordinates);
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

        DraggableCircle circle = new DraggableCircle(CURRENT_LOCATION, DEFAULT_RADIUS);
        currentCircle = circle;

        setZoneLatLngs();
        enableCurrentLocation();

        // Move the map so that it is centered on the initial circle
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(CURRENT_LOCATION, 20.0f));
    }

    private LatLng getCurrLocation() {
        return new LatLng(52.9532976,-1.187156);
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

    //@Override
    //public void onMapLongClick(LatLng point) {
//        // We know the center, let's place the outline at a point 3/4 along the view.
//        View view = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
//                .getView();
//        LatLng radiusLatLng = mMap.getProjection().fromScreenLocation(new Point(
//                view.getHeight() * 3 / 4, view.getWidth() * 3 / 4));
//
//        // ok create it
//        DraggableCircle circle = new DraggableCircle(point, radiusLatLng);
//        mCircles.add(circle);
  //  }

    public Zone[] getZones() {
        return new Zone[] {
                new Zone(52.9536037,-1.1890631, 10.0),
                new Zone(52.9205425,-1.17, 100.0),
                new Zone(52.9417713,-1.17, 100.0),
                new Zone(52.9387713,-1.17, 100.0)
        };
    }

    private void setZoneLatLngs() {
        Zone[] zones = getZones();
        for(Zone zone : zones) {
            drawCircle(zone);
        }
    }

    private void drawCircle(Zone zone) {
        int strokeColor = 0xffff0000; //red outline
        int shadeColor = 0x44ff0000; //opaque red fill
        Circle mCircle;

        CircleOptions circleOptions = new CircleOptions()
                .center(new LatLng(zone.lat, zone.lng))
                .radius(zone.radiusInMeters)
                .fillColor(shadeColor)
                .strokeColor(strokeColor)
                .strokeWidth(8);
        mCircle = mMap.addCircle(circleOptions);

        Marker m = mMap.addMarker(new MarkerOptions()
                .title("Hallward Library")
                .snippet("75% productivity")
                .position(new LatLng(zone.lat, zone.lng)));

        // hard to show every info window...
        m.showInfoWindow();
    }

    private static final double DEFAULT_RADIUS = 5;

    public static final double RADIUS_OF_EARTH_METERS = 6371009;

    private DraggableCircle currentCircle;

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
                    .fillColor(Color.HSVToColor(100, new float[]{10,1,1})));
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

    /** Generate LatLng of radius marker */
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
