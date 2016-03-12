package com.harrymt.productivitymapping;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by harrymt on 10/03/16.
 */

public class DraggableCircle {

    private static final double RADIUS_OF_EARTH_METERS = 6371009;

    public final Marker centerMarker;

    public final Marker radiusMarker;

    public final Circle circle;

    public double radius;

    Context context;

    public DraggableCircle(Context c, GoogleMap mMap, LatLng center, double radius) {
        context = c;
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


    /**
     * Generate LatLng of radius marker
     */
    public static LatLng toRadiusLatLng(LatLng center, double radius) {
        double radiusAngle = Math.toDegrees(radius / RADIUS_OF_EARTH_METERS) /
                Math.cos(Math.toRadians(center.latitude));
        return new LatLng(center.latitude, center.longitude + radiusAngle);
    }

    /**
     * Get distance between 2 points using an inverse formula, see docs.
     *
     */
    public static double toRadiusMeters(LatLng center, LatLng radius) {
        float[] result = new float[1];
        Location.distanceBetween(center.latitude, center.longitude,
                radius.latitude, radius.longitude, result);
        return result[0];
    }

    private BitmapDescriptor getLargeMarker() {
        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_marker_black);
        return BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(largeIcon, 250, 250, false));
    }
}
