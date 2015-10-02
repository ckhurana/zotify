package com.zuccessful.zotify.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.zuccessful.zotify.Utilities;

/**
 * Created by Chirag Khurana on 01-Sep-15.
 */
public class ZotifyProvider extends ContentProvider {

    static final int ZOTIFY = 100;
    static final int ZOTIFY_GENERAL = 101;
    static final int ZOTIFY_SUBJECTS = 102;
    static final int ZOTIFY_WITH_ID = 103;

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private ZotifyDbHelper mOpenHelper;

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = ZotifyContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, ZotifyContract.PATH_ZOTIFY, ZOTIFY);
        matcher.addURI(authority, ZotifyContract.PATH_GENERAL, ZOTIFY_GENERAL);
        matcher.addURI(authority, ZotifyContract.PATH_SUBJECTS, ZOTIFY_SUBJECTS);
        matcher.addURI(authority, ZotifyContract.PATH_ZOTIFY + "/#", ZOTIFY_WITH_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new ZotifyDbHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);

        switch (match) {
            case ZOTIFY: {
                retCursor = db.query(ZotifyContract.NotificationEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case ZOTIFY_GENERAL: {
                retCursor = db.query(ZotifyContract.NotificationEntry.TABLE_NAME,
                        projection,
                        ZotifyContract.NotificationEntry.COLUMN_NOTIF_TYPE + " = ? AND " +
                                ZotifyContract.NotificationEntry.COLUMN_NOTIF_COURSE + " = ?",
                        new String[]{"general", Utilities.getPreferredCourse(getContext())},
                        null,
                        null,
                        ZotifyContract.NotificationEntry._ID + " DESC",
                        "20"
                );
                break;
            }
            case ZOTIFY_SUBJECTS: {
                retCursor = db.query(ZotifyContract.NotificationEntry.TABLE_NAME,
                        projection,
                        ZotifyContract.NotificationEntry.COLUMN_NOTIF_TYPE + " != ? AND " +
                                ZotifyContract.NotificationEntry.COLUMN_NOTIF_COURSE + " = ?",
                        new String[]{"general", Utilities.getPreferredCourse(getContext())},
                        null,
                        null,
                        ZotifyContract.NotificationEntry._ID + " DESC",
                        "20"
                );
                break;
            }

            case ZOTIFY_WITH_ID: {
                String id = uri.getPathSegments().get(1);
                retCursor = db.query(ZotifyContract.NotificationEntry.TABLE_NAME,
                        projection,
                        ZotifyContract.NotificationEntry._ID + " = ?",
                        new String[]{id},
                        null,
                        null,
                        null
                );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ZOTIFY:
                return ZotifyContract.NotificationEntry.CONTENT_TYPE;
            case ZOTIFY_GENERAL:
                return ZotifyContract.NotificationEntry.CONTENT_TYPE;
            case ZOTIFY_SUBJECTS:
                return ZotifyContract.NotificationEntry.CONTENT_TYPE;
            case ZOTIFY_WITH_ID:
                return ZotifyContract.NotificationEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case ZOTIFY: {
                long _id = db.insert(ZotifyContract.NotificationEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = ZotifyContract.NotificationEntry.buildNotificationUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ZOTIFY:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(ZotifyContract.NotificationEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        int rowsDeleted;

        if (selection == null)
            selection = "1";

        switch (sUriMatcher.match(uri)) {
            case ZOTIFY: {
                rowsDeleted = db.delete(ZotifyContract.NotificationEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
            }
            break;

            case ZOTIFY_WITH_ID: {
                String id = uri.getPathSegments().get(1);
                rowsDeleted = db.delete(ZotifyContract.NotificationEntry.TABLE_NAME,
                        ZotifyContract.NotificationEntry._ID + " = ?",
                        new String[]{id}
                );
            }
            break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rowsUpdated;

        switch (sUriMatcher.match(uri)) {
            case ZOTIFY: {
                rowsUpdated = db.update(ZotifyContract.NotificationEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Failed to update rows from " + uri);
        }

        if (rowsUpdated != 0 || selection == null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }
}
