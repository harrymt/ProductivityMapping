package com.harrymt.productivitymapping.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.harrymt.productivitymapping.utility.MapUtil;
import com.harrymt.productivitymapping.ViewHolder;
import com.harrymt.productivitymapping.adapters.MapAdapter;
import com.harrymt.productivitymapping.database.DatabaseAdapter;
import com.harrymt.productivitymapping.R;
import com.harrymt.productivitymapping.Zone;

import java.util.ArrayList;

import android.support.v4.app.ListFragment;

public class ZonesFragment extends Fragment {

    private ListFragment mList;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_map, container, false);
        loadZonesToListView();
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadZonesToListView();
    }

    // Reload all zones from the DB again
    public void loadZonesToListView() {

        // TODO add empty list item to zones that says no zones, go create one.
        // Set a custom list adapter for a list of locations
        ArrayList<Zone> zs = getZones();
        Log.e("g53ids", zs.size()+ "");
        mList = (ListFragment) getChildFragmentManager().findFragmentById(R.id.list);
        // Convert to array
        Zone[] zones = new Zone[zs.size()]; int i = 0; for (Zone z : zs) { zones[i] = z; i++;}

        mList.setListAdapter(new MapAdapter(this, getActivity(), zones));
        // Set a RecyclerListener to clean up MapView from ListView
        mList.getListView().setRecyclerListener(mRecycleListener);
    }


    /**
     * Displays a on a
     * {@link com.google.android.gms.maps.GoogleMap}.
     * Adds a marker and centers the camera on the NamedLocation with the normal map type.
     */
    public void setMapLocation(GoogleMap map, Zone zone) {

        // Add a marker for this item and set the camera
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(zone.lat, zone.lng), 18.0f));

        MapUtil.drawCircle(getContext(), map, zone);

        // Set the map type back to normal.
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        // map.setIndoorEnabled(true); // Not supported in LITE mode
    }

    /**
     * RecycleListener that completely clears the {@link com.google.android.gms.maps.GoogleMap}
     * attached to a row in the ListView.
     * Sets the map type to {@link com.google.android.gms.maps.GoogleMap#MAP_TYPE_NONE} and clears
     * the map.
     */
    private AbsListView.RecyclerListener mRecycleListener = new AbsListView.RecyclerListener() {

        @Override
        public void onMovedToScrapHeap(View view) {
            ViewHolder holder = (ViewHolder) view.getTag();
            if (holder != null && holder.map != null) {
                // Clear the map and free up resources by changing the map type to none
                holder.map.clear();
                holder.map.setMapType(GoogleMap.MAP_TYPE_NONE);
            }
        }
    };

    /**
     * A list of locations to show in this ListView.
     */
    private ArrayList<Zone> getZones() {
        DatabaseAdapter dbAdapter = new DatabaseAdapter(getContext());
        ArrayList<Zone> zones = dbAdapter.getAllZones();
        dbAdapter.close();
        return zones;
    }
}