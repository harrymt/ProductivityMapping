package com.harrymt.productivitymapping.database;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.harrymt.productivitymapping.NotificationParts;
import com.harrymt.productivitymapping.PROJECT_GLOBALS;
import com.harrymt.productivitymapping.Session;
import com.harrymt.productivitymapping.Zone;

import java.util.ArrayList;

import static com.harrymt.productivitymapping.database.DatabaseSchema.*;

/**
 * The adapter to the database.
 *
 * TODO It would be good to move to a contract and implement the base columns class.
 * https://developer.android.com/training/basics/data-storage/databases.html
 */
public class DatabaseAdapter
{

    private DatabaseHelper dbHelper;
    public SQLiteDatabase db;
    private Context context;

    public DatabaseAdapter(Context context) {
        this.context = context;

        // Open and prepare the database
        this.open();
    }

    private DatabaseAdapter open() throws SQLException {
        dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        if (dbHelper != null) {
            dbHelper.close();
        }
    }


    public void deleteZone(int id) {
        db.execSQL("DELETE FROM " + ZONE.TABLE
                + " WHERE " + ZONE.KEY.ID + "=" + id + ";");
    }

    /**
     * Gets the first zone found that is in given location.
     *
     * Uses Haversine formula
     * http://stackoverflow.com/a/123305/2235593
     *
     * @param loc Location to match with
     * @return Zone object that radius is in the location.
     */
    public Zone getZoneInLocation(Location loc) {
        ArrayList<Zone> zones = getAllZones();

        for (Zone z: zones) {
            double distance = distFrom(z.lat, z.lng, loc.getLatitude(), loc.getLongitude());
            if(distance < 1) { // select the first zone
                return z;
            }
        }
        
        return null;
    }

    /**
     *
     *
     * Note: has the 100 * at end!
     * @param lat1 Lat of Point 1
     * @param lng1 Lng of Point 1
     * @param lat2 Lat of Point 2
     * @param lng2 Lng of Point 2
     * @return The distance from point 1 and point 2
     */
    public static double distFrom(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371.0; // 3958.75 miles (or 6371.0 kilometers)
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);
        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return earthRadius * c * 100;
    }

    /**
     * Get all zones that have their synced flag as 0
     */
    public ArrayList<Zone> getAllZonesThatNeedToBeSynced()
    {
        Cursor c = db.query(ZONE.TABLE, new String[] {
                ZONE.KEY.ID,
                ZONE.KEY.NAME,
                ZONE.KEY.RADIUS,
                ZONE.KEY.LAT,
                ZONE.KEY.LNG,
                ZONE.KEY.HAS_SYNCED,
                ZONE.KEY.BLOCKING_APPS,
                ZONE.KEY.KEYWORDS
        }, ZONE.KEY.HAS_SYNCED + "=0", null, null, null, null);

        ArrayList<Zone> zones = new ArrayList<>();
        if(c.moveToFirst()) {
            do {
                int id = c.getInt(c.getColumnIndex(ZONE.KEY.ID));
                String name = c.getString(c.getColumnIndex(ZONE.KEY.NAME));
                float radius = c.getFloat(c.getColumnIndex(ZONE.KEY.RADIUS));
                double lat = c.getDouble(c.getColumnIndex(ZONE.KEY.LAT));
                double lng = c.getDouble(c.getColumnIndex(ZONE.KEY.LNG));
                int hasSynced = c.getInt(c.getColumnIndex(ZONE.KEY.HAS_SYNCED));
                String[] blockingApps = Zone.stringToArray(c.getString(c.getColumnIndex(ZONE.KEY.BLOCKING_APPS)));
                String[] keywords = Zone.stringToArray(c.getString(c.getColumnIndex(ZONE.KEY.KEYWORDS)));

                Zone z = new Zone(id, lat, lng, radius, name, -1, hasSynced, blockingApps, keywords);
                zones.add(z);
            } while (c.moveToNext());
        }
        c.close();

        return zones;
    }

    /**
     * Read all the Zones from the Zone Table database.
     */
    public ArrayList<Zone> getAllZones()
    {
        Cursor c = db.query(ZONE.TABLE, new String[] {
                ZONE.KEY.ID,
                ZONE.KEY.NAME,
                ZONE.KEY.RADIUS,
                ZONE.KEY.LAT,
                ZONE.KEY.LNG,
                ZONE.KEY.AUTO_START_STOP,
                ZONE.KEY.HAS_SYNCED,
                ZONE.KEY.BLOCKING_APPS,
                ZONE.KEY.KEYWORDS
        }, null, null, null, null, null);

        ArrayList<Zone> zones = new ArrayList<>();
        if(c.moveToFirst()) {
           do {
                int id = c.getInt(c.getColumnIndex(ZONE.KEY.ID));

                String name = c.getString(c.getColumnIndex(ZONE.KEY.NAME));
                float radius = c.getFloat(c.getColumnIndex(ZONE.KEY.RADIUS));
                double lat = c.getDouble(c.getColumnIndex(ZONE.KEY.LAT));
                double lng = c.getDouble(c.getColumnIndex(ZONE.KEY.LNG));
                int hasSynced = c.getInt(c.getColumnIndex(ZONE.KEY.HAS_SYNCED));
                int autoStart = c.getInt(c.getColumnIndex(ZONE.KEY.AUTO_START_STOP));
                String[] blockingApps = Zone.stringToArray(c.getString(c.getColumnIndex(ZONE.KEY.BLOCKING_APPS)));
                String[] keywords = Zone.stringToArray(c.getString(c.getColumnIndex(ZONE.KEY.KEYWORDS)));

                Zone z = new Zone(id, lat, lng, radius, name, autoStart, hasSynced, blockingApps, keywords);
                zones.add(z);
            } while (c.moveToNext());
        }
        c.close();

        return zones;
    }

    /**
     * Write a new zone (give) to the Zone Table database.
     * @param zone to write.
     */
    public void writeZone(Zone zone)
    {
        db.execSQL("INSERT INTO " + ZONE.TABLE + " ("
                + ZONE.KEY.NAME + ","
                + ZONE.KEY.RADIUS + ","
                + ZONE.KEY.LAT + ","
                + ZONE.KEY.LNG + ","
                + ZONE.KEY.AUTO_START_STOP + ","
                + ZONE.KEY.HAS_SYNCED + ","
                + ZONE.KEY.BLOCKING_APPS + ","
                + ZONE.KEY.KEYWORDS
                + ") "
                + "VALUES "
                + "('"
                + zone.name + "', "
                + zone.radiusInMeters + ", "
                + zone.lat + ", "
                + zone.lng + ", "
                + zone.autoStartStop + ", "
                + zone.hasSynced + ", "
                + "'" + zone.blockingAppsAsStr() + "', "
                + "'" + zone.keywordsAsStr()
                + "');");
    }

    /**
     * Edit a current zone in the Zone Table database, based on the ID of the zone object.
     * @param zone to edit.
     */
    public void editZone(Zone zone)
    {
        db.execSQL("UPDATE " + ZONE.TABLE
                + " SET "
                + ZONE.KEY.NAME + "= '" + zone.name + "', "
                + ZONE.KEY.RADIUS + "=" + zone.radiusInMeters + ", "
                + ZONE.KEY.LAT + "=" + zone.lat + ", "
                + ZONE.KEY.LNG + "=" + zone.lng + ", "
                + ZONE.KEY.AUTO_START_STOP + "=" + zone.autoStartStop + ", "
                + ZONE.KEY.HAS_SYNCED + "=" + zone.hasSynced + ", "
                + ZONE.KEY.BLOCKING_APPS + "='" + zone.blockingAppsAsStr() + "', "
                + ZONE.KEY.KEYWORDS + "='" + zone.keywordsAsStr() + "'"
                + " WHERE "
                + ZONE.KEY.ID + " = " + zone.zoneID
                + ";");
    }

    /**
     * Mark a zone as synced to the server.
     *
     * @param zone_id to mark as synced.
     */
    public void setZoneAsSynced(int zone_id)
    {
        db.execSQL("UPDATE " + ZONE.TABLE
                + " SET "
                + ZONE.KEY.HAS_SYNCED + "=" + 1
                + " WHERE "
                + ZONE.KEY.ID + " = " + zone_id
                + ";");
    }


    /**
     * Track the notification in a new Notification Table row,
     * based on the current session id.
     *
     * @param n The notification to save.
     */
    public void writeNotification(StatusBarNotification n)
    {
        // Save the notification based on the current session
        db.execSQL("INSERT INTO " + NOTIFICATION.TABLE + " ("
                + NOTIFICATION.KEY.PACKAGE + ","
                + NOTIFICATION.KEY.SENT_TO_USER + ","
                + NOTIFICATION.KEY.SESSION_ID
                + ") "
                + "VALUES "
                + "('" + n.getPackageName() + "', "
                + "0, "
                + PROJECT_GLOBALS.SESSION_ID + ");");
    }


    /**
     * Track the app usage by writing a new row to
     * the Notification Table based on the session id.
     *
     * @param packageName Name of app's package.
     * @param timeSpentInSeconds Length of time spent in app.
     */
    public void writeAppUsage(String packageName, long timeSpentInSeconds)
    {
        Log.d("g53ids", "Writing app usage!");

        // Save the app usage based on the current session
        db.execSQL("INSERT INTO " + APPUSAGE.TABLE + " ("
                + APPUSAGE.KEY.APP_PACKAGE_NAME + ", "
                + APPUSAGE.KEY.TIME_SPENT + ", "
                + APPUSAGE.KEY.SESSION_ID
                + ") "
                + "VALUES "
                + "('" + packageName + "', " + timeSpentInSeconds + ", " + PROJECT_GLOBALS.SESSION_ID + ");");
    }

    /**
     * Starts a new session by creating a new row in the Session Table,
     * and getting that new Session ID.
     */
    public void startNewSession(Integer zoneID, long startTime) {

        // Create a new Row in the Session Table
        db.execSQL("INSERT INTO " + SESSION.TABLE + " ("
                + SESSION.KEY.ZONE_ID + ","
                + SESSION.KEY.START_TIME + ""
                + ") "
                + "VALUES "
                + "(" + zoneID + ", " + startTime + ");");

        // Set the project state session ID
        PROJECT_GLOBALS.SESSION_ID = getLastSessionId();
    }

    /**
     * Mark a session end time.
     *
     * @param session_id ID of the session to mark as ended.
     */
    public void finishSession(Integer session_id) {
        long stop_time = System.currentTimeMillis() / 1000;

        db.execSQL("UPDATE " + SESSION.TABLE
                + " SET "
                + SESSION.KEY.STOP_TIME + "=" + stop_time
                + " WHERE "
                + SESSION.KEY.ID + " = " + session_id
                + ";");
    }

    private Integer getLastSessionId()
    {
        Integer sessionId = null;
        Cursor c = db.query(SESSION.TABLE, new String[]{SESSION.KEY.ID}, null, null, null, null, null);
        if(c.moveToLast()) {
            sessionId = c.getInt(0);
        }
        c.close();
        return sessionId;
    }


    public boolean hasASessionEverStartedYet()
    {
        boolean session_does_exist = false;
        Cursor c = db.query(SESSION.TABLE, new String[]{SESSION.KEY.ID}, null, null, null, null, null);
        if(c.moveToLast()) {
            session_does_exist = true;
        }
        c.close();
        return session_does_exist;
    }


    public Session getLastSessionDetails() {
        int session_id = getLastSessionId();
        Session session = new Session();

        Cursor c_session_details = db.query(SESSION.TABLE, new String[]{
                SESSION.KEY.ZONE_ID,
                SESSION.KEY.START_TIME,
                SESSION.KEY.STOP_TIME,
                SESSION.KEY.PRODUCTIVITY_PERCENTAGE
        }, SESSION.KEY.ID + "=" + session_id, null, null, null, null);


        if(c_session_details.moveToFirst()) {
            int zone_id = c_session_details.getInt(c_session_details.getColumnIndex(SESSION.KEY.ZONE_ID));
            int start_time = c_session_details.getInt(c_session_details.getColumnIndex(SESSION.KEY.START_TIME));
            int stop_time = c_session_details.getInt(c_session_details.getColumnIndex(SESSION.KEY.STOP_TIME));
            int productivity_percentage = c_session_details.getInt(c_session_details.getColumnIndex(SESSION.KEY.PRODUCTIVITY_PERCENTAGE));

            session = new Session(zone_id, start_time, stop_time, productivity_percentage);
        }
        c_session_details.close();

        return session;
    }

    public void setNotificationHasBeenSentToUser(int notificationID) {
        db.execSQL("UPDATE " + NOTIFICATION.TABLE
                + " SET "
                + NOTIFICATION.KEY.SENT_TO_USER + "=" + 1
                + " WHERE "
                + NOTIFICATION.KEY.ID + " = " + notificationID
                + ";");
    }

    public ArrayList<NotificationParts> getLastSessionNotificationDetails() {
        int session_id = getLastSessionId();
        ArrayList<NotificationParts> notifications = new ArrayList<>();

        Cursor c_session_notifications = db.query(NOTIFICATION.TABLE, new String[]{
                NOTIFICATION.KEY.ID,
                NOTIFICATION.KEY.PACKAGE
        }, NOTIFICATION.KEY.ID + "=" + session_id + " AND " + NOTIFICATION.KEY.SENT_TO_USER + "=0", null, null, null, null);

        if(c_session_notifications.moveToFirst()) {
            int notificationID = c_session_notifications.getInt(c_session_notifications.getColumnIndex(NOTIFICATION.KEY.ID));
            String package_name = c_session_notifications.getString(c_session_notifications.getColumnIndex(NOTIFICATION.KEY.PACKAGE));
            notifications.add(new NotificationParts(notificationID, package_name));
        }
        c_session_notifications.close();

        return notifications;
    }

    public Zone getZoneFromID(int zoneId) {
        Zone z = null;

        Cursor c = db.query(ZONE.TABLE, new String[] {
                ZONE.KEY.ID,
                ZONE.KEY.NAME,
                ZONE.KEY.RADIUS,
                ZONE.KEY.LAT,
                ZONE.KEY.LNG,
                ZONE.KEY.AUTO_START_STOP,
                ZONE.KEY.HAS_SYNCED,
                ZONE.KEY.BLOCKING_APPS,
                ZONE.KEY.KEYWORDS
        }, ZONE.KEY.ID + "=" + zoneId, null, null, null, null);

        if(c.moveToFirst()) {
            int id = c.getInt(c.getColumnIndex(ZONE.KEY.ID));
            String name = c.getString(c.getColumnIndex(ZONE.KEY.NAME));
            float radius = c.getFloat(c.getColumnIndex(ZONE.KEY.RADIUS));
            double lat = c.getDouble(c.getColumnIndex(ZONE.KEY.LAT));
            double lng = c.getDouble(c.getColumnIndex(ZONE.KEY.LNG));
            int hasSynced = c.getInt(c.getColumnIndex(ZONE.KEY.HAS_SYNCED));
            int autoStart = c.getInt(c.getColumnIndex(ZONE.KEY.AUTO_START_STOP));
            String[] blockingApps = Zone.stringToArray(c.getString(c.getColumnIndex(ZONE.KEY.BLOCKING_APPS)));
            String[] keywords = Zone.stringToArray(c.getString(c.getColumnIndex(ZONE.KEY.KEYWORDS)));

            z = new Zone(id, lat, lng, radius, name, autoStart, hasSynced, blockingApps, keywords);
        }

        c.close();

        return z;
    }


    /** --- STATS --- **/
    public int getUniqueNumberOfBlockingApps() {
        return 0;
    }

    public int getUniqueNumberOfKeywords() {
        return 0;
    }

    public long getNumberOfZones() {
        return  DatabaseUtils.queryNumEntries(db, ZONE.TABLE);
    }

    public String getMostPopularKeyword() {
        return "Harry";
    }

    public String getMostBlockedApp() {
        return "Facebook";
    }
}