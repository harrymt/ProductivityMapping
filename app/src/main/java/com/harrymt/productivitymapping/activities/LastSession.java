package com.harrymt.productivitymapping.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.harrymt.productivitymapping.R;

public class LastSession extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_last_session);
        this.setTitle("Last Session");

        TextView tvNotificationsBlocked = (TextView) findViewById(R.id.tvNotificationsBlocked);
        tvNotificationsBlocked.setText("10 Facebook \n5 WhatsApp");

        TextView tvAppUsage = (TextView) findViewById(R.id.tvAppUsage);
        tvAppUsage.setText("9 Minutes on Facebook\n15 Minutes on WhatsApp");
    }

    public void sendAllBlockedNotifications(View view) {
        Toast.makeText(this, "0 notifications to send", Toast.LENGTH_SHORT).show();
    }

}
