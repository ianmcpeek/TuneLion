package com.huscii.ian.tunelion;

import android.provider.BaseColumns;

/**
 * Created by Ian on 7/31/2015.
 */
public class PlayCountContract {

    public PlayCountContract() {}

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + PlayCountEntry.TABLE_NAME +
            " (" + PlayCountEntry.COLUMN_NAME_SONGNAME + TEXT_TYPE + COMMA_SEP +
            PlayCountEntry.COLUMN_NAME_PLAYCOUNT + "INTEGER" +
            " )";

    public static abstract class PlayCountEntry implements BaseColumns {
        public static final String TABLE_NAME = "PlayCounts";
        public static final String COLUMN_NAME_SONGNAME = "song name";
        public static final String COLUMN_NAME_PLAYCOUNT = "play count";
    }
}
