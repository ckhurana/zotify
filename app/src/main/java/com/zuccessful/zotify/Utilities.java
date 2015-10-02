package com.zuccessful.zotify;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

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

    public static String timeZonedList(String dateTime){

        Date currDate = new Date();
        String currDateStr, enteredDate = dateTime;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

        try {
            Date date = sdf.parse(dateTime);
            sdf = new SimpleDateFormat("yyyyMMdd", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("GMT+05:30"));

            currDateStr = sdf.format(currDate);
            enteredDate =  sdf.format(date);

            if(currDateStr.equals(enteredDate)){
                sdf = new SimpleDateFormat("hh:mm a", Locale.US);
                return sdf.format(date);
            }else {
                sdf = new SimpleDateFormat("MMM d", Locale.US);
                return sdf.format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateTime;

    }

    public static String timeZoneDetailView(String timeStr){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        try {
            Date date = sdf.parse(timeStr);
            sdf = new SimpleDateFormat("MMM d, HH:mm", Locale.ENGLISH);
            sdf.setTimeZone(TimeZone.getTimeZone("GMT+05:30"));
            timeStr = sdf.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timeStr;
    }

    public static void setActiveAppPref(Context context, boolean isActive){
        SharedPreferences sp = context.getSharedPreferences(context.getString(R.string.active_app_pref_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(context.getString(R.string.is_app_active), isActive);
        editor.apply();
    }

    public static boolean getActiveAppPref(Context context){
        SharedPreferences sp = context.getSharedPreferences(context.getString(R.string.active_app_pref_key), Context.MODE_PRIVATE);
        return sp.getBoolean(context.getString(R.string.is_app_active), false);
    }

    public static void setLastNotifIdPref(Context context, long id){
        SharedPreferences sp = context.getSharedPreferences(context.getString(R.string.last_notif_id_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(context.getString(R.string.last_notif_id_value), id);
        editor.apply();
    }
    public static long getLastNotifIdPref(Context context){
        SharedPreferences sp = context.getSharedPreferences(context.getString(R.string.last_notif_id_key), Context.MODE_PRIVATE);
        return sp.getLong(context.getString(R.string.last_notif_id_value), 0);
    }

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
