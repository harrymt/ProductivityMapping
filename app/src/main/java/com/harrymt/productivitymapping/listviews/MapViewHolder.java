package com.harrymt.productivitymapping.listviews;

import android.widget.Button;
import android.widget.TextView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.harrymt.productivitymapping.PROJECT_GLOBALS;
import com.harrymt.productivitymapping.coredata.Zone;
import com.harrymt.productivitymapping.fragments.ZonesFragment;

/**
 * View holder for the zones list of zones. Holds a Google map, and information about a
 * zone.
 */
public class MapViewHolder implements OnMapReadyCallback {
    private static final String TAG = PROJECT_GLOBALS.LOG_NAME + "MapViewHolder";

    // Reference to the parent fragment.
    private ZonesFragment zonesFragment;

    // Map view around the google map
    public MapView mapView;

    // Information about the zone.
    public TextView name;
    public TextView appsToBlock;
    public TextView keywords;

    // Interact with zone.
    public Button editZone;
    public Button deleteZone;

    // The Google map
    public GoogleMap map;

    /**
     * Constructor.
     *
     * @param zonesFragment Parent fragment.
     */
    public MapViewHolder(ZonesFragment zonesFragment) {
        this.zonesFragment = zonesFragment;
    }

    /**
     * When the google map is ready to be displayed.
     *
     * @param googleMap map reference.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(zonesFragment.getContext());
        map = googleMap;
        Zone data = (Zone) mapView.getTag();
        if (data != null) {
            zonesFragment.setMapLocation(map, data);
        }
    }

    /**
     * Setup the map view.
     */
    public void initializeMapView() {
        if (mapView != null) {
            mapView.onCreate(null);
            mapView.getMapAsync(this);
        }
    }
}