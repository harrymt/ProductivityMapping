package com.harrymt.productivitymapping.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.harrymt.productivitymapping.PROJECT_GLOBALS;
import com.harrymt.productivitymapping.activities.MainActivity;
import com.harrymt.productivitymapping.utility.MapUtil;
import com.harrymt.productivitymapping.listviews.MapViewHolder;
import com.harrymt.productivitymapping.listviews.MapAdapter;
import com.harrymt.productivitymapping.database.DatabaseAdapter;
import com.harrymt.productivitymapping.R;
import com.harrymt.productivitymapping.coredata.Zone;
import java.util.ArrayList;
import android.support.v4.app.ListFragment;

/**
 * Displays a list of zones in a list view, with a google map for each item
 * and information about the zone.
 */
public class ZonesFragment extends Fragment {
    private static final String TAG = PROJECT_GLOBALS.LOG_NAME + "ZonesFragment";

    /**
     * Creates the view by loading the zones into the list view.
     *
     * @param inflater The LayoutInflater object that is used to inflate the view in the fragment.
     * @param container The parent view that the fragment's UI should be attached to.
     * @param savedInstanceState Fragments saved state.
     * @return Return the View for the fragment's UI.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_map, container, false);
        loadZonesToListView();
        return v;
    }

    /**
     * Reload all the zones from the database back to the list view.
     */
    public void loadZonesToListView() {
        // TODO add empty list item to zones that says no zones, go create one.
        // Set a custom list adapter for a list of locations
        ArrayList<Zone> zs = getZones();
        ListFragment mList = (ListFragment) getChildFragmentManager().findFragmentById(R.id.list);
        // Convert to array
        Zone[] zones = new Zone[zs.size()]; int i = 0; for (Zone z : zs) { zones[i] = z; i++;}

        // Set the adapater.
        mList.setListAdapter(new MapAdapter(this, getActivity(), (MainActivity) this.getActivity(), zones));

        // Set a RecyclerListener to clean up MapView from ListView
        mList.getListView().setRecyclerListener(mRecycleListener);
    }

    /**
     * Move the camera to the zone and draw it .
     * @param map The map reference.
     * @param zone Zone to draw.
     */
    public void setMapLocation(GoogleMap map, Zone zone) {

        // Add a marker for this item and set the camera
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(zone.lat, zone.lng), 18.0f));

        // Draw zone
        MapUtil.drawCircleWithWindow(getContext(), map, zone);

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

        /**
         * When a view is scrapped.
         * @param view View to be scrapped.
         */
        @Override
        public void onMovedToScrapHeap(View view) {
            MapViewHolder holder = (MapViewHolder) view.getTag();
            if (holder != null && holder.map != null) {
                // Clear the map and free up resources by changing the map type to none
                holder.map.clear();
                holder.map.setMapType(GoogleMap.MAP_TYPE_NONE);
            }
        }
    };

    /**
     * A list of locations to show in this ListView.
     * @return List of zones.
     */
    private ArrayList<Zone> getZones() {
        DatabaseAdapter dbAdapter = new DatabaseAdapter(getContext());
        ArrayList<Zone> zones = dbAdapter.getAllZones();
        dbAdapter.close();
        return zones;
    }

    /**
     * Refresh the data source in this fragment.
     */
    public void refresh() {
        loadZonesToListView();
    }
}