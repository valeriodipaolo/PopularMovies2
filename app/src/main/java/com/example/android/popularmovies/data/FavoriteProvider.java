package com.example.android.popularmovies.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

/**
 * Created by abde3445 on 03/03/2017.
 */

public class FavoriteProvider extends ContentProvider{

    private static final String TAG = "FavoriteProvider";
    public static final int CODE_FAVORITE = 100;
    public static final int CODE_FAVORITE_WITH_ID = 101;

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private FavoriteDBHelper mOpenHelper;

    public static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = FavoriteContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, FavoriteContract.PATH_FAVORITE, CODE_FAVORITE);
        matcher.addURI(authority, FavoriteContract.PATH_FAVORITE + "/#", CODE_FAVORITE_WITH_ID);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new FavoriteDBHelper(getContext());
        return true;
    }


        @Override
        public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                            String[] selectionArgs, String sortOrder) {

            Cursor cursor;

            switch (sUriMatcher.match(uri)) {

                case CODE_FAVORITE: {
                    cursor = mOpenHelper.getReadableDatabase().query(
                            FavoriteContract.FavoriteEntry.TABLE_NAME,
                            projection,
                            selection,
                            selectionArgs,
                            null,
                            null,
                            sortOrder);

                    break;
                }

                case CODE_FAVORITE_WITH_ID:{
                    String id = uri.getPathSegments().get(1);
                    cursor = mOpenHelper.getReadableDatabase().query(
                            FavoriteContract.FavoriteEntry.TABLE_NAME,
                            projection,
                            "_id=?",
                            new String[]{id},
                            null,
                            null,
                            sortOrder);
                    break;
                }

                default:
                    throw new UnsupportedOperationException("Unknown uri: " + uri);
            }

            cursor.setNotificationUri(getContext().getContentResolver(), uri);
            return cursor;
        }

        @Override
        public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {

            int numRowsDeleted;

            switch (sUriMatcher.match(uri)) {

                case CODE_FAVORITE_WITH_ID:
                    String id = uri.getPathSegments().get(1);
                    numRowsDeleted = mOpenHelper.getWritableDatabase().delete(
                            FavoriteContract.FavoriteEntry.TABLE_NAME,
                            "_id=?",
                            new String[]{id});

                    break;

                default:
                    throw new UnsupportedOperationException("Unknown uri: " + uri);
            }

            if (numRowsDeleted != 0) {
                getContext().getContentResolver().notifyChange(uri, null);
            }

            return numRowsDeleted;
        }

        @Override
        public Uri insert(@NonNull Uri uri, ContentValues values) {

            final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            long _id = 0;
            Uri result = null;
            switch (sUriMatcher.match(uri)) {

                case CODE_FAVORITE:
                    db.beginTransaction();
                    try {
                        _id = db.insert(FavoriteContract.FavoriteEntry.TABLE_NAME, null, values);
                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown uri: " + uri);
            }

            if(_id>0) {
                result = ContentUris.withAppendedId(uri, _id);
                getContext().getContentResolver().notifyChange(uri, null);
            }

            return result;
        }

        @Override
        public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
            throw new UnsupportedOperationException("Update method not allowed");
        }

        @Override
        public String getType(@NonNull Uri uri) {
            throw new UnsupportedOperationException("getType method not allowed");
        }

        @Override
        @TargetApi(11)
        public void shutdown() {
            mOpenHelper.close();
            super.shutdown();
        }
}
