package com.harrymt.productivitymapping;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class ZonePreferenceEdit extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zone_preference_edit);
    }

    // Send data back in an intent
    public void setZonePreferences(View view) {
        EditText packages = (EditText) findViewById(R.id.etPackage);
        EditText keywords = (EditText) findViewById(R.id.etKeywords);

        Intent data = new Intent();
        data.putExtra("keywords", keywords.getText().toString());
        data.putExtra("packages", packages.getText().toString());
        setResult(RESULT_OK, data);
        finish(); // Leave
    }
}
