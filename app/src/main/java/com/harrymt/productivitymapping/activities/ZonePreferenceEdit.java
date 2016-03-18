package com.harrymt.productivitymapping.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

import com.harrymt.productivitymapping.BlockedApps;
import com.harrymt.productivitymapping.R;
import com.harrymt.productivitymapping.Zone;
import com.harrymt.productivitymapping.adapters.BlockedAppsArrayAdapter;

import java.util.ArrayList;

public class ZonePreferenceEdit extends Activity {

    ListView blockedAppsList;
    BlockedAppsArrayAdapter adapter;

    EditText etKeywords;
    EditText etName;
    CheckBox cbAutoStartStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zone_preference_edit);

        etKeywords = (EditText) findViewById(R.id.etKeywords);
        etName = (EditText) findViewById(R.id.etZoneName);
        cbAutoStartStop = (CheckBox) findViewById(R.id.cbAutoStartStop);

        // Get Zone object being passed in.
        Zone z = getIntent().getParcelableExtra("zone");

        // Set previous items
        etKeywords.setText(z.keywordsAsStr());
        etName.setText(z.name);
        cbAutoStartStop.setChecked(z.autoStartStop == 1);

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

        // Setup the list view
        adapter = new BlockedAppsArrayAdapter(this, R.layout.list_apps_row, values, itemChecked);
        blockedAppsList = (ListView) findViewById(R.id.lvAppsToBlock);
        blockedAppsList.setAdapter(adapter);
        blockedAppsList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    }


    /**
     * On set preferences click.
     *
     * @param view Set Zone preferences button.
     */
    public void setZonePreferences(View view) {
        String[] packages = adapter.getSelectedItemsPackageNames();

        Intent data = new Intent();
        data.putExtra("keywords", getKeywords());
        data.putExtra("packages", packages);
        data.putExtra("name", etName.getText().toString());
        data.putExtra("autoStartStop", cbAutoStartStop.isChecked());

        setResult(RESULT_OK, data);
        finish(); // Leave
    }

    /**
     * Get the keywords from the textbox.
     *
     * @return Keywords array.
     */
    private String[] getKeywords() {
        String keywords = etKeywords.getText().toString();
        if (keywords.length() == 0) return new String[] {};
        return keywords.split(",", -1);
    }

}