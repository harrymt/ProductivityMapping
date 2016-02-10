package com.harrymt.productivitymapping;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MapFragment extends Fragment {

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("g53ids", "MapFragment.onCreateView()");
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

}
