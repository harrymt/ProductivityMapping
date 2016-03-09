package com.harrymt.productivitymapping.database;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.harrymt.productivitymapping.PROJECT_GLOBALS;
import com.harrymt.productivitymapping.Zone;

import java.util.ArrayList;

// Import the database schema
import static com.harrymt.productivitymapping.database.DatabaseSchema.*;

public class DatabaseAdapter
{
    private static final String TAG = "g53ids";

    // https://developer.android.com/training/basics/data-storage/databases.html
    // TODO move to a contract and add implement the BaseColumns class.

    private DatabaseHelper dbHelper;
    public SQLiteDatabase db;
    private Context context;

    public DatabaseAdapter(Context context) {
        this.context = context;
    }

    public DatabaseAdapter open() throws SQLException {
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
        db.execSQL("DELETE FROM " + ZONE_TABLE
                + " WHERE " + ZONE_KEY_ID + "=" + id + ";");
    }


    /**
     * Get all zones that have their synced flag as 0
     */
    public ArrayList<Zone> getAllZonesThatNeedToBeSynced()
    {
        Cursor c = db.query(ZONE_TABLE, new String[] {
                ZONE_KEY_ID,
                ZONE_KEY_NAME,
                ZONE_KEY_RADIUS,
                ZONE_KEY_LAT,
                ZONE_KEY_LNG,
                ZONE_KEY_HAS_SYNCED,
                ZONE_KEY_BLOCKING_APPS,
                ZONE_KEY_KEYWORDS
        }, ZONE_KEY_HAS_SYNCED + "=0", null, null, null, null);

        ArrayList<Zone> zones = new ArrayList<>();
        if(c.moveToFirst()) {
            while (c.moveToNext()) {
                int id = c.getInt(c.getColumnIndex(ZONE_KEY_ID));
                String name = c.getString(c.getColumnIndex(ZONE_KEY_NAME));
                float radius = c.getFloat(c.getColumnIndex(ZONE_KEY_RADIUS));
                double lat = c.getDouble(c.getColumnIndex(ZONE_KEY_LAT));
                double lng = c.getDouble(c.getColumnIndex(ZONE_KEY_LNG));
                int hasSynced = c.getInt(c.getColumnIndex(ZONE_KEY_HAS_SYNCED));
                String[] blockingApps = Zone.stringToArray(c.getString(c.getColumnIndex(ZONE_KEY_BLOCKING_APPS)));
                String[] keywords = Zone.stringToArray(c.getString(c.getColumnIndex(ZONE_KEY_KEYWORDS)));

                Zone z = new Zone(id, lat, lng, radius, name, -1, hasSynced, blockingApps, keywords);
                zones.add(z);
            }
        }
        c.close();

        return zones;
    }

    /**
     * Read all the Zones from the Zone Table database.
     */
    public ArrayList<Zone> getAllZones()
    {
        Cursor c = db.query(ZONE_TABLE, new String[] {
                ZONE_KEY_ID,
                ZONE_KEY_NAME,
                ZONE_KEY_RADIUS,
                ZONE_KEY_LAT,
                ZONE_KEY_LNG,
                ZONE_KEY_AUTO_START_STOP,
                ZONE_KEY_HAS_SYNCED,
                ZONE_KEY_BLOCKING_APPS,
                ZONE_KEY_KEYWORDS
        }, null, null, null, null, null);

        ArrayList<Zone> zones = new ArrayList<>();
        if(c.moveToFirst()) {
            while (c.moveToNext()) {
                int id = c.getInt(c.getColumnIndex(ZONE_KEY_ID));

                String name = c.getString(c.getColumnIndex(ZONE_KEY_NAME));
                float radius = c.getFloat(c.getColumnIndex(ZONE_KEY_RADIUS));
                double lat = c.getDouble(c.getColumnIndex(ZONE_KEY_LAT));
                double lng = c.getDouble(c.getColumnIndex(ZONE_KEY_LNG));
                int autoStart = c.getInt(c.getColumnIndex(ZONE_KEY_AUTO_START_STOP));
                int hasSynced = c.getInt(c.getColumnIndex(ZONE_KEY_HAS_SYNCED));
                String[] blockingApps = Zone.stringToArray(c.getString(c.getColumnIndex(ZONE_KEY_BLOCKING_APPS)));
                String[] keywords = Zone.stringToArray(c.getString(c.getColumnIndex(ZONE_KEY_KEYWORDS)));

                Zone z = new Zone(id, lat, lng, radius, name, autoStart, hasSynced, blockingApps, keywords);
                zones.add(z);
            }
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
        db.execSQL("INSERT INTO " + ZONE_TABLE + " ("
                + ZONE_KEY_NAME + ","
                + ZONE_KEY_RADIUS + ","
                + ZONE_KEY_LAT + ","
                + ZONE_KEY_LNG + ","
                + ZONE_KEY_AUTO_START_STOP + ","
                + ZONE_KEY_HAS_SYNCED + ","
                + ZONE_KEY_BLOCKING_APPS + ","
                + ZONE_KEY_KEYWORDS
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
        db.execSQL("UPDATE " + ZONE_TABLE
                + " SET "
                + ZONE_KEY_NAME + "= '" + zone.name + "', "
                + ZONE_KEY_RADIUS + "=" + zone.radiusInMeters + ", "
                + ZONE_KEY_LAT + "=" + zone.lat + ", "
                + ZONE_KEY_LNG + "=" + zone.lng + ", "
                + ZONE_KEY_AUTO_START_STOP + "=" + zone.autoStartStop + ", "
                + ZONE_KEY_HAS_SYNCED + "=" + zone.hasSynced + ", "
                + ZONE_KEY_BLOCKING_APPS + "='" + zone.blockingAppsAsStr() + "', "
                + ZONE_KEY_KEYWORDS + "='" + zone.keywordsAsStr() + "'"
                + " WHERE "
                + ZONE_KEY_ID + " = " + zone.zoneID
                + ";");
    }

    /**
     * Mark a zone as synced to the server.
     *
     * @param zone_id to mark as synced.
     */
    public void setZoneAsSynced(int zone_id)
    {
        db.execSQL("UPDATE " + ZONE_TABLE
                + " SET "
                + ZONE_KEY_HAS_SYNCED + "=" + 1
                + " WHERE "
                + ZONE_KEY_ID + " = " + zone_id
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
        db.execSQL("INSERT INTO " + NOTIFICATION_TABLE + " ("
                + NOTIFICATION_KEY_PACKAGE + ","
                + NOTIFICATION_KEY_SESSION_ID
                + ") "
                + "VALUES "
                + "('" + n.getPackageName() + "', " + PROJECT_GLOBALS.SESSION_ID  + ");");
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
        db.execSQL("INSERT INTO " + APPUSAGE_TABLE + " ("
                + APPUSAGE_KEY_APP_PACKAGE_NAME + ", "
                + APPUSAGE_KEY_TIME_SPENT + ", "
                + APPUSAGE_KEY_SESSION_ID
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
        db.execSQL("INSERT INTO " + SESSION_TABLE + " ("
                + SESSION_KEY_ZONE_ID + ","
                + SESSION_KEY_START_TIME + ""
                + ") "
                + "VALUES "
                + "(" + zoneID + ", " + startTime + ");");

        // Set the project state session ID
        PROJECT_GLOBALS.SESSION_ID = getLastSessionId();
    }

    private Integer getLastSessionId()
    {
        Cursor c = db.query(SESSION_TABLE, new String[] { SESSION_KEY_ID }, null, null, null, null, null);
        c.moveToLast();
        Integer sessionId = c.getInt(0);
        c.close();
        return sessionId;
    }
}