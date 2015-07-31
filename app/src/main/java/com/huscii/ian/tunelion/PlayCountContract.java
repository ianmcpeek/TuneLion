package com.huscii.ian.tunelion;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by Ian on 7/31/2015.
 */
public final class PlayCountContract {

    public PlayCountContract() {}

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    public static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + PlayCountEntry.TABLE_NAME +
            " (" + PlayCountEntry.COLUMN_NAME_SONGNAME + TEXT_TYPE + COMMA_SEP +
            PlayCountEntry.COLUMN_NAME_PLAYCOUNT + "INTEGER" +
            " )";

    public static abstract class PlayCountEntry implements BaseColumns {
        public static final String TABLE_NAME = "PlayCounts";
        public static final String COLUMN_NAME_SONGNAME = "song name";
        public static final String COLUMN_NAME_PLAYCOUNT = "play count";
    }

    public class PlayCountDatabaseHelper extends SQLiteOpenHelper {

        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "PlayCount.db";

        public PlayCountDatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(PlayCountContract.SQL_CREATE_ENTRIES);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }

        public void insert(String songname, PlayCountDatabaseHelper dbHelper) {
            ContentValues values = new ContentValues();
            values.put(PlayCountEntry.COLUMN_NAME_SONGNAME, songname);
            values.put(PlayCountEntry.COLUMN_NAME_PLAYCOUNT, 1);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.insert(PlayCountEntry.TABLE_NAME, null, values);
        }

        public void read() {

        }

        public void update() {

        }
    }
}
