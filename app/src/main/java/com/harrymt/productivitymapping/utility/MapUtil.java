package com.harrymt.productivitymapping.utility;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;
import com.harrymt.productivitymapping.PROJECT_GLOBALS;
import com.harrymt.productivitymapping.coredata.Zone;

/**
 * Class containing utility functions for the Google Maps.
 */
public class MapUtil {
    private static final String TAG = PROJECT_GLOBALS.LOG_NAME + "MapUtil";

    /**
     * Draws a circle to the map with an info window.
     *
     * @param c Context of app.
     * @param mMap Map to draw on.
     * @param zone Zone information to display on the map.
     */
    public static void drawCircleWithWindow(Context c, GoogleMap mMap, Zone zone) {
        int shadeColor = 0x44ff0000; //opaque red fill
        int strokeColor = 0xffff0000; //red outline

        CircleOptions circleOptions = new CircleOptions()
                .center(new LatLng(zone.lat, zone.lng))
                .radius(zone.radiusInMeters)
                .fillColor(shadeColor)
                .strokeColor(strokeColor)
                .strokeWidth(8);
        mMap.addCircle(circleOptions);

        // Add icon with name
        IconGenerator ic = new IconGenerator(c);
        Marker m = mMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromBitmap(ic.makeIcon(zone.name)))
                        .position(new LatLng(zone.lat, zone.lng))
        );

        m.showInfoWindow();
    }

    /**
     * Calculates the distance from a point to another point, using Haversines formula.
     *
     * https://en.wikipedia.org/wiki/Haversine_formula
     *
     * Taken from:
     * http://stackoverflow.com/questions/27928/calculate-distance-between-two-latitude-longitude-points-haversine-formula?rq=1
     *
     * @param lat1 Lat of Point 1.
     * @param lng1 Lng of Point 1.
     * @param lat2 Lat of Point 2.
     * @param lng2 Lng of Point 2.
     * @return The distance from point 1 and point 2.
     */
    public static double distFrom(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371.0; // 3958.75 miles (or 6371.0 kilometers)
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);
        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return earthRadius * c * 100;
    }
}