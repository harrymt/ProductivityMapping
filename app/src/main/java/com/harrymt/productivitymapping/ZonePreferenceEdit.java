package com.harrymt.productivitymapping;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ZonePreferenceEdit extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zone_preference_edit);

        // Get Zone object being passed in.
        Zone z = getIntent().getParcelableExtra("zone");

        EditText packages = (EditText) findViewById(R.id.etPackage);
        packages.setText(z.blockingAppsAsStr());
        EditText keywords = (EditText) findViewById(R.id.etKeywords);
        keywords.setText(z.keywordsAsStr());
        EditText name = (EditText) findViewById(R.id.etZoneName);
        name.setText(z.name);


        this.setTitle("Set zone preferences");
    }

    // Send data back in an intent
    public void setZonePreferences(View view) {
        EditText packages = (EditText) findViewById(R.id.etPackage);
        EditText keywords = (EditText) findViewById(R.id.etKeywords);
        EditText name = (EditText) findViewById(R.id.etZoneName);

        Intent data = new Intent();
        data.putExtra("keywords", convertCSVToStringArray(keywords.getText().toString()));
        data.putExtra("packages", convertCSVToStringArray(packages.getText().toString()));
        data.putExtra("name", name.getText().toString());
        setResult(RESULT_OK, data);
        finish(); // Leave
    }

    public String[] convertCSVToStringArray(String str) {
        if (str.length() == 0) return new String[] {};
        return str.split(",", -1);
    }

}