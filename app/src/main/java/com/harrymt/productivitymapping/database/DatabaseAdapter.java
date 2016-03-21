package com.harrymt.productivitymapping.database;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import com.harrymt.productivitymapping.coredata.NotificationParts;
import com.harrymt.productivitymapping.PROJECT_GLOBALS;
import com.harrymt.productivitymapping.coredata.Session;
import com.harrymt.productivitymapping.coredata.Zone;
import com.harrymt.productivitymapping.utility.MapUtil;
import com.harrymt.productivitymapping.utility.Util;

import java.util.ArrayList;
import java.util.Map;
import static com.harrymt.productivitymapping.database.DatabaseSchema.*;

/**
 * The adapter that interacts with the database.
 */
public class DatabaseAdapter
{
    private static final String TAG = PROJECT_GLOBALS.LOG_NAME + "DatabaseAdapter";

    // Helper methods.
    private DatabaseHelper dbHelper;

    // Reference to the actual database.
    public SQLiteDatabase db;

    // Context of the app.
    private Context context;

    /**
     * Constructor.
     * Opens and prepares the database.
     *
     * @param context Context of app.
     */
    public DatabaseAdapter(Context context) {
        this.context = context;

        // Open and prepare the database
        this.open();
    }

    /**
     * Opens the database for writing/reading.
     *
     * @return a reference to the database adapter.
     * @throws SQLException If we cannot open the database.
     */
    private DatabaseAdapter open() throws SQLException {
        dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();
        return this;
    }

    /**
     * Closes the database for cleanup.
     */
    public void close() {
        if (dbHelper != null) {
            dbHelper.close();
        }
    }


    //
    //
    // SELECT
    //
    //


    /**
     * Gets the first zone found that is in given location.
     *
     * Uses Haversine formula
     * http://stackoverflow.com/a/123305/2235593
     *
     * @param loc Location to match with.
     * @return Zone object that radius is in the location.
     */
    public Zone getZoneInLocation(Location loc) {
        ArrayList<Zone> zones = getAllZones();

        for (Zone z: zones) {
            double distance = MapUtil.distFrom(z.lat, z.lng, loc.getLatitude(), loc.getLongitude());
            if(distance < 1) { // select the first zone
                return z;
            }
        }

        return null;
    }


    /**
     * Get all zones that have their synced flag as 0.
     *
     * @return Array list of zones to be synced.
     */
    public ArrayList<Zone> getAllZonesThatNeedToBeSynced() {
        Cursor c = db.query(ZONE.TABLE, new String[]{
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
                String[] blockingApps = Util.stringToArray(c.getString(c.getColumnIndex(ZONE.KEY.BLOCKING_APPS)));
                String[] keywords = Util.stringToArray(c.getString(c.getColumnIndex(ZONE.KEY.KEYWORDS)));

                Zone z = new Zone(id, lat, lng, radius, name, hasSynced, blockingApps, keywords);
                zones.add(z);
            } while (c.moveToNext());
        }
        c.close();

        return zones;
    }

    /**
     * Read all the Zones from the Zone Table database.
     *
     * @return ArrayList of zones.
     */
    public ArrayList<Zone> getAllZones()
    {
        Cursor c = db.query(ZONE.TABLE, new String[]{
                ZONE.KEY.ID,
                ZONE.KEY.NAME,
                ZONE.KEY.RADIUS,
                ZONE.KEY.LAT,
                ZONE.KEY.LNG,
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
                String[] blockingApps = Util.stringToArray(c.getString(c.getColumnIndex(ZONE.KEY.BLOCKING_APPS)));
                String[] keywords = Util.stringToArray(c.getString(c.getColumnIndex(ZONE.KEY.KEYWORDS)));

                Zone z = new Zone(id, lat, lng, radius, name, hasSynced, blockingApps, keywords);
                zones.add(z);
            } while (c.moveToNext());
        }
        c.close();

        return zones;
    }

    /**
     * Get all the keywords from every zone.
     *
     * @return Array list of string array of keywords.
     */
    public ArrayList<String[]> getAllKeywords()
    {
        Cursor c = db.query(ZONE.TABLE, new String[]{
                ZONE.KEY.KEYWORDS
        }, null, null, null, null, null);

        ArrayList<String[]> array_of_keywords = new ArrayList<>();
        if(c.moveToFirst()) {
            do {
                String[] keywords = Util.stringToArray(c.getString(c.getColumnIndex(ZONE.KEY.KEYWORDS)));
                array_of_keywords.add(keywords);
            } while (c.moveToNext());
        }
        c.close();

        return array_of_keywords;
    }


    /**
     * Get all the apps from every zone.
     *
     * @return Array list of string array of apps.
     */
    public ArrayList<String[]> getAllBlockingApps()
    {
        Cursor c = db.query(ZONE.TABLE, new String[]{
                ZONE.KEY.BLOCKING_APPS
        }, null, null, null, null, null);

        ArrayList<String[]> array_of_apps = new ArrayList<>();
        if(c.moveToFirst()) {
            do {
                String[] apps = Util.stringToArray(c.getString(c.getColumnIndex(ZONE.KEY.BLOCKING_APPS)));
                array_of_apps.add(apps);
            } while (c.moveToNext());
        }
        c.close();

        return array_of_apps;
    }


    /**
     * Get the id of the last session created.
     *
     * @return ID of last session.
     */
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


    /**
     * Check to see if a session has ever existed yet.
     *
     * @return True if it has, false if it hasn't.
     */
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

    /**
     * Get information on the last session.
     *
     * @return Session object containing information about the last
     * session.
     */
    public Session getLastSessionDetails() {
        int session_id = getLastSessionId();
        Session session = new Session();

        Cursor c_session_details = db.query(SESSION.TABLE, new String[]{
                SESSION.KEY.ZONE_ID,
                SESSION.KEY.START_TIME,
                SESSION.KEY.STOP_TIME
        }, SESSION.KEY.ID + "=" + session_id, null, null, null, null);


        if(c_session_details.moveToFirst()) {
            int zone_id = c_session_details.getInt(c_session_details.getColumnIndex(SESSION.KEY.ZONE_ID));
            int start_time = c_session_details.getInt(c_session_details.getColumnIndex(SESSION.KEY.START_TIME));
            int stop_time = c_session_details.getInt(c_session_details.getColumnIndex(SESSION.KEY.STOP_TIME));

            session = new Session(zone_id, start_time, stop_time);
        }
        c_session_details.close();

        return session;
    }

    /**
     * Get a list of notifications that were blocked during the last session.
     *
     * @return A list of notification parts.
     */
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

    /**
     * Get a zone from the given ID.
     *
     * @param zoneId id of zone to get.
     * @return Zone object.
     */
    public Zone getZoneFromID(int zoneId) {
        Zone z = null;

        Cursor c = db.query(ZONE.TABLE, new String[] {
                ZONE.KEY.ID,
                ZONE.KEY.NAME,
                ZONE.KEY.RADIUS,
                ZONE.KEY.LAT,
                ZONE.KEY.LNG,
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
            String[] blockingApps = Util.stringToArray(c.getString(c.getColumnIndex(ZONE.KEY.BLOCKING_APPS)));
            String[] keywords = Util.stringToArray(c.getString(c.getColumnIndex(ZONE.KEY.KEYWORDS)));

            z = new Zone(id, lat, lng, radius, name, hasSynced, blockingApps, keywords);
        }

        c.close();

        return z;
    }

    /**
     * Get the total number of zones.
     *
     * @return long, number of zones currently alive.
     */
    public long getNumberOfZones() {
        return DatabaseUtils.queryNumEntries(db, ZONE.TABLE);
    }

    /**
     * Gets the number of unique apps being blocked across all zones.
     *
     * @return The number of apps that are being blocked.
     */
    public int getUniqueNumberOfBlockingApps() {
        Map<String, Integer> apps = Util.getOccurrencesFromListOfArrays(getAllBlockingApps());
        return apps.size();
    }

    /**
     * Gets the number of unique keywords across all zones.
     *
     * @return The number of keywords that are used.
     */
    public int getUniqueNumberOfKeywords() {
        Map<String, Integer> words = Util.getOccurrencesFromListOfArrays(getAllKeywords());
        return words.size();
    }



    //
    //
    // UPDATE
    //
    //

    /**
     * Update a current zone in the Zone Table database, based on the ID of the zone object.
     *
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

    /**
     * Set a notification as synced based on the given notification id.
     *
     * @param notificationID Id of notification to be synced.
     */
    public void setNotificationHasBeenSentToUser(int notificationID) {
        db.execSQL("UPDATE " + NOTIFICATION.TABLE
                + " SET "
                + NOTIFICATION.KEY.SENT_TO_USER + "=" + 1
                + " WHERE "
                + NOTIFICATION.KEY.ID + " = " + notificationID
                + ";");
    }




    //
    //
    // DELETE
    //
    //


    /**
     * Delete a zone with the given ID.
     *
     * @param id ID of zone to delete.
     */
    public void deleteZone(int id) {
        db.execSQL("DELETE FROM " + ZONE.TABLE
                + " WHERE " + ZONE.KEY.ID + "=" + id + ";");
    }




    //
    //
    // INSERT
    //
    //

    /**
     * Write a new zone (give) to the Zone Table database.
     *
     * @param zone to write.
     */
    public void writeZone(Zone zone)
    {
        db.execSQL("INSERT INTO " + ZONE.TABLE + " ("
                + ZONE.KEY.NAME + ","
                + ZONE.KEY.RADIUS + ","
                + ZONE.KEY.LAT + ","
                + ZONE.KEY.LNG + ","
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
                + zone.hasSynced + ", "
                + "'" + zone.blockingAppsAsStr() + "', "
                + "'" + zone.keywordsAsStr()
                + "');");
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
     * Starts a new session by creating a new row in the Session Table,
     * and getting that new Session ID.
     *
     * @param zoneID Zone that relates to the session.
     * @param startTime Start time of the session.
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

}