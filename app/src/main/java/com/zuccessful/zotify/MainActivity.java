package com.zuccessful.zotify;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.zuccessful.zotify.data.ZotifyContract;
import com.zuccessful.zotify.sync.ZotifySyncAdapter;

public class MainActivity extends AppCompatActivity {

    private static int mDefTabIndex = 0;
    private static String mDefCourse;
    public static boolean isReCreated = false;         //for setting up correct tab
    Toolbar toolbar;
    TabLayout tabLayout;
    ViewPager viewPager;
    PagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDefCourse = Utilities.getPreferredCourse(this);

        pagerAdapter = new PagerAdapter(getSupportFragmentManager());

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tabLayout = (TabLayout) findViewById(R.id.tab_layout);

        viewPager = (ViewPager) findViewById(R.id.view_pager);

        viewPager.setAdapter(pagerAdapter);

        tabLayout.setTabsFromPagerAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        onFirstLaunch();

        ZotifySyncAdapter.initializeSyncAdapter(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utilities.setActiveAppPref(this, true);
        clearNotifs();
        onPreferenceChanged();
    }

    public void onFirstLaunch(){
        if(Utilities.getFirstLaunchPref(this)){
            Utilities.setFirstLaunchPref(this, false);
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            sp.edit().putString(getString(R.string.pref_course_key), getString(R.string.pref_course_btech_cs_value)).apply();
            resetData();

            Toast.makeText(MainActivity.this, "Select your Preferred Course from the App Settings", Toast.LENGTH_LONG).show();
        }
    }

    public void clearNotifs(){
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(ZotifySyncAdapter.ZOTIFY_NOTIFICATION_ID);
    }

    public void onPreferenceChanged() {
        int preferredFreq = Integer.valueOf(Utilities.getPreferredFreq(this));
        if (preferredFreq != ZotifySyncAdapter.SYNC_INTERVAL) {
            ZotifySyncAdapter.SYNC_INTERVAL = preferredFreq;
            ZotifySyncAdapter.SYNC_FLEXTIME = preferredFreq / 3;
            ZotifySyncAdapter.configurePeriodicSync(this, ZotifySyncAdapter.SYNC_INTERVAL, ZotifySyncAdapter.SYNC_FLEXTIME);
            ZotifySyncAdapter.syncImmediately(this);
        }


        if(!isReCreated){
            int defTab = Integer.valueOf(Utilities.getPreferredTab(this));
            if (mDefTabIndex != defTab) {
                mDefTabIndex = defTab;
            }
            isReCreated = true;
        }
        viewPager.setCurrentItem(mDefTabIndex);


        String defCourse = Utilities.getPreferredCourse(this);
        if(! mDefCourse.equals(defCourse)){
            mDefCourse = defCourse;
            getContentResolver().delete(ZotifyContract.NotificationEntry.CONTENT_URI, null, null);
            Utilities.setLastNotifIdPref(this, 0);
            ZotifySyncAdapter.syncImmediately(this);
        }
    }

    public void resetData(){
        getContentResolver().delete(ZotifyContract.NotificationEntry.CONTENT_URI, null, null);
        getContentResolver().delete(ZotifyContract.CoursesEntry.CONTENT_URI, null, null);
        Utilities.setLastNotifIdPref(this, 0);
        Utilities.setCourseUpdatePref(this, 0);
        ZotifySyncAdapter.syncImmediately(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDefTabIndex = viewPager.getCurrentItem();
        Utilities.setActiveAppPref(this, false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        if(id == R.id.action_add){
            startActivity(new Intent(this, AddNotif.class));
            return true;
        }

        if (id == R.id.action_refresh) {
            ZotifySyncAdapter.syncImmediately(this);
        }
        if (id == R.id.action_reset) {
            resetData();
        }

        return super.onOptionsItemSelected(item);
    }

    class PagerAdapter extends FragmentPagerAdapter {

        Fragment fragment = null;

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                fragment = new GeneralFragment();
                return fragment;
            } else if (position == 1) {
                fragment = new SubjectsFragment();
                return fragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return "GENERAL";
            } else if (position == 1) {
                return "SUBJECTS";
            }
            return null;
        }
    }
}
