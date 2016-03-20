package com.harrymt.productivitymapping;

import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.harrymt.productivitymapping.fragments.ZonesFragment;

/**
 * Holder for Views used in the
 * Once the  the <code>map</code> field is set, otherwise it is null.
 * When the {@link #onMapReady(com.google.android.gms.maps.GoogleMap)} callback is received and
 * the {@link com.google.android.gms.maps.GoogleMap} is ready, it stored in the {@link #map}
 * field. The map is then initialised with the NamedLocation that is stored as the tag of the
 * MapView. This ensures that the map is initialised with the latest data that it should
 * display.
 */
public class ViewHolder implements OnMapReadyCallback {

    private ZonesFragment zonesFragment;
    public MapView mapView;

    public TextView name;
    public TextView appsToBlock;
    public TextView keywords;

    public Button editZone;
    public Button deleteZone;

    public GoogleMap map;

    public ViewHolder(ZonesFragment zonesFragment) {
        this.zonesFragment = zonesFragment;
    }

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
