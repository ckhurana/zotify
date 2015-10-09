package com.zuccessful.zotify.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Chirag Khurana on 31-Aug-15.
 */
public class ZotifyContract {
    public static final String CONTENT_AUTHORITY = "com.zuccessful.zotify";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_ZOTIFY = "zotify";
    public static final String GENERAL = "general";
    public static final String SUBJECTS = "subjects";
    public static final String PATH_GENERAL = PATH_ZOTIFY + "/" + GENERAL;
    public static final String PATH_SUBJECTS = PATH_ZOTIFY + "/" + SUBJECTS;


    public static final class NotificationEntry implements BaseColumns{

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ZOTIFY;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ZOTIFY;

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_ZOTIFY).build();

        public static final String TABLE_NAME = "notifications";
        public static final String COLUMN_NOTIF_COURSE = "course";
        public static final String COLUMN_NOTIF_TYPE = "type";
        public static final String COLUMN_NOTIF_TYPE_NAME = "type_name";
        public static final String COLUMN_NOTIF_AUTHOR = "author";
        public static final String COLUMN_NOTIF_PRIORITY = "priority";
        public static final String COLUMN_NOTIF_TITLE = "title";
        public static final String COLUMN_NOTIF_DESC = "description";
        public static final String COLUMN_NOTIF_TIME = "timeStamp";

        public static Uri buildNotificationUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildNotifyUriWithId(long id){
            return CONTENT_URI.buildUpon().appendPath(Long.toString(id)).build();
        }
    }
}
