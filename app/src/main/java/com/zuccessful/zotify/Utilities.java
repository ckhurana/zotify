package com.zuccessful.zotify;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;

import com.zuccessful.zotify.data.ZotifyContract;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Vector;

/**
 * Created by Chirag Khurana on 02-Sep-15.
 */
public class Utilities {
    static public String getPriorityString(String priorityCode){
        String priority = "";
        switch (priorityCode){
            case "1":
                priority = "High Priority";
                break;
            case "2":
                priority = "Medium Priority";
                break;
            case "3":
                priority = "Low Priority";
        }
        return priority;
    }

    public static String timeNormalized(String inputTimestamp, boolean isListView){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        if(isListView){
            try {
                Date currDate = new Date();

                sdf.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
                Date date = sdf.parse(inputTimestamp);

                sdf = new SimpleDateFormat("yyyyMMdd", Locale.US);
                inputTimestamp = sdf.format(date);
                String currDateStr = sdf.format(currDate);


                if(currDateStr.equals(inputTimestamp)){
                    sdf = new SimpleDateFormat("hh:mm a", Locale.US);
                    return sdf.format(date);
                }else {
                    sdf = new SimpleDateFormat("MMM d", Locale.US);
                    return sdf.format(date);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            sdf.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
            try {
                Date date = sdf.parse(inputTimestamp);
                sdf = new SimpleDateFormat("MMM d, HH:mm", Locale.US);
                sdf.setTimeZone(TimeZone.getTimeZone("GMT+05:30"));
                inputTimestamp = sdf.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return inputTimestamp;
    }

    public static CharSequence[] getCourseCodes(Context context){
        Vector<String> v = new Vector<>();

        Cursor cursor = context.getContentResolver().query(ZotifyContract.CoursesEntry.CONTENT_URI, null, null, null, null);

        if(cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                v.add(cursor.getString(cursor.getColumnIndex(ZotifyContract.CoursesEntry.COLUMN_COURSE_CODE)));
                cursor.moveToNext();
            }
        }
        CharSequence[] cs = new CharSequence[v.size()];
        v.toArray(cs);
        return cs;
    }

    public static CharSequence[] getCourseNames(Context context){
        Vector<String> v = new Vector<>();

        Cursor cursor = context.getContentResolver().query(ZotifyContract.CoursesEntry.CONTENT_URI, null, null, null, null);

        if(cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                v.add(cursor.getString(cursor.getColumnIndex(ZotifyContract.CoursesEntry.COLUMN_COURSE_NAME)));
                cursor.moveToNext();
            }
        }
        CharSequence[] cs = new CharSequence[v.size()];
        v.toArray(cs);
        return cs;
    }


    public static void setActiveAppPref(Context context, boolean isActive){
        SharedPreferences sp = context.getSharedPreferences(context.getString(R.string.utilities_pref_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(context.getString(R.string.is_app_foreground), isActive);
        editor.apply();
    }

    public static boolean getActiveAppPref(Context context){
        SharedPreferences sp = context.getSharedPreferences(context.getString(R.string.utilities_pref_key), Context.MODE_PRIVATE);
        return sp.getBoolean(context.getString(R.string.is_app_foreground), false);
    }

    public static void setLastNotifIdPref(Context context, long id){
        SharedPreferences sp = context.getSharedPreferences(context.getString(R.string.utilities_pref_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(context.getString(R.string.last_synced_notif), id);
        editor.apply();
    }
    public static long getLastNotifIdPref(Context context){
        SharedPreferences sp = context.getSharedPreferences(context.getString(R.string.utilities_pref_key), Context.MODE_PRIVATE);
        return sp.getLong(context.getString(R.string.last_synced_notif), 0);
    }

    public static void setFirstLaunchPref(Context context, boolean isFirst){
        SharedPreferences sp = context.getSharedPreferences(context.getString(R.string.utilities_pref_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(context.getString(R.string.is_first_launch), isFirst);
        editor.apply();
    }
    public static boolean getFirstLaunchPref(Context context){
        SharedPreferences sp = context.getSharedPreferences(context.getString(R.string.utilities_pref_key), Context.MODE_PRIVATE);
        return sp.getBoolean(context.getString(R.string.is_first_launch), true);
    }

    public static void setSuccessCodePref(Context context, String code){
        SharedPreferences sp = context.getSharedPreferences(context.getString(R.string.utilities_pref_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(context.getString(R.string.success_code_sp), code);
        editor.apply();
    }
    public static String getSuccessCodePref(Context context) {
        SharedPreferences sp = context.getSharedPreferences(context.getString(R.string.utilities_pref_key), Context.MODE_PRIVATE);
        return sp.getString(context.getString(R.string.success_code_sp), "1");
    }

    public static void setSessionPref(Context context, String session){
        SharedPreferences sp = context.getSharedPreferences(context.getString(R.string.utilities_pref_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(context.getString(R.string.session_sp), session);
        editor.apply();
    }
    public static String getSessionPref(Context context) {
        SharedPreferences sp = context.getSharedPreferences(context.getString(R.string.utilities_pref_key), Context.MODE_PRIVATE);
        return sp.getString(context.getString(R.string.session_sp), "2015b");
    }

    public static void setCourseUpdatePref(Context context, int count){
        SharedPreferences sp = context.getSharedPreferences(context.getString(R.string.utilities_pref_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(context.getString(R.string.course_update), count);
        editor.apply();
    }
    public static int getCourseUpdatePref(Context context) {
        SharedPreferences sp = context.getSharedPreferences(context.getString(R.string.utilities_pref_key), Context.MODE_PRIVATE);
        return sp.getInt(context.getString(R.string.course_update), 0);
    }


    // --------- Setting's Default Shared Preferences ----------

    public static String getPreferredFreq(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(context.getString(R.string.pref_sync_freq_key),
                context.getString(R.string.pref_notif_1));
    }

    public static String getPreferredTab(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(context.getString(R.string.pref_tab_key),
                context.getString(R.string.pref_tab_gen_v));
    }

    public static Boolean getPreferredNotificationSetting(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(context.getString(R.string.pref_enable_notifications_key),
                Boolean.parseBoolean(context.getString(R.string.pref_enable_notifications_default)));
    }

    public static String getPreferredCourse(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(context.getString(R.string.pref_course_key),
                context.getString(R.string.pref_course_btech_cs_value));
    }
}
