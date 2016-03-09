package com.harrymt.productivitymapping.database;

/**
 * Created by harrymt on 09/03/16.
 */
public class DatabaseSchema {

    public static final String ZONE_TABLE = "zoneTbl";
    public static final String ZONE_KEY_ID = "id";
    public static final String ZONE_KEY_LAT = "lat";
    public static final String ZONE_KEY_LNG = "lng";
    public static final String ZONE_KEY_RADIUS = "radius";
    public static final String ZONE_KEY_NAME = "name";
    public static final String ZONE_KEY_AUTO_START_STOP = "autoStartStop";
    public static final String ZONE_KEY_HAS_SYNCED= "hasSynced"; // 1 it has, 0 is hasnt

    // TODO possibly want to extract these 2 into another table..?
    public static final String ZONE_KEY_BLOCKING_APPS = "blockingApps";
    public static final String ZONE_KEY_KEYWORDS = "keywords";



    public static final String SQLITE_CREATE_TABLE_ZONE =
            "CREATE TABLE if not exists " + ZONE_TABLE + " (" +
                    ZONE_KEY_ID + " INTEGER PRIMARY KEY autoincrement," +
                    ZONE_KEY_LAT + " REAL, " +
                    ZONE_KEY_LNG + " REAL, " +
                    ZONE_KEY_RADIUS + " REAL, " +
                    ZONE_KEY_NAME + " TEXT, " +
                    ZONE_KEY_AUTO_START_STOP + " INTEGER, " +
                    ZONE_KEY_HAS_SYNCED + " INTEGER, " +
                    ZONE_KEY_BLOCKING_APPS + " TEXT, " +
                    ZONE_KEY_KEYWORDS + " TEXT " +
                    ");";


    public static final String SESSION_TABLE = "sessionTbl";
    public static final String SESSION_KEY_ID = "id";
    public static final String SESSION_KEY_ZONE_ID = "zoneId";
    public static final String SESSION_KEY_START_TIME = "startTime";
    public static final String SESSION_KEY_STOP_TIME = "stopTime";
    public static final String SESSION_KEY_PRODUCTIVITY_PERCENTAGE = "productivityPercentage";


    public static final String SQLITE_CREATE_TABLE_SESSION =
            "CREATE TABLE if not exists " + SESSION_TABLE + " (" +
                    SESSION_KEY_ID + " INTEGER PRIMARY KEY autoincrement," +
                    SESSION_KEY_ZONE_ID + " INTEGER, " +
                    SESSION_KEY_START_TIME + " INTEGER, " +
                    SESSION_KEY_STOP_TIME + " INTEGER, " +
                    SESSION_KEY_PRODUCTIVITY_PERCENTAGE + " REAL " +
                    ");";


    public static final String APPUSAGE_TABLE = "appUsageTbl";
    public static final String APPUSAGE_KEY_ID = "id";
    public static final String APPUSAGE_KEY_SESSION_ID = "sessionId";
    public static final String APPUSAGE_KEY_APP_PACKAGE_NAME = "packageName";
    public static final String APPUSAGE_KEY_TIME_SPENT = "timeSpent"; // in seconds
    public static final String APPUSAGE_KEY_CATEGORY = "category";

    // TODO add other appCharacteristics

    public static final String SQLITE_CREATE_TABLE_APPUSAGE =
            "CREATE TABLE if not exists " + APPUSAGE_TABLE + " (" +
                    APPUSAGE_KEY_ID + " INTEGER PRIMARY KEY autoincrement," +
                    APPUSAGE_KEY_SESSION_ID + " INTEGER, " +
                    APPUSAGE_KEY_APP_PACKAGE_NAME + " TEXT, " +
                    APPUSAGE_KEY_TIME_SPENT + " INTEGER, " +
                    APPUSAGE_KEY_CATEGORY + " TEXT " +
                    ");";


    public static final String NOTIFICATION_TABLE = "notificationTbl";
    public static final String NOTIFICATION_KEY_ID = "id";
    public static final String NOTIFICATION_KEY_SESSION_ID = "sessionId";
    public static final String NOTIFICATION_KEY_PACKAGE = "package";
    // TODO add other notification characteristics
    // public static final String NOTIFICATION_KEY_ICON = "icon";


    public static final String SQLITE_CREATE_TABLE_NOTIFICATION =
            "CREATE TABLE if not exists " + NOTIFICATION_TABLE + " (" +
                    NOTIFICATION_KEY_ID + " INTEGER PRIMARY KEY autoincrement," +
                    NOTIFICATION_KEY_SESSION_ID + " INTEGER, " +
                    NOTIFICATION_KEY_PACKAGE + " TEXT " +
                    ");";

}
