package com.zuccessful.zotify.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.zuccessful.zotify.MainActivity;
import com.zuccessful.zotify.R;
import com.zuccessful.zotify.Utilities;
import com.zuccessful.zotify.data.ZotifyContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

/**
 * Created by Chirag Khurana on 01-Sep-15.
 */
public class ZotifySyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String LOG_TAG = ZotifySyncAdapter.class.getSimpleName();
    public static final int ZOTIFY_NOTIFICATION_ID = 3105;
    // Interval at which to sync with the weather, in seconds.
    // 60 seconds (1 minute) * 180 = 3 hours
    public static int SYNC_INTERVAL = 60 * 180;
    public static int SYNC_FLEXTIME = SYNC_INTERVAL / 3;


    public ZotifySyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    /**
     * Helper method to have the sync adapter sync immediately
     *
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if (null == accountManager.getPassword(newAccount)) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        ZotifySyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }


    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "Starting Sync");

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String notifyJsonStr = null;

        try {
            final String NOTIFY_BASE_URL = getContext().getString(R.string.sync_url);

            String courseParam = "course=";
            String selectedCourse = Utilities.getPreferredCourse(getContext());

            String lastIdParam = "lastId=";
            String lastId = Long.toString(Utilities.getLastNotifIdPref(getContext()));

            String NOTIFY_URL = NOTIFY_BASE_URL + courseParam + selectedCourse + '&' + lastIdParam + lastId;

            URL url = new URL(NOTIFY_URL);

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
//            urlConnection.setConnectTimeout(5000);
//            urlConnection.setReadTimeout(10000);
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null)
                return;

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                return;
            }

            notifyJsonStr = buffer.toString();
            getNotificationsFromJson(notifyJsonStr);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void getNotificationsFromJson(String zotifyJsonStr) {
        final String ZOTIFY_SUCCESS = "success";
        final String ZOTIFY_MESSAGE = "message";
        final String ZOTIFY_NOTIFICATIONS = "notifications";
        final String ZOTIFY_ID = "ID";
        final String ZOTIFY_COURSE = "course";
        final String ZOTIFY_TYPE = "type";
        final String ZOTIFY_TYPE_NAME = "type_name";
        final String ZOTIFY_AUTHOR = "author";
        final String ZOTIFY_TITLE = "title";
        final String ZOTIFY_DESCRIPTION = "description";
        final String ZOTIFY_PRIORITY = "priority";
        final String ZOTIFY_TIME = "timeStamp";

        long lastScannedId = 0;

        try {
            JSONObject zotifyJson = new JSONObject(zotifyJsonStr);

            //Check server msgs for database
            successCodeChanged(zotifyJson.getString(ZOTIFY_SUCCESS));

            JSONArray notificationsArray = zotifyJson.getJSONArray(ZOTIFY_NOTIFICATIONS);

            Vector<ContentValues> cVector = new Vector<ContentValues>(notificationsArray.length());

            for (int i = 0; i < notificationsArray.length(); i++) {
                ContentValues values = new ContentValues();
                JSONObject notificationObject = notificationsArray.getJSONObject(i);

                long id = notificationObject.getInt(ZOTIFY_ID);

                if (id > lastScannedId)
                    lastScannedId = id;

                String course = notificationObject.getString(ZOTIFY_COURSE);
                String type = notificationObject.getString(ZOTIFY_TYPE);
                String type_name = notificationObject.getString(ZOTIFY_TYPE_NAME);
                String author = notificationObject.getString(ZOTIFY_AUTHOR);
                String title = notificationObject.getString(ZOTIFY_TITLE);
                String description = notificationObject.getString(ZOTIFY_DESCRIPTION);
                String priority = notificationObject.getString(ZOTIFY_PRIORITY);
                String timestamp = notificationObject.getString(ZOTIFY_TIME);

                values.put(ZotifyContract.NotificationEntry._ID, id);
                values.put(ZotifyContract.NotificationEntry.COLUMN_NOTIF_COURSE, course);
                values.put(ZotifyContract.NotificationEntry.COLUMN_NOTIF_TYPE, type);
                values.put(ZotifyContract.NotificationEntry.COLUMN_NOTIF_TYPE_NAME, type_name);
                values.put(ZotifyContract.NotificationEntry.COLUMN_NOTIF_AUTHOR, author);
                values.put(ZotifyContract.NotificationEntry.COLUMN_NOTIF_TITLE, title);
                values.put(ZotifyContract.NotificationEntry.COLUMN_NOTIF_DESC, description);
                values.put(ZotifyContract.NotificationEntry.COLUMN_NOTIF_PRIORITY, priority);
                values.put(ZotifyContract.NotificationEntry.COLUMN_NOTIF_TIME, timestamp);

                cVector.add(values);
            }

            int inserted = 0;
            if (cVector.size() > 0) {
                Utilities.setLastNotifIdPref(getContext(), lastScannedId);

                ContentValues[] cArray = new ContentValues[cVector.size()];
                cVector.toArray(cArray);

                inserted = getContext().getContentResolver().bulkInsert(ZotifyContract.NotificationEntry.CONTENT_URI, cArray);

                if (inserted > 0) {
                    zotifyNotifs(inserted);
                }
            }
            Log.d(LOG_TAG, "Notifications Sync completed, " + inserted + " successful inserts.");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void successCodeChanged(String code) {
        Log.i(LOG_TAG, "Success code from Server: " + code);
        Context context = getContext();
        String savedCode = Utilities.getSuccessCodePref(context);

        if (savedCode != code) {
            if (savedCode.equals(context.getString(R.string.fail_reset_code)) && code.equals("1")) {
                Utilities.setSuccessCodePref(context, context.getString(R.string.success_reset_code));
            } else {
                Utilities.setSuccessCodePref(context, code);
            }

            if (!(savedCode.equals(context.getString(R.string.success_reset_code)) || savedCode.equals(context.getString(R.string.fail_reset_code))) &&
                    (code.equals(context.getString(R.string.success_reset_code))
                            || code.equals(context.getString(R.string.fail_reset_code)))) {
                Log.i(LOG_TAG, "Reset database on Server's Request");
                context.getContentResolver().delete(ZotifyContract.NotificationEntry.CONTENT_URI, null, null);
                Utilities.setLastNotifIdPref(context, 0);
                syncImmediately(context);
            }
        }
    }

    private void zotifyNotifs(int number) {
        Context context = getContext();

        if (Utilities.getPreferredNotificationSetting(context) && !Utilities.getActiveAppPref(context)) {
            String contentText = String.format(context.getString(R.string.format_notification), number);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.ic_stat_logo_base)
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText(contentText)
                    .setTicker(contentText)
                    .setAutoCancel(true)
                    .setColor(context.getResources().getColor(R.color.colorPrimary))
                    .setVibrate(new long[]{0, 400, 400, 400})
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));


            Intent resultIntent = new Intent(context, MainActivity.class);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                    0,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );
            mBuilder.setContentIntent(resultPendingIntent);

            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(ZOTIFY_NOTIFICATION_ID, mBuilder.build());

        }
    }
}
