package com.harrymt.productivitymapping.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.harrymt.productivitymapping.MapUtil;
import com.harrymt.productivitymapping.database.DatabaseAdapter;
import com.harrymt.productivitymapping.R;
import com.harrymt.productivitymapping.Zone;
import com.harrymt.productivitymapping.activities.ZoneEditActivity;

import java.util.ArrayList;
import java.util.HashSet;

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
    private void loadZonesToListView() {

        // TODO add empty list item to zones that says no zones, go create one.
        // Set a custom list adapter for a list of locations
        ArrayList<Zone> zs = getZones();
        mList = (ListFragment) getChildFragmentManager().findFragmentById(R.id.list);
        // Convert to array
        Zone[] zones = new Zone[zs.size()]; int i = 0;for (Zone z : zs) { zones[i] = z; i++;}

        mList.setListAdapter(new MapAdapter(getActivity(), zones));
        // Set a RecyclerListener to clean up MapView from ListView
        mList.getListView().setRecyclerListener(mRecycleListener);
    }

    /**
     * Adapter that displays a name and {@link com.google.android.gms.maps.MapView} for each item.
     * The layout is defined in <code>lite_list_demo_row.xml</code>. It contains a MapView
     * that is programatically initialised in
     * {@link #getView(int, android.view.View, android.view.ViewGroup)}
     */
    private class MapAdapter extends ArrayAdapter<Zone> {

        private final HashSet<MapView> mMaps = new HashSet<>();

        public MapAdapter(Context context, Zone[] locations) {
            super(context, R.layout.list_map_row, R.id.tvZoneName, locations);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            ViewHolder holder;

            // Check if a view can be reused, otherwise inflate a layout and set up the view holder
            if (row == null) {
                // Inflate view from layout file
                row = getActivity().getLayoutInflater().inflate(R.layout.list_map_row, null);

                // Set up holder and assign it to the View
                holder = new ViewHolder();
                holder.mapView = (MapView) row.findViewById(R.id.lite_listrow_map);
                holder.name = (TextView) row.findViewById(R.id.tvZoneName);
                holder.appsToBlock = (TextView) row.findViewById(R.id.tvZoneAppsToBlock);
                holder.keywords = (TextView) row.findViewById(R.id.tvZoneKeywords);
                holder.editZone = (Button) row.findViewById(R.id.btnEditZone);
                holder.deleteZone = (Button) row.findViewById(R.id.btnDeleteZone);


                // Set holder as tag for row for more efficient access.
                row.setTag(holder);

                // Initialise the MapView
                holder.initializeMapView();

                // Keep track of MapView
                mMaps.add(holder.mapView);
            } else {
                // View has already been initialised, get its holder
                holder = (ViewHolder) row.getTag();
            }

            // Get the NamedLocation for this zone and attach it to the MapView
            Zone zone = getItem(position);
            holder.mapView.setTag(zone);

            // Ensure the map has been initialised by the on map ready callback in ViewHolder.
            // If it is not ready yet, it will be initialised with the NamedLocation set as its tag
            // when the callback is received.
            if (holder.map != null) {
                // The map is already ready to be used
                setMapLocation(holder.map, zone);
            }

            // Set the text label for this zone
            holder.name.setText(zone.name);

            String apps = ""; for(String app : zone.blockingApps) { apps += app + ", "; }
            String keywords = ""; for(String word : zone.keywords) { keywords += word + ", "; }

            holder.appsToBlock.setText("Apps blocked: " + (zone.blockingApps.length == 0 ? "none" : apps));
            holder.keywords.setText("Keywords set: " + (zone.keywords.length == 0 ? "none" : keywords));

            // setup on click for edit zone button
            holder.editZone.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // Get the zone object, saved in the tag!
                    ViewHolder vh = (ViewHolder) ((View) v.getParent().getParent()).getTag();
                    Zone z = (Zone) vh.mapView.getTag();

                    // start set zone activity, for this current zone
                    Intent editZoneActivityIntent = new Intent(getActivity(), ZoneEditActivity.class);
                    editZoneActivityIntent.putExtra("zone", z);
                    getActivity().startActivityForResult(editZoneActivityIntent, REQUEST_CODE_EDIT_ZONE);
                }
            });


            // setup on click for edit zone button
            holder.deleteZone.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // Get the zone object, saved in the tag!
                    ViewHolder vh = (ViewHolder) ((View) v.getParent().getParent()).getTag();
                    final Zone z = (Zone) vh.mapView.getTag();

                    // Show dialog box for this zone
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case DialogInterface.BUTTON_POSITIVE:
                                    // Delete the zone
                                    DatabaseAdapter dbAdapter = new DatabaseAdapter(getContext());
                                    dbAdapter.deleteZone(z.zoneID);
                                    dbAdapter.close();

                                    // Reload the listview
                                    loadZonesToListView();

                                    dialog.dismiss();
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    dialog.dismiss();
                                    break;
                            }
                        }
                    };

                    // Actually show the dialog box
                    AlertDialog.Builder ab = new AlertDialog.Builder(v.getContext());
                    ab.setMessage("Are you sure to delete?")
                            .setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();


                }
            });

            return row;
        }

        /**
         * Retuns the set of all initialised {@link MapView} objects.
         *
         * @return All MapViews that have been initialised programmatically by this adapter
         */
        public HashSet<MapView> getMaps() {
            return mMaps;
        }
    }

    int REQUEST_CODE_EDIT_ZONE = 3;

    /**
     * Displays a on a
     * {@link com.google.android.gms.maps.GoogleMap}.
     * Adds a marker and centers the camera on the NamedLocation with the normal map type.
     */
    private void setMapLocation(GoogleMap map, Zone zone) {
        // Add a marker for this item and set the camera
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(zone.lat, zone.lng), 18.0f));

        MapUtil.drawCircle(getContext(), map, zone);

        // Set the map type back to normal.
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        // map.setIndoorEnabled(true); // Not supported in LITE mode
    }

    /**
     * Holder for Views used in the
     * Once the  the <code>map</code> field is set, otherwise it is null.
     * When the {@link #onMapReady(com.google.android.gms.maps.GoogleMap)} callback is received and
     * the {@link com.google.android.gms.maps.GoogleMap} is ready, it stored in the {@link #map}
     * field. The map is then initialised with the NamedLocation that is stored as the tag of the
     * MapView. This ensures that the map is initialised with the latest data that it should
     * display.
     */
    class ViewHolder implements OnMapReadyCallback {

        MapView mapView;

        TextView name;
        TextView appsToBlock;
        TextView keywords;

        Button editZone;
        Button deleteZone;

        GoogleMap map;

        @Override
        public void onMapReady(GoogleMap googleMap) {
            MapsInitializer.initialize(getContext());
            map = googleMap;
            Zone data = (Zone) mapView.getTag();
            if (data != null) {
                setMapLocation(map, data);
            }
        }

        /**
         * Initialises the MapView by calling its lifecycle methods.
         */
        public void initializeMapView() {
            if (mapView != null) {
                // Initialise the MapView
                mapView.onCreate(null);
                // Set the map ready callback to receive the GoogleMap object
                mapView.getMapAsync(this);
            }
        }

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