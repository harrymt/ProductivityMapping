package com.harrymt.productivitymapping;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class MapFragment extends Fragment  implements OnMapReadyCallback {

    private GoogleMap mMap;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("g53ids", "MapFragment.onCreateView()");

        View v = inflater.inflate(R.layout.fragment_map, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_map);
        mapFragment.getMapAsync(this);
        return v;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setIndoorEnabled(false);
        setZoneLatLngs();

        Marker m = mMap.addMarker(new MarkerOptions().position(new LatLng(52.9536037, -1.1890631)).title("Home: 70%"));
        m.showInfoWindow();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(52.9536037, -1.1890631), 18.0f));
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

        CircleOptions circleOptions = new CircleOptions()
                .center(new LatLng(zone.lat, zone.lng))
                .radius(zone.radiusInMeters)
                .fillColor(shadeColor)
                .strokeColor(strokeColor)
                .strokeWidth(8);
        mMap.addCircle(circleOptions);
    }
}