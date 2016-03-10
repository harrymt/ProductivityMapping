package com.harrymt.productivitymapping.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static com.harrymt.productivitymapping.database.DatabaseSchema.*;

class DatabaseHelper extends SQLiteOpenHelper {
    private static String TAG = "g53ids";

    public DatabaseHelper(Context context) {
        super(context, "userData", null, 14);
    }

    /**
     * When the database is created, will be called on the constructor
     * if the database isnt created.
     *
     * @param db Database to create.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ZONE.SQL_CREATE_TABLE);
        db.execSQL(SESSION.SQL_CREATE_TABLE);
        db.execSQL(APPUSAGE.SQL_CREATE_TABLE);
        db.execSQL(NOTIFICATION.SQL_CREATE_TABLE);
    }

    /**
     * When the database version number has been increased.
     * @param db Database
     * @param oldVersion Old integer
     * @param newVersion New integer
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading Database from " + oldVersion + " to " + newVersion);

        db.execSQL("DROP TABLE IF EXISTS " + ZONE.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + SESSION.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + APPUSAGE.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + NOTIFICATION.TABLE);
        onCreate(db);
    }
}