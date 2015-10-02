package com.zuccessful.zotify.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Chirag Khurana on 01-Sep-15.
 */
public class ZotifyAuthenticatorService extends Service {
    private ZotifyAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        mAuthenticator = new ZotifyAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
