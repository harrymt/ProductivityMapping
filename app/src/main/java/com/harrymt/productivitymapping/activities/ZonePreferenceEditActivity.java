package com.harrymt.productivitymapping.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import com.harrymt.productivitymapping.coredata.BlockedApps;
import com.harrymt.productivitymapping.PROJECT_GLOBALS;
import com.harrymt.productivitymapping.R;
import com.harrymt.productivitymapping.coredata.Zone;
import com.harrymt.productivitymapping.listviews.BlockedAppsArrayAdapter;
import com.harrymt.productivitymapping.utility.Util;
import java.util.ArrayList;

/**
 * Displays information about a zone to the user, including a list of apps
 * to block, the name of the zone, keywords and if they can auto-start-stop
 * when entering/exiting a geofence.
 */
public class ZonePreferenceEditActivity extends Activity {
    private static final String TAG = PROJECT_GLOBALS.LOG_NAME + "ZonePreferenceEditActivity";

    // List of apps a user can choose to block.
    ListView blockedAppsList;

    // Adapter that links the infor about each app to block.
    BlockedAppsArrayAdapter adapter;

    // Zone information.
    EditText etKeywords;
    EditText etName;

    /**
     * OnCreate of ZonePreferenceEditActivity, setup the existing zone information
     * and load the list of blocked apps.
     *
     * @param savedInstanceState saved state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zone_preference_edit);

        etKeywords = (EditText) findViewById(R.id.etKeywords);
        etName = (EditText) findViewById(R.id.etZoneName);

        // Get Zone object being passed in.
        Zone z = getIntent().getParcelableExtra("zone");

        // Set previous items
        etKeywords.setText(z.keywordsAsStr());
        etName.setText(z.name);

        // Get the previous list of selected items as a boolean array
        ArrayList<BlockedApps> values = BlockedApps.getListOfApps(getBaseContext());
        ArrayList<Boolean> itemChecked = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            itemChecked.add(i, false);
            for(String app : z.blockingApps) {
                if(values.get(i).package_name.equals(app)) {
                    itemChecked.set(i, true);
                }
            }
        }

        // Setup the list view.
        adapter = new BlockedAppsArrayAdapter(this, R.layout.list_apps_row, values, itemChecked);
        blockedAppsList = (ListView) findViewById(R.id.lvAppsToBlock);
        blockedAppsList.setAdapter(adapter);
        blockedAppsList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    }

    /**
     * OnClick btnSetZonePreferences
     *
     * When a user clicks on the set preferences button, save the zone and
     * bring the user back to the MainActivity via the ZoneEditActivity.
     *
     * @param view Button: btnSetZonePreferences
     */
    public void setZonePreferences(View view) {
        String[] packages = adapter.getSelectedItemsPackageNames();

        Intent data = new Intent();
        data.putExtra("keywords", Util.splitCSVStringToArray(etKeywords.getText().toString()));
        data.putExtra("packages", packages);
        data.putExtra("name", etName.getText().toString());

        setResult(RESULT_OK, data);
        finish(); // Leave
    }
}