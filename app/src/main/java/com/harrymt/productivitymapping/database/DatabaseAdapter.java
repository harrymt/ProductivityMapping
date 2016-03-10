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
            while (c.moveToNext()) {
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
            while (c.moveToNext()) {
                int id = c.getInt(c.getColumnIndex(ZONE.KEY.ID));

                String name = c.getString(c.getColumnIndex(ZONE.KEY.NAME));
                float radius = c.getFloat(c.getColumnIndex(ZONE.KEY.RADIUS));
                double lat = c.getDouble(c.getColumnIndex(ZONE.KEY.LAT));
                double lng = c.getDouble(c.getColumnIndex(ZONE.KEY.LNG));
                int autoStart = c.getInt(c.getColumnIndex(ZONE.KEY.AUTO_START_STOP));
                int hasSynced = c.getInt(c.getColumnIndex(ZONE.KEY.HAS_SYNCED));
                String[] blockingApps = Zone.stringToArray(c.getString(c.getColumnIndex(ZONE.KEY.BLOCKING_APPS)));
                String[] keywords = Zone.stringToArray(c.getString(c.getColumnIndex(ZONE.KEY.KEYWORDS)));

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
                + NOTIFICATION.KEY.SESSION_ID
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

    private Integer getLastSessionId()
    {
        Cursor c = db.query(SESSION.TABLE, new String[] { SESSION.KEY.ID }, null, null, null, null, null);
        c.moveToLast();
        Integer sessionId = c.getInt(0);
        c.close();
        return sessionId;
    }
}