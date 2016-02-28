package com.harrymt.productivitymapping;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import android.support.v4.app.ListFragment;

public class MapFragment extends Fragment {

    private ListFragment mList;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("g53ids", "MapFragment.onCreateView()");

        View v = inflater.inflate(R.layout.fragment_map, container, false);
        loadZonesToListView();
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("g53ids", "MapFragment.resume()");

        loadZonesToListView();
    }

    // Reload all zones from the DB again
    private void loadZonesToListView() {

        // TODO add empty list item to zones that says no zones, go create one.
        // Set a custom list adapter for a list of locations
        ArrayList<Zone> zs = getZones();
        Zone[] zones;
        // Convert to array
        zones = new Zone[zs.size()]; int i = 0;for (Zone z : zs) { zones[i] = z; i++;}

        mList = (ListFragment) getChildFragmentManager().findFragmentById(R.id.list);
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
                holder.productivityPercentage = (TextView) row.findViewById(R.id.tvZoneProductivityPercentage);
                holder.appsToBlock = (TextView) row.findViewById(R.id.tvZoneAppsToBlock);
                holder.keywords = (TextView) row.findViewById(R.id.tvZoneKeywords);
                holder.editZone = (Button) row.findViewById(R.id.btnEditZone);


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
            holder.productivityPercentage.setText("75% productive!");
            holder.appsToBlock.setText("Blocked Apps: " + zone.blockingAppsAsStr());
            holder.keywords.setText("Keywords: " + zone.keywordsAsStr());

            // setup on click for edit zone button
            holder.editZone.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // Get the zone object, saved in the tag!
                    ViewHolder vh = (ViewHolder) ((View) v.getParent()).getTag();
                    Zone z = (Zone) vh.mapView.getTag();

                    // start set zone activity, for this current zone
                    Intent editZoneActivityIntent = new Intent(getActivity(), ZoneEditActivity.class);
                    editZoneActivityIntent.putExtra("zone", z);
                    getActivity().startActivityForResult(editZoneActivityIntent, REQUEST_CODE_EDIT_ZONE);
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

        int strokeColor = 0xffff0000; //red outline
        int shadeColor = 0x44ff0000; //opaque red fill

        CircleOptions circleOptions = new CircleOptions()
                .center(new LatLng(zone.lat, zone.lng))
                .radius(zone.radiusInMeters)
                .fillColor(shadeColor)
                .strokeColor(strokeColor)
                .strokeWidth(8);
        map.addCircle(circleOptions);

        // Add icon with percentage and name
        IconGenerator ic = new IconGenerator(getContext());
        // Choose colour
        double productivityPercentage = (double) Math.round(new Random().nextDouble() * 100d) / 100d; // TODO get Productivity percentage%
        int iconColor = IconGenerator.STYLE_DEFAULT;
        if(productivityPercentage > 0.7) {
            iconColor = IconGenerator.STYLE_GREEN;
        } else if (productivityPercentage < 0.3) {
            iconColor = IconGenerator.STYLE_RED;
        }
        ic.setStyle(iconColor);
        Marker m = map.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromBitmap(ic.makeIcon(productivityPercentage + "% : " + zone.name)))
                        .position(new LatLng(zone.lat, zone.lng))
        );

        m.showInfoWindow();



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
        TextView productivityPercentage;
        TextView appsToBlock;
        TextView keywords;

        Button editZone;

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
        dbAdapter.open(); // Open it for writing
        ArrayList<Zone> zones = dbAdapter.getAllZones();
        dbAdapter.close();
        return zones;
    }
}