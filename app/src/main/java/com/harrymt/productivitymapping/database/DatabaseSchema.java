package com.harrymt.productivitymapping.database;

public class DatabaseSchema {

    static class ZONE {

        static final String TABLE = "zoneTbl";

        static class KEY {
            static final String ID = "id";
            static final String LAT = "lat";
            static final String LNG = "lng";
            static final String RADIUS = "radius";
            static final String NAME = "name";
            static final String AUTO_START_STOP = "autoStartStop";
            static final String HAS_SYNCED = "hasSynced"; // 1 it has, 0 is hasnt

            // TODO possibly want to extract these 2 into another table..?
            static final String BLOCKING_APPS = "blockingApps";
            static final String KEYWORDS = "keywords";
        }

        static final String SQL_CREATE_TABLE =
                "CREATE TABLE if not exists " + TABLE + " (" +
                        KEY.ID + " INTEGER PRIMARY KEY autoincrement," +
                        KEY.LAT + " REAL, " +
                        KEY.LNG + " REAL, " +
                        KEY.RADIUS + " REAL, " +
                        KEY.NAME + " TEXT, " +
                        KEY.AUTO_START_STOP + " INTEGER, " +
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
            static final String PRODUCTIVITY_PERCENTAGE = "productivityPercentage";
        }

        static final String SQL_CREATE_TABLE =
                "CREATE TABLE if not exists " + TABLE + " (" +
                        KEY.ID + " INTEGER PRIMARY KEY autoincrement," +
                        KEY.ZONE_ID + " INTEGER, " +
                        KEY.START_TIME + " INTEGER, " +
                        KEY.STOP_TIME + " INTEGER, " +
                        KEY.PRODUCTIVITY_PERCENTAGE + " REAL " +
                        ");";

    }


    static class APPUSAGE {

        static final String TABLE = "appUsageTbl";

        static class KEY {
            static final String ID = "id";
            static final String SESSION_ID = "sessionId";
            static final String APP_PACKAGE_NAME = "packageName";
            static final String TIME_SPENT = "timeSpent"; // in seconds
            static final String CATEGORY = "category";

            // TODO add other appCharacteristics
        }

        static final String SQL_CREATE_TABLE =
                "CREATE TABLE if not exists " + TABLE + " (" +
                        KEY.ID + " INTEGER PRIMARY KEY autoincrement," +
                        KEY.SESSION_ID + " INTEGER, " +
                        KEY.APP_PACKAGE_NAME + " TEXT, " +
                        KEY.TIME_SPENT + " INTEGER, " +
                        KEY.CATEGORY + " TEXT " +
                        ");";
    }


    static class NOTIFICATION {
        static final String TABLE = "notificationTbl";

        static class KEY {
            static final String ID = "id";
            static final String SENT_TO_USER = "sentToUser";
            static final String SESSION_ID = "sessionId";
            static final String PACKAGE = "package";
            // TODO add other notification characteristics
            // static final String ICON = "icon";
        }

        static final String SQL_CREATE_TABLE =
                "CREATE TABLE if not exists " + TABLE + " (" +
                        KEY.ID + " INTEGER PRIMARY KEY autoincrement," +
                        KEY.SESSION_ID + " INTEGER, " +
                        KEY.SENT_TO_USER + " INTEGER, " +
                        KEY.PACKAGE + " TEXT " +
                        ");";
    }
}
