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
 * Represents a draggable circle with 2 markers, 1 marks the center point that
 * can be moved around the other is displayed on the edge of the circle serving
 * as a handle to increase the radius.
 *
 * Class taken from Google sample code.
 * https://github.com/googlemaps/android-samples/blob/master/ApiDemos/app/src/main/java/com/example/mapdemo/CircleDemoActivity.java
 */
public class DraggableCircle {
    private static final String TAG = PROJECT_GLOBALS.LOG_NAME + "DraggableCircle";

    private static final double RADIUS_OF_EARTH_METERS = 6371009;

    // Markers
    public final Marker centerMarker;
    public final Marker radiusMarker;

    // Actual circle
    public final Circle circle;

    // Circle radius
    public double radius;

    // Context of app.
    Context context;

    /**
     * Constructor.
     *
     * @param c Context of app.
     * @param mMap Map reference.
     * @param center Center point.
     * @param radius Radius.
     */
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

    /**
     * When either marker gets moved.
     *
     * @param marker Marker.
     * @return True if its one of the markers we have placed, false if not.
     */
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
     * Gets a point on the edge of the circle.
     *
     * @param center Center point of circle
     * @param radius Radius of circle
     * @return Point on edge of circle.
     */
    public static LatLng toRadiusLatLng(LatLng center, double radius) {
        double radiusAngle = Math.toDegrees(radius / RADIUS_OF_EARTH_METERS) /
                Math.cos(Math.toRadians(center.latitude));
        return new LatLng(center.latitude, center.longitude + radiusAngle);
    }

    /**
     * Gets the radius 2 points if they are on a circle.
     * @param center Center point of circle.
     * @param radius Radius of circle.
     * @return Radius.
     */
    public static double toRadiusMeters(LatLng center, LatLng radius) {
        float[] result = new float[1];
        Location.distanceBetween(center.latitude, center.longitude,
                radius.latitude, radius.longitude, result);
        return result[0];
    }

    /**
     * Create a large scaled marker.
     *
     * @return A scaled marker image.
     */
    public BitmapDescriptor getLargeMarker() {
        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_marker_black);
        return BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(largeIcon, 250, 250, false));
    }
}