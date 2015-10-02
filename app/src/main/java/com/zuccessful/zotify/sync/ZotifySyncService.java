package com.zuccessful.zotify.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by Chirag Khurana on 01-Sep-15.
 */
public class ZotifySyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static ZotifySyncAdapter sNotifySyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d("NotifySync", "onCreate - ZotifySyncService");
        synchronized (sSyncAdapterLock) {
            if (sNotifySyncAdapter == null) {
                sNotifySyncAdapter = new ZotifySyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sNotifySyncAdapter.getSyncAdapterBinder();
    }
}
