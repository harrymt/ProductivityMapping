package com.harrymt.productivitymapping.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static com.harrymt.productivitymapping.database.DatabaseSchema.*;

class DatabaseHelper extends SQLiteOpenHelper {

    public DatabaseHelper(Context context) {
        super(context, "userData", null, 14);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
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