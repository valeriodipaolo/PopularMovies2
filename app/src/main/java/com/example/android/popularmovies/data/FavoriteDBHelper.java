package com.example.android.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.popularmovies.data.FavoriteContract.FavoriteEntry;

/**
 * Created by abde3445 on 03/03/2017.
 */

public class FavoriteDBHelper extends SQLiteOpenHelper{

    public static final String DATABASE_NAME = "favorite.db";
    private static final int DATABASE_VERSION = 2;

    public FavoriteDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_FAVORITE_TABLE =

                "CREATE TABLE " +
                        FavoriteEntry.TABLE_NAME + " (" +
                            FavoriteEntry._ID               + " INTEGER PRIMARY KEY, " +
                            FavoriteEntry.COLUMN_POSTER     + " TEXT NOT NULL, "       +
                            FavoriteEntry.COLUMN_TITLE      + " TEXT NOT NULL,"        +
                            FavoriteEntry.COLUMN_REVIEW     + " TEXT, "                +
                            FavoriteEntry.COLUMN_RELEASE    + " TEXT, "                +
                            FavoriteEntry.COLUMN_PLOT       + " TEXT, "                +
                            FavoriteEntry.COLUMN_TIMESTAMP  + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP);";

        sqLiteDatabase.execSQL(SQL_CREATE_FAVORITE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + FavoriteEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}