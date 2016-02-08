package com.harrymt.productivitymapping;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class ZoneEditActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zone_edit);
    }

    public void setZoneCoords(View view) {
        int RESULT_CODE = 123;
        Intent zoneCoordinates = new Intent();
        setResult(RESULT_CODE, zoneCoordinates);
        finish();
    }
}
