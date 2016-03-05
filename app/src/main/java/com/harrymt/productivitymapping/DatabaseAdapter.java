package com.harrymt.productivitymapping;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.ArrayList;

public class DatabaseAdapter
{
    private static final String TAG = "g53ids";

    // https://developer.android.com/training/basics/data-storage/databases.html
    // TODO move to a contract and add implement the BaseColumns class.

    private static final String ZONE_TABLE = "zoneTbl";
        public static final String ZONE_KEY_ID = "id";
        public static final String ZONE_KEY_LAT = "lat";
        public static final String ZONE_KEY_LNG = "lng";
        public static final String ZONE_KEY_RADIUS = "radius";
        public static final String ZONE_KEY_NAME = "name";
        public static final String ZONE_KEY_AUTO_START_STOP = "autoStartStop";
        // TODO possibly want to extract these 2 into another table..?
        public static final String ZONE_KEY_BLOCKING_APPS = "blockingApps";
        public static final String ZONE_KEY_KEYWORDS = "keywords";


    private static final String SQLITE_CREATE_TABLE_ZONE =
        "CREATE TABLE if not exists " + ZONE_TABLE + " (" +
            ZONE_KEY_ID + " INTEGER PRIMARY KEY autoincrement," +
            ZONE_KEY_LAT + " REAL, " +
            ZONE_KEY_LNG + " REAL, " +
            ZONE_KEY_RADIUS + " REAL, " +
            ZONE_KEY_NAME + " TEXT, " +
            ZONE_KEY_AUTO_START_STOP + " INTEGER, " +
            ZONE_KEY_BLOCKING_APPS + " TEXT, " +
            ZONE_KEY_KEYWORDS + " TEXT " +
        ");";


    private static final String SESSION_TABLE = "sessionTbl";
        public static final String SESSION_KEY_ID = "id";
        public static final String SESSION_KEY_ZONE_ID = "zoneId";
        public static final String SESSION_KEY_START_TIME = "startTime";
        public static final String SESSION_KEY_STOP_TIME = "stopTime";
        public static final String SESSION_KEY_PRODUCTIVITY_PERCENTAGE = "productivityPercentage";


    private static final String SQLITE_CREATE_TABLE_SESSION =
        "CREATE TABLE if not exists " + SESSION_TABLE + " (" +
            SESSION_KEY_ID + " INTEGER PRIMARY KEY autoincrement," +
            SESSION_KEY_ZONE_ID + " INTEGER, " +
            SESSION_KEY_START_TIME + " INTEGER, " +
            SESSION_KEY_STOP_TIME + " INTEGER, " +
            SESSION_KEY_PRODUCTIVITY_PERCENTAGE + " REAL " +
        ");";


    private static final String APPUSAGE_TABLE = "appUsageTbl";
        public static final String APPUSAGE_KEY_ID = "id";
        public static final String APPUSAGE_KEY_SESSION_ID = "sessionId";
        public static final String APPUSAGE_KEY_APP_PACKAGE_NAME = "packageName";
        public static final String APPUSAGE_KEY_TIME_SPENT = "timeSpent"; // in seconds
        public static final String APPUSAGE_KEY_CATEGORY = "category";

        // TODO add other appCharacteristics

    private static final String SQLITE_CREATE_TABLE_APPUSAGE =
        "CREATE TABLE if not exists " + APPUSAGE_TABLE + " (" +
            APPUSAGE_KEY_ID + " INTEGER PRIMARY KEY autoincrement," +
            APPUSAGE_KEY_SESSION_ID + " INTEGER, " +
            APPUSAGE_KEY_APP_PACKAGE_NAME + " TEXT, " +
            APPUSAGE_KEY_TIME_SPENT + " INTEGER, " +
            APPUSAGE_KEY_CATEGORY + " TEXT " +
        ");";


    private static final String NOTIFICATION_TABLE = "notificationTbl";
    public static final String NOTIFICATION_KEY_ID = "id";
    public static final String NOTIFICATION_KEY_SESSION_ID = "sessionId";
        public static final String NOTIFICATION_KEY_PACKAGE = "package";
        // TODO add other notification characteristics
        // public static final String NOTIFICATION_KEY_ICON = "icon";


    private static final String SQLITE_CREATE_TABLE_NOTIFICATION =
        "CREATE TABLE if not exists " + NOTIFICATION_TABLE + " (" +
            NOTIFICATION_KEY_ID + " INTEGER PRIMARY KEY autoincrement," +
            NOTIFICATION_KEY_SESSION_ID + " INTEGER, " +
            NOTIFICATION_KEY_PACKAGE + " TEXT " +
        ");";

    private DatabaseHelper dbHelper;
    public SQLiteDatabase db;
    private Context context;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context) {
            super(context, "userData", null, 12);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d("g53ids", "onCreate");
            // Create tables
            db.execSQL(SQLITE_CREATE_TABLE_ZONE);
            db.execSQL(SQLITE_CREATE_TABLE_SESSION);
            db.execSQL(SQLITE_CREATE_TABLE_APPUSAGE);
            db.execSQL(SQLITE_CREATE_TABLE_NOTIFICATION);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.d("g53ids", "onUpgrade() - UPGRADING DATABASE FROM: " + oldVersion + " TO NEWVERSION: " + newVersion);

            db.execSQL("DROP TABLE IF EXISTS " + ZONE_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + SESSION_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + APPUSAGE_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + NOTIFICATION_TABLE);
            onCreate(db);
        }
    }

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



    /** ------ Database interactions ------ **/


    public void deleteZone(int id) {
        db.execSQL("DELETE FROM " + ZONE_TABLE
                + " WHERE " + ZONE_KEY_ID + "=" + id + ";");
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
                String[] blockingApps = Zone.stringToArray(c.getString(c.getColumnIndex(ZONE_KEY_BLOCKING_APPS)));
                String[] keywords = Zone.stringToArray(c.getString(c.getColumnIndex(ZONE_KEY_KEYWORDS)));

                Zone z = new Zone(id, lat, lng, radius, name, autoStart, blockingApps, keywords);
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
                + ZONE_KEY_BLOCKING_APPS + ","
                + ZONE_KEY_KEYWORDS
                + ") "
                + "VALUES "
                + "('"
                + zone.name + "', "
                + zone.radiusInMeters + ", "
                + zone.lat + ", "
                + zone.lng + ", "
                + zone.autoStartStop + ", '"
                + zone.blockingAppsAsStr() + "', '"
                + zone.keywordsAsStr()
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
                + ZONE_KEY_BLOCKING_APPS + "='" + zone.blockingAppsAsStr() + "', "
                + ZONE_KEY_KEYWORDS + "='" + zone.keywordsAsStr() + "'"
                + " WHERE "
                + ZONE_KEY_ID + " = " + zone.zoneID
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
                + "('" + n.getPackageName() + "', " + ProjectStates.SESSION_ID  + ");");
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
                + "('" + packageName + "', " + timeSpentInSeconds + ", " + ProjectStates.SESSION_ID + ");");
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
        ProjectStates.SESSION_ID = getLastSessionId();
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