package com.harrymt.productivitymapping.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.MapView;
import com.harrymt.productivitymapping.R;
import com.harrymt.productivitymapping.ViewHolder;
import com.harrymt.productivitymapping.Zone;
import com.harrymt.productivitymapping.activities.ZoneEditActivity;
import com.harrymt.productivitymapping.database.DatabaseAdapter;
import com.harrymt.productivitymapping.fragments.ZonesFragment;

import java.util.HashSet;

/**
 * Adapter that displays a name and {@link com.google.android.gms.maps.MapView} for each item.
 * The layout is defined in <code>lite_list_demo_row.xml</code>. It contains a MapView
 * that is programatically initialised in
 * {@link #getView(int, android.view.View, android.view.ViewGroup)}
 */
public class MapAdapter extends ArrayAdapter<Zone> {

    private ZonesFragment zonesFragment;
    private final HashSet<MapView> mMaps = new HashSet<>();

    int REQUEST_CODE_EDIT_ZONE = 3;


    public MapAdapter(ZonesFragment zonesFragment, Context context, Zone[] locations) {
        super(context, R.layout.list_map_row, R.id.tvZoneName, locations);
        this.zonesFragment = zonesFragment;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder;

        // Check if a view can be reused, otherwise inflate a layout and set up the view holder
        if (row == null) {
            // Inflate view from layout file
            row = zonesFragment.getActivity().getLayoutInflater().inflate(R.layout.list_map_row, null);

            // Set up holder and assign it to the View
            holder = new ViewHolder(zonesFragment);
            holder.mapView = (MapView) row.findViewById(R.id.lite_listrow_map);
            holder.name = (TextView) row.findViewById(R.id.tvZoneName);
            holder.appsToBlock = (TextView) row.findViewById(R.id.tvZoneAppsToBlock);
            holder.keywords = (TextView) row.findViewById(R.id.tvZoneKeywords);
            holder.editZone = (Button) row.findViewById(R.id.btnEditZone);
            holder.deleteZone = (Button) row.findViewById(R.id.btnDeleteZone);

            holder.mapView.setClickable(false);

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
            zonesFragment.setMapLocation(holder.map, zone);
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
                Intent editZoneActivityIntent = new Intent(zonesFragment.getActivity(), ZoneEditActivity.class);
                editZoneActivityIntent.putExtra("zone", z);
                zonesFragment.getActivity().startActivityForResult(editZoneActivityIntent, REQUEST_CODE_EDIT_ZONE);
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
                                zonesFragment.loadZonesToListView();

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
}
