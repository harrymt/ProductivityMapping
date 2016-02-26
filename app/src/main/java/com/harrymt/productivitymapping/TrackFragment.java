package com.harrymt.productivitymapping;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
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


/**
 * Created by harrymt on 06/02/16
 */
public class TrackFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_track, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_track);
        mapFragment.getMapAsync(this);

        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("g53ids", "TrackFragment.onPause()");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        final LatLng CURRENT_ZONE = new LatLng(52.9536037,-1.1890631); // TODO get current zone
        drawExistingZonesToMap();
        enableCurrentLocation();

        // Move the map so that it is centered on the initial circle
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(CURRENT_ZONE, 17.0f));
    }

    /**
     * Draw all the existing zones to the map.
     */
    private void drawExistingZonesToMap() {
        DatabaseAdapter dbAdapter = new DatabaseAdapter(getContext()); // Prepare the database
        dbAdapter.open(); // Open it for writing

        ArrayList<Zone> zones = dbAdapter.getAllZones();
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

    private void enableCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            // Move to curr location // mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
        } else {
            // Show rationale and request permission.
        }
    }


}