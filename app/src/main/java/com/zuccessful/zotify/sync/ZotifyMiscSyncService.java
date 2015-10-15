package com.zuccessful.zotify.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by Chirag on 15-Oct-15.
 */
public class ZotifyMiscSyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static ZotifyMiscSyncAdapter sZotifyMiscSyncAdapter = null;
    private String LOG_TAG = ZotifyMiscSyncService.class.getSimpleName();

    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "onCreate - ZotifyMiscSyncService");
        synchronized (sSyncAdapterLock) {
            if (sZotifyMiscSyncAdapter == null) {
                sZotifyMiscSyncAdapter = new ZotifyMiscSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sZotifyMiscSyncAdapter.getSyncAdapterBinder();
    }
}
