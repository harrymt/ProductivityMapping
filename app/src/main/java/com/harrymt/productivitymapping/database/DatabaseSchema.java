package com.harrymt.productivitymapping.database;

import com.harrymt.productivitymapping.PROJECT_GLOBALS;

/**
 * Defines the database layout.
 */
public class DatabaseSchema {
    private static final String TAG = PROJECT_GLOBALS.LOG_NAME + "DatabaseSchema";

    static class ZONE {
        static final String TABLE = "zoneTbl";

        static class KEY {
            static final String ID = "id";
            static final String LAT = "lat";
            static final String LNG = "lng";
            static final String RADIUS = "radius";
            static final String NAME = "name";
            static final String HAS_SYNCED = "hasSynced"; // 1 it has, 0 is hasn't
            static final String BLOCKING_APPS = "blockingApps";
            static final String KEYWORDS = "keywords";
        }

        static final String SQL_CREATE_TABLE =
                "CREATE TABLE if not exists " + TABLE + " (" +
                        KEY.ID + " INTEGER PRIMARY KEY autoincrement, " +
                        KEY.LAT + " REAL, " +
                        KEY.LNG + " REAL, " +
                        KEY.RADIUS + " REAL, " +
                        KEY.NAME + " TEXT, " +
                        KEY.HAS_SYNCED + " INTEGER, " +
                        KEY.BLOCKING_APPS + " TEXT, " +
                        KEY.KEYWORDS + " TEXT " +
                        ");";
    }


    static class SESSION {
        static final String TABLE = "sessionTbl";

        static class KEY {
            static final String ID = "id";
            static final String ZONE_ID = "zoneId";
            static final String START_TIME = "startTime";
            static final String STOP_TIME = "stopTime";
        }

        static final String SQL_CREATE_TABLE =
                "CREATE TABLE if not exists " + TABLE + " (" +
                        KEY.ID + " INTEGER PRIMARY KEY autoincrement, " +
                        KEY.ZONE_ID + " INTEGER, " +
                        KEY.START_TIME + " INTEGER, " +
                        KEY.STOP_TIME + " INTEGER " +
                        ");";

    }


    static class NOTIFICATION {
        static final String TABLE = "notificationTbl";

        static class KEY {
            static final String ID = "id";
            static final String SENT_TO_USER = "sentToUser";
            static final String SESSION_ID = "sessionId";
            static final String PACKAGE = "package";
            static final String TITLE = "title";
            static final String TEXT = "nText";
            static final String SUB_TEXT = "subText";
            static final String CONTENT_INFO = "contentInfo";
            static final String ICON = "icon";
            static final String LARGE_ICON = "lIcon";
        }

        static final String SQL_CREATE_TABLE =
                "CREATE TABLE if not exists " + TABLE + " (" +
                        KEY.ID + " INTEGER PRIMARY KEY autoincrement, " +
                        KEY.SESSION_ID + " INTEGER, " +
                        KEY.SENT_TO_USER + " INTEGER, " +
                        KEY.PACKAGE + " TEXT, " +
                        KEY.TITLE + " TEXT, " +
                        KEY.TEXT + " TEXT, " +
                        KEY.SUB_TEXT + " TEXT, " +
                        KEY.CONTENT_INFO + " TEXT, " +
                        KEY.ICON + " INTEGER, " +
                        KEY.LARGE_ICON + " BLOB" +
                        ");";
    }
}