package com.harrymt.productivitymapping;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseAdapter
{

    private static final String ZONE_TABLE = "zoneTbl";
        public static final String ZONE_KEY_ID = "id";
        public static final String ZONE_KEY_X = "x";
        public static final String ZONE_KEY_Y = "y";
        public static final String ZONE_KEY_RADIUS = "radius";
        public static final String ZONE_KEY_NAME = "name";
        public static final String ZONE_KEY_AUTO_START_STOP = "autoStartStop";
        // TODO possibly want to extract these 2 into another table..?
        public static final String ZONE_KEY_BLOCKING_APPS = "blockingApps";
        public static final String ZONE_KEY_KEYWORDS = "keywords";


    private static final String SQLITE_CREATE_TABLE_ZONE =
        "CREATE TABLE if not exists " + ZONE_TABLE + " (" +
            ZONE_KEY_ID + " INTEGER PRIMARY KEY autoincrement," +
            ZONE_KEY_X + " REAL, " +
            ZONE_KEY_Y + " REAL, " +
            ZONE_KEY_RADIUS + " REAL, " +
            ZONE_KEY_NAME + " TEXT, " +
            ZONE_KEY_AUTO_START_STOP + " INTEGER, " +
            ZONE_KEY_BLOCKING_APPS + " INTEGER, " +
            ZONE_KEY_KEYWORDS + " TEXT, " +
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
            SESSION_KEY_PRODUCTIVITY_PERCENTAGE + " REAL, " +
        ");";


    private static final String APPUSAGE_TABLE = "appUsageTbl";
        public static final String APPUSAGE_KEY_ID = "id";
        public static final String APPUSAGE_KEY_APP_NAME = "appName";
        public static final String APPUSAGE_KEY_TIME_SPENT = "timeSpent";
        public static final String APPUSAGE_KEY_CATEGORY = "category";
        // TODO add other appCharacteristics

    private static final String SQLITE_CREATE_TABLE_APPUSAGE =
        "CREATE TABLE if not exists " + APPUSAGE_TABLE + " (" +
            APPUSAGE_KEY_ID + " INTEGER PRIMARY KEY autoincrement," +
            APPUSAGE_KEY_APP_NAME + " TEXT, " +
            APPUSAGE_KEY_TIME_SPENT + " INTEGER, " +
            APPUSAGE_KEY_CATEGORY + " TEXT, " +
        ");";


    private static final String NOTIFICATION_TABLE = "notificationTbl";
        public static final String NOTIFICATION_KEY_ID = "id";
        public static final String NOTIFICATION_KEY_TITLE = "title";
        // TODO add other notification characteristics
        // public static final String NOTIFICATION_KEY_ICON = "icon";


    private static final String SQLITE_CREATE_TABLE_NOTIFICATION =
        "CREATE TABLE if not exists " + NOTIFICATION_TABLE + " (" +
            NOTIFICATION_KEY_ID + " INTEGER PRIMARY KEY autoincrement," +
            NOTIFICATION_KEY_TITLE + " TEXT, " +
        ");";


    private DatabaseHelper dbHelper;
    public SQLiteDatabase db;
    private Context context;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context) {
            super(context, "martinDB", null, 5);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d("g54mdp", "onCreate");
            // Create tables
            db.execSQL(SQLITE_CREATE_TABLE_ZONE);
            db.execSQL(SQLITE_CREATE_TABLE_SESSION);
            db.execSQL(SQLITE_CREATE_TABLE_APPUSAGE);
            db.execSQL(SQLITE_CREATE_TABLE_NOTIFICATION);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + SQLITE_CREATE_TABLE_ZONE);
            db.execSQL("DROP TABLE IF EXISTS " + SQLITE_CREATE_TABLE_SESSION);
            db.execSQL("DROP TABLE IF EXISTS " + SQLITE_CREATE_TABLE_APPUSAGE);
            db.execSQL("DROP TABLE IF EXISTS " + SQLITE_CREATE_TABLE_NOTIFICATION);
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


    public void exampleInsertName(String name)
    {
        db.execSQL("INSERT INTO " + ZONE_TABLE + " (" + ZONE_KEY_NAME + ") " +
                "VALUES " +
                "('" + name + "');");
    }

    public Cursor fetchNameAsCursor()
    {
        Cursor c = db.query(ZONE_TABLE, new String[] { ZONE_KEY_ID, ZONE_KEY_NAME }, null, null, null, null, null);
        return c;
    }

}