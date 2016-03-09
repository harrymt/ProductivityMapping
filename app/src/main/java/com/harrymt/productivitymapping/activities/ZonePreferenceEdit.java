package com.harrymt.productivitymapping.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.harrymt.productivitymapping.PROJECT_GLOBALS;
import com.harrymt.productivitymapping.R;
import com.harrymt.productivitymapping.Zone;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ZonePreferenceEdit extends Activity {

    ListView blockedAppsList;
    ArrayList<Boolean> itemChecked = new ArrayList<Boolean>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zone_preference_edit);

        // Get Zone object being passed in.
        Zone z = getIntent().getParcelableExtra("zone");

        EditText keywords = (EditText) findViewById(R.id.etKeywords);
        keywords.setText(z.keywordsAsStr());
        EditText name = (EditText) findViewById(R.id.etZoneName);
        name.setText(z.name);

        CheckBox autoStartStop = (CheckBox) findViewById(R.id.cbAutoStartStop);
        autoStartStop.setChecked(z.autoStartStop == 1);

        this.setTitle("Set zone preferences");

        final List<BlockedApps> values = getListOfApps(); // put values in this
        blockedAppsList = (ListView) findViewById(R.id.lvAppsToBlock);

        for (int i = 0; i < values.size(); i++) {
            itemChecked.add(i, false);
            for(String app : z.blockingApps) {
                if(values.get(i).package_name.equals(app)) {
                    itemChecked.set(i, true);
                }
            }
        }

        final ArrayAdapter<BlockedApps> adapter = new ArrayAdapter<BlockedApps>(this, R.layout.list_apps_row, R.id.tvAppName, values) {

            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                View row = convertView;
                BlockedAppsViewHolder holder;

                if(row == null) {

                    // Inflate for each row
                    row = getLayoutInflater().inflate(R.layout.list_apps_row, null);

                    // View Holder Object to contain row elements
                    holder = new BlockedAppsViewHolder();
                    holder.name = (TextView) row.findViewById(R.id.tvAppName);
                    holder.package_name = (TextView) row.findViewById(R.id.tvPackageName);
                    holder.isSelected = (CheckBox) row.findViewById(R.id.cbAppSelected);

                    holder.isSelected.setTag(position);

                    // Set holder with LayoutInflater
                    row.setTag(holder);

                } else {
                    holder = (BlockedAppsViewHolder) row.getTag();
                }

                if(values.size() <= 0) {
                    holder.name.setText("No apps found");
                } else {
                    // Get each Model object from Arraylist
                    BlockedApps app = getItem( position );

                    // Set holder
                    holder.name.setText(app.name + (app.isPopular ? " popular" : ""));
                    holder.package_name.setText(app.package_name);

                    holder.isSelected.setOnClickListener(new View.OnClickListener() {

                        public void onClick(View v) {

                            CheckBox cb = (CheckBox) v.findViewById(R.id.cbAppSelected);

                            if (cb.isChecked()) {
                                itemChecked.set(position, true);
                            } else {
                                itemChecked.set(position, false);
                            }
                        }
                    });
                    holder.isSelected.setChecked(itemChecked.get(position)); // this will Check or Uncheck the
                }
                return row;
            }
        };

        blockedAppsList.setAdapter(adapter);
        blockedAppsList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    }

    public static class BlockedAppsViewHolder {
        public TextView name;
        public TextView package_name;
        public CheckBox isSelected;
    }

    // Send data back in an intent
    public void setZonePreferences(View view) {
        EditText keywords = (EditText) findViewById(R.id.etKeywords);
        EditText name = (EditText) findViewById(R.id.etZoneName);
        CheckBox autoStartStop = (CheckBox) findViewById(R.id.cbAutoStartStop);

        // Get the package names of the apps the user has selected
        ArrayList<String> packages = new ArrayList<>();
        for(int i = 0; i < itemChecked.size(); i++) {
            if(itemChecked.get(i)) { // if this item is checked
                BlockedApps app = (BlockedApps) blockedAppsList.getItemAtPosition(i);
                packages.add(app.package_name);
            }
        }

        Intent data = new Intent();
        data.putExtra("keywords", convertCSVToStringArray(keywords.getText().toString()));
        data.putExtra("packages", packages.toArray(new String[packages.size()]));
        data.putExtra("name", name.getText().toString());
        data.putExtra("autoStartStop", autoStartStop.isChecked());

        setResult(RESULT_OK, data);
        finish(); // Leave
    }

    public String[] convertCSVToStringArray(String str) {
        if (str.length() == 0) return new String[] {};
        return str.split(",", -1);
    }

    class BlockedApps {
        public String name;
        public String package_name;
        public Drawable icon;
        public boolean isPopular;

        public BlockedApps(String n, String pn, Drawable i, boolean p) {
            name = n; package_name = pn; icon = i; isPopular = p;
        }
    }

    private ArrayList<BlockedApps> getListOfApps() {
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        final List<ResolveInfo> pkgAppsList = getBaseContext().getPackageManager().queryIntentActivities( mainIntent, 0);
        ArrayList<BlockedApps> o = new ArrayList<>();
        boolean popularApp;

        ArrayList<BlockedApps> popularApps = new ArrayList<>();

        for (ResolveInfo info: pkgAppsList) {
            popularApp = false;

            Drawable icon = getBaseContext().getPackageManager().getApplicationIcon(info.activityInfo.applicationInfo);
            final String title 	= getBaseContext().getPackageManager().getApplicationLabel(info.activityInfo.applicationInfo).toString();

            for(String app : PROJECT_GLOBALS.TOP_APPS_BLOCKED) {
                if(app.equals(info.activityInfo.packageName)) {
                    popularApp = true;
                }
            }

            if(popularApp) {
                popularApps.add(new BlockedApps(title, info.activityInfo.packageName, icon, true));
            } else {
                o.add(new BlockedApps(title, info.activityInfo.packageName, icon, false));
            }
        }

        // Put popular apps at the start
        o.addAll(0, popularApps);

        return o;
    }


}