package com.harrymt.productivitymapping.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.harrymt.productivitymapping.PROJECT_GLOBALS;
import com.harrymt.productivitymapping.utility.NotificationUtil;
import com.harrymt.productivitymapping.coredata.NotificationParts;
import com.harrymt.productivitymapping.R;
import com.harrymt.productivitymapping.coredata.Session;
import com.harrymt.productivitymapping.coredata.Zone;
import com.harrymt.productivitymapping.database.DatabaseAdapter;
import com.harrymt.productivitymapping.utility.Util;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Show information to the user about their last study session.
 */
public class LastSession extends Activity {
    private static final String TAG = PROJECT_GLOBALS.LOG_NAME + "LastSession";

    // List of notifications
    ArrayList<NotificationParts> ns;

    // Session information.
    Session s;

    /**
     * On Activity create, display stats to the user.
     *
     * @param savedInstanceState saved state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_last_session);

        displayStats();
    }

    /**
     * OnClick btnSendAllNotifications
     *
     * Send all blocked notifications during a study session, back to the user.
     *
     * @param view Button: btnSendAllNotifications
     */
    public void sendAllBlockedNotifications(View view) {
        Toast.makeText(this, ns.size() + " notifications to send.", Toast.LENGTH_SHORT).show();

        // Build the notification, send it, then mark it as sent in the database.
        NotificationUtil builder = new NotificationUtil(this);
        DatabaseAdapter dbAdapter = new DatabaseAdapter(this); // Open and prepare the database
        for (NotificationParts sbn : ns) {
            dbAdapter.setNotificationHasBeenSentToUser(sbn.id);
            builder.postNotification(sbn);
        }
        dbAdapter.close();

        // Wipe the notification array;
        ns = new ArrayList<>();
    }

    /**
     * Display details about the last session zone.
     *
     * @param zone Zone object from last session.
     */
    private void setZoneDetails(Zone zone) {
        String sessionLength = Util.convertTimeToFriendlyString(s.stopTime - s.startTime);
        String numberOfNotificationsBlocked = ns.size() + "";
        String zoneKeywords = (zone == null ? null : zone.keywordsAsStr());

        String sessionStr = "Session lasted " + sessionLength + ",";
        String notificationStr = " blocking " + numberOfNotificationsBlocked + " notification(s)";
        String keywordsStr = (zoneKeywords == null ? "." : "letting through notification(s) because of the following keywords. " + zoneKeywords);

        TextView tvSessionGeneral = (TextView) findViewById(R.id.tvSessionGeneral);
        tvSessionGeneral.setText(sessionStr + notificationStr + keywordsStr);
    }

    /**
     * Display last session stats to user.
     */
    public void displayStats() {
        // Load information from database.
        DatabaseAdapter dbAdapter = new DatabaseAdapter(this); // Open and prepare the database
        s = dbAdapter.getLastSessionDetails();
        ns = dbAdapter.getLastSessionNotificationDetails();
        Zone zone = dbAdapter.getZoneFromID(s.zoneId);
        setZoneDetails(zone);
        dbAdapter.close();

        Map<String, Integer> notifications = sortNotificationPartsToMap();
        String appsStr = "";
        for (Map.Entry<String, Integer> entry : notifications.entrySet()) {
            appsStr += entry.getKey() + ": " + entry.getValue();
        }

        TextView tvNotificationsBlocked = (TextView) findViewById(R.id.tvNotificationsBlocked);
        tvNotificationsBlocked.setText(appsStr);
    }

    /**
     * Sort ns array to Map.
     * @return Map with the notification package name and number of times we blocked it.
     */
    private Map<String, Integer> sortNotificationPartsToMap() {
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

        return notifications;
    }
}
