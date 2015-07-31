package com.huscii.ian.tunelion;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by Ian on 7/31/2015.
 */
public final class PlayCountContract {

    public PlayCountContract() {}

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ", ";
    public static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + PlayCountEntry.TABLE_NAME +
            " (" + PlayCountEntry.COLUMN_NAME_SONGNAME + TEXT_TYPE + COMMA_SEP +
            PlayCountEntry.COLUMN_NAME_PLAYCOUNT + " INTEGER" +
            ")";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + PlayCountEntry.TABLE_NAME;

    public static void insert(String songname, PlayCountDatabaseHelper dbHelper) {
        ContentValues values = new ContentValues();
        values.put(PlayCountEntry.COLUMN_NAME_SONGNAME, songname);
        values.put(PlayCountEntry.COLUMN_NAME_PLAYCOUNT, 1);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.insert(PlayCountEntry.TABLE_NAME, null, values);
    }

    public static int read(String songname, PlayCountDatabaseHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {
                PlayCountEntry.COLUMN_NAME_SONGNAME,
                PlayCountEntry.COLUMN_NAME_PLAYCOUNT
        };

        String sortOrder = PlayCountEntry.COLUMN_NAME_SONGNAME + " DESC";

        Cursor c = db.query(PlayCountEntry.TABLE_NAME, null, null, null, null, null, null);
        c.moveToFirst();
        while (c.moveToNext()) {
            int nameColIndex = c.getColumnIndex(PlayCountEntry.COLUMN_NAME_SONGNAME);
            int countColIndex = c.getColumnIndex(PlayCountEntry.COLUMN_NAME_PLAYCOUNT);
            if(songname.equals(c.getString(nameColIndex))) {
                int playCount = c.getInt(countColIndex);
                c.close();
                return playCount;
            }
        }
        c.close();
        return 0;
    }

    public static void update(String songname, int oldPlayCount, PlayCountDatabaseHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        ContentValues values = new ContentValues();
        values.put(PlayCountEntry.COLUMN_NAME_PLAYCOUNT, oldPlayCount+1);

        String selection = PlayCountEntry.COLUMN_NAME_SONGNAME + " LIKE ?";
        db.update(
                PlayCountEntry.TABLE_NAME,
                values,
                selection,
                new String[]{songname});
    }



    public static abstract class PlayCountEntry implements BaseColumns {
        public static final String TABLE_NAME = "PlayCounts";
        public static final String COLUMN_NAME_SONGNAME = "song_name";
        public static final String COLUMN_NAME_PLAYCOUNT = "play_count";
    }

    public static final class PlayCountDatabaseHelper extends SQLiteOpenHelper {

        public static final int DATABASE_VERSION = 3;
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
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }

    }
}
