package com.zuccessful.zotify.sync;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.zuccessful.zotify.R;
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
 * Created by Chirag on 24-Oct-15.
 */
public class SyncUtilities {

    private static final String LOG_TAG = ZotifySyncAdapter.class.getSimpleName();

    public static void fetchJsonCourses(Context context){
        Log.d(LOG_TAG, "Starting Courses Sync");

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String zotifyCoursesJsonStr = null;

        try {
            final String ZOTIFY_COURSES_BASE_URL = context.getString(R.string.sync_courses_url);
            URL url = new URL(ZOTIFY_COURSES_BASE_URL);

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream is = urlConnection.getInputStream();

            if(is == null) {
                return;
            }

            StringBuffer buffer = new StringBuffer();
            reader = new BufferedReader(new InputStreamReader(is));

            String line;

            while ((line = reader.readLine()) != null){
                buffer.append(line + "\n");
            }

            if(buffer.length() == 0) {
                return;
            }

            zotifyCoursesJsonStr = buffer.toString();
            getCoursesFromJson(context, zotifyCoursesJsonStr);

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

    public static void getCoursesFromJson(Context context, String zotifyCoursesJsonStr) {
        final String ZOTIFY_SUCCESS = "success";
        final String ZOTIFY_MESSAGE = "message";
        final String ZOTIFY_COURSES = "courses";
        final String ZOTIFY_COURSE_ID = "courseId";
        final String ZOTIFY_COURSE_CODE = "courseCode";
        final String ZOTIFY_COURSE_NAME = "courseName";

        try {
            JSONObject zotifyCoursesJson = new JSONObject(zotifyCoursesJsonStr);
            JSONArray coursesArray = zotifyCoursesJson.getJSONArray(ZOTIFY_COURSES);

            Vector<ContentValues> cVector = new Vector<>(coursesArray.length());

            for (int i = 0; i < coursesArray.length(); i++) {
                ContentValues values = new ContentValues();
                JSONObject courseObject = coursesArray.getJSONObject(i);

                values.put(ZotifyContract.CoursesEntry._ID, courseObject.getString(ZOTIFY_COURSE_ID));
                values.put(ZotifyContract.CoursesEntry.COLUMN_COURSE_CODE, courseObject.getString(ZOTIFY_COURSE_CODE));
                values.put(ZotifyContract.CoursesEntry.COLUMN_COURSE_NAME, courseObject.getString(ZOTIFY_COURSE_NAME));

                cVector.add(values);
            }

            int inserted = 0;

            if(cVector.size() > 0) {
                ContentValues[] cArray = new ContentValues[cVector.size()];
                cVector.toArray(cArray);

                inserted = context.getContentResolver().bulkInsert(ZotifyContract.CoursesEntry.CONTENT_URI, cArray);
            }
            Log.d(LOG_TAG, "Courses Sync completed, " + inserted + " successful inserts.");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
