package com.harrymt.productivitymapping;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapFragment extends Fragment {

    private SupportMapFragment fragment;
    private GoogleMap map;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("g53ids", "MapFragment.onCreateView()");
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d("g53ids", "MapFragment.onActivityCreated()");
        super.onActivityCreated(savedInstanceState);
        FragmentManager fm = getChildFragmentManager();
        fragment = (SupportMapFragment) fm.findFragmentById(R.id.map);
        if (fragment == null) {
            fragment = SupportMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.map, fragment).commit();
        }
    }

    @Override
    public void onResume() {
        Log.d("g53ids", "MapFragment.onResume()");
        super.onResume();
        if (map == null) {
            map = fragment.getMap();
            map.addMarker(new MarkerOptions().position(new LatLng(0, 0)));
        }
    }

    @Override
    public void onDestroy() {
        Log.d("g53ids", "MapFragment.onDestroy()");
    }

    @Override
    public void onPause() {
        Log.d("g53ids", "MapFragment.onPause()");
    }
}
