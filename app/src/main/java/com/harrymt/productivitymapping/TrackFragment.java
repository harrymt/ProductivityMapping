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

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;


/**
 * Created by harrymt on 06/02/16.
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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setIndoorEnabled(false);
        setZoneLatLngs();
        enableCurrentLocation();
    }

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

    private void enableCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            // Move to curr location // mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
        } else {
            // Show rationale and request permission.
        }
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
    }
}