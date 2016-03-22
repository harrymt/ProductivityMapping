package com.harrymt.productivitymapping.listviews;

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
import com.harrymt.productivitymapping.PROJECT_GLOBALS;
import com.harrymt.productivitymapping.R;
import com.harrymt.productivitymapping.activities.MainActivity;
import com.harrymt.productivitymapping.coredata.Zone;
import com.harrymt.productivitymapping.activities.ZoneEditActivity;
import com.harrymt.productivitymapping.database.DatabaseAdapter;
import com.harrymt.productivitymapping.fragments.ZonesFragment;
import java.util.HashSet;

/**
 * Adapter that displays a text box and a Google Map for each item.
 */
public class MapAdapter extends ArrayAdapter<Zone> implements View.OnClickListener {
    private static final String TAG = PROJECT_GLOBALS.LOG_NAME + "MapAdapter";

    // Context back to MainActivity
    private final MainActivity mainActivity;

    // Reference to the parent fragment
    private ZonesFragment zonesFragment;

    // List of all map views.
    private final HashSet<MapView> mMaps = new HashSet<>();

    /**
     * Constructor.
     *
     * @param zonesFragment reference to parent fragment.
     * @param context Context of app.
     * @param locations List of information (zones) for each item.
     */
    public MapAdapter(ZonesFragment zonesFragment, Context context, MainActivity activity, Zone[] locations) {
        super(context, R.layout.list_map_row, R.id.tvZoneName, locations);
        this.zonesFragment = zonesFragment;
        this.mainActivity = activity;
    }

    /**
     * Gets the view at the current position. Recycle the view if its already
     * been created, if not, create it.
     *
     * @param position row position.
     * @param convertView View of current item.
     * @param parent Parent view (list view).
     * @return The view at the position.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        MapViewHolder holder;

        // Check if a view can be reused, otherwise inflate a layout and set up the view holder
        if (row == null) {
            // Inflate view from layout file
            row = zonesFragment.getActivity().getLayoutInflater().inflate(R.layout.list_map_row, null);

            // Set up holder and assign it to the View
            holder = new MapViewHolder(zonesFragment);
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
            holder = (MapViewHolder) row.getTag();
        }

        // Get the zone and attach it to the MapView
        Zone zone = getItem(position);
        holder.mapView.setTag(zone);

        // Make sure we init the map!
        if (holder.map != null) {
            // The map is already ready to be used
            zonesFragment.setMapLocation(holder.map, zone);
        }

        // Display the zone name
        holder.name.setText(zone.name);

        // Display the list of apps and keywords set.
        String apps = ""; for(String app : zone.blockingApps) { apps += app + ", "; }
        String keywords = ""; for(String word : zone.keywords) { keywords += word + ", "; }
        holder.appsToBlock.setText("Apps blocked: " + (zone.blockingApps.length == 0 ? "none" : apps));
        holder.keywords.setText("Keywords set: " + (zone.keywords.length == 0 ? "none" : keywords));

        // Setup on click for edit zone button
        holder.editZone.setOnClickListener(new View.OnClickListener() {

            /**
             * OnClick btnEditZone
             *
             * Open the ZoneEditActivity with the current zone item, so the user
             * can edit the zone.
             *
             * @param v Button: btnEditZone
             */
            @Override
            public void onClick(View v) {
                // Get the zone object, saved in the tag!
                MapViewHolder vh = (MapViewHolder) ((View) v.getParent().getParent()).getTag();
                Zone z = (Zone) vh.mapView.getTag();

                // Start set zone activity, for this current zone
                Intent editZoneActivityIntent = new Intent(zonesFragment.getActivity(), ZoneEditActivity.class);
                editZoneActivityIntent.putExtra("zone", z);
                zonesFragment.getActivity().startActivityForResult(editZoneActivityIntent, PROJECT_GLOBALS.REQUEST_CODE_EDIT_ZONE);
            }
        });

        // Setup on click for edit zone button
        holder.deleteZone.setOnClickListener(this);

        return row;
    }

    /**
     * OnClick btnDeleteZone
     *
     * Open a dialog box so the user can choose if they want to delete a zone or not.
     *
     * @param v Button: btnDeleteZone
     */
    @Override
    public void onClick(View v) {
        // Get the zone object, saved in the tag!
        MapViewHolder vh = (MapViewHolder) ((View) v.getParent().getParent()).getTag();
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

                        // Reload all data sources
                        mainActivity.refreshAllFragmentDatasources();

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
}
