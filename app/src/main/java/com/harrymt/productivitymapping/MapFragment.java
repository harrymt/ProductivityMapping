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
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("g53ids", "MapFragment.onCreateView()");

        View v = inflater.inflate(R.layout.fragment_map, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        return v;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        setZoneLatLngs();

        enableCurrentLocation();
    }

    private void setZoneLatLngs() {
        // Add a marker and move the camera.
        LatLng location = new LatLng(52.9487713,-1.17);


        mMap.addMarker(new MarkerOptions().position(new LatLng(52.9505425,-1.1706994)).title("Marker at home"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(52.9205425,-1.1706994)).title("Marker at home"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(-52.9105425,-1.1706994)).title("Marker at home"));




        mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
    }

    private void enableCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            // Show rationale and request permission.
        }
    }
}
