package com.harrymt.productivitymapping.activities;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.harrymt.productivitymapping.NotificationBuilderUtil;
import com.harrymt.productivitymapping.NotificationParts;
import com.harrymt.productivitymapping.PROJECT_GLOBALS;
import com.harrymt.productivitymapping.R;
import com.harrymt.productivitymapping.Session;
import com.harrymt.productivitymapping.database.DatabaseAdapter;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LastSession extends Activity {

    ArrayList<NotificationParts> ns;
    Session s;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_last_session);
        this.setTitle("Last Session");


        TextView tvAppUsage = (TextView) findViewById(R.id.tvAppUsage);
        tvAppUsage.setText("App usage not available at this time");

        displayStats();
    }

    public void sendAllBlockedNotifications(View view) {
        Toast.makeText(this, ns.size() + " notifications to send", Toast.LENGTH_SHORT).show();
        NotificationBuilderUtil builder = new NotificationBuilderUtil(this);

        DatabaseAdapter dbAdapter = new DatabaseAdapter(this); // Open and prepare the database

        for (NotificationParts sbn : ns) {
            dbAdapter.setNotificationHasBeenSentToUser(sbn.id);
            builder.postNotification(sbn);
        }

        dbAdapter.close();

        // Wipe the notification array;
        ns = new ArrayList<>();
    }

    public void displayStats() {
        DatabaseAdapter dbAdapter = new DatabaseAdapter(this); // Open and prepare the database
        s = dbAdapter.getLastSessionDetails();
        ns = dbAdapter.getLastSessionNotificationDetails();
        Zone zone = dbAdapter.getZoneFromID(s.zoneId);
        dbAdapter.close();

        TextView tvAppUsage = (TextView) findViewById(R.id.tvSessionGeneral);

        String sessionLength = convertTimeToFriendlyString(s.stopTime - s.startTime);
        String numberOfNotificationsBlocked = ns.size() + "";
        String zoneKeywords = (zone == null ? null : zone.keywordsAsStr());

        String sessionStr = "Session lasted " + sessionLength + ",";
        String notificationStr = " blocking " + numberOfNotificationsBlocked + " notification(s)";
        String keywordsStr = (zoneKeywords == null ? "." : "letting through notification(s) because of the following keywords. " + zoneKeywords);
        tvAppUsage.setText(sessionStr + notificationStr + keywordsStr);

        Map<String, Integer> notifications = new HashMap<>();

        for(NotificationParts n : ns) {
            if(notifications.containsKey(n.packageName)) {
                Integer number = notifications.get(n.packageName);
                number++;
                notifications.remove(n.packageName);
                notifications.put(n.packageName, number);
            } else {
                notifications.put(n.packageName, 1);
            }
        }
        String appsStr = "";
        for (Map.Entry<String, Integer> entry : notifications.entrySet()) {
            appsStr += entry.getKey() + ": " + entry.getValue();
        }

        TextView tvNotificationsBlocked = (TextView) findViewById(R.id.tvNotificationsBlocked);
        tvNotificationsBlocked.setText(appsStr);
    }

    private String convertTimeToFriendlyString(long epochTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.UK);
        return sdf.format(new Date(epochTime));
    }
}
