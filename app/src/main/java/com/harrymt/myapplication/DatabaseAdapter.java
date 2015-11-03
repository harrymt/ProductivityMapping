package com.harrymt.myapplication;


import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.Calendar;

/**
 * Created by harrymt on 03/11/15.
 */
public class DatabaseAdapter
{
    public static final String KEY_ROWID = "_id";
    public static final String KEY_STUDY_START_TIME = "startTime";

    private static final String STUDY_TIMES_TABLE  = "studyTimes";

    private static final String SQLITE_CREATE =
            "CREATE TABLE if not exists " + STUDY_TIMES_TABLE + " (" +
                    KEY_ROWID + " integer PRIMARY KEY autoincrement," +
                    KEY_STUDY_START_TIME +
            ");";

    private DatabaseHelper dbHelper;
    public SQLiteDatabase db;
    private Context context;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context) {
            super(context, "harrysDB", null, 5);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d("g53ids", "onCreate");

            db.execSQL(SQLITE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + STUDY_TIMES_TABLE);
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

    public Cursor getStartTime()
    {
        Cursor c = db.query(STUDY_TIMES_TABLE, new String[] { KEY_STUDY_START_TIME }, null, null, null, null, null);
        return c;
    }


    public void addStartTime(String startTime)
    {
        db.execSQL("INSERT INTO " + STUDY_TIMES_TABLE + " (" + KEY_STUDY_START_TIME + ") " +
                "VALUES " +
                "('" + startTime + "');");
    }
}
