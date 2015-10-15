package com.zuccessful.zotify.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.zuccessful.zotify.Utilities;
import com.zuccessful.zotify.data.ZotifyContract.NotificationEntry;
import com.zuccessful.zotify.data.ZotifyContract.CoursesEntry;
import com.zuccessful.zotify.sync.ZotifySyncAdapter;

/**
 * Created by Chirag Khurana on 31-Aug-15.
 */
public class ZotifyDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "zotify.db";
    private static final int DATABASE_VERSION = 5;
    private static Context mContext;

    public ZotifyDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String queryNotifications = "CREATE TABLE " + NotificationEntry.TABLE_NAME + " (" +
                NotificationEntry._ID + " INTEGER PRIMARY KEY," +
                NotificationEntry.COLUMN_NOTIF_COURSE + " TEXT NOT NULL," +
                NotificationEntry.COLUMN_NOTIF_TYPE + " TEXT NOT NULL," +
                NotificationEntry.COLUMN_NOTIF_TYPE_NAME + " VARCHAR(100) NOT NULL," +
                NotificationEntry.COLUMN_NOTIF_AUTHOR + " VARCHAR(30) NOT NULL," +
                NotificationEntry.COLUMN_NOTIF_TITLE + " TEXT NOT NULL," +
                NotificationEntry.COLUMN_NOTIF_DESC + " TEXT NOT NULL," +
                NotificationEntry.COLUMN_NOTIF_PRIORITY + " CHAR NOT NULL," +
                NotificationEntry.COLUMN_NOTIF_TIME + " TIME NOT NULL" +
                ");";

        String queryCourses = "CREATE TABLE " + CoursesEntry.TABLE_NAME + " (" +
                CoursesEntry._ID + " INTEGER PRIMARY KEY," +
                NotificationEntry.COLUMN_NOTIF_TYPE + " TEXT NOT NULL," +
                CoursesEntry.COLUMN_COURSE_CODE + " VARCHAR(100) NOT NULL," +
                CoursesEntry.COLUMN_COURSE_NAME + " VARCHAR(100) NOT NULL," +
                ");";
        db.execSQL(queryNotifications);
        db.execSQL(queryCourses);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + NotificationEntry.TABLE_NAME + ";");
        db.execSQL("DROP TABLE IF EXISTS " + CoursesEntry.TABLE_NAME + ";");
        Utilities.setLastNotifIdPref(mContext, 0);
        onCreate(db);
        ZotifySyncAdapter.syncImmediately(mContext);
    }
}
