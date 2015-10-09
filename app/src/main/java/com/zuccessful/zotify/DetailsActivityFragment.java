package com.zuccessful.zotify;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zuccessful.zotify.data.ZotifyContract;

public class DetailsActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    static final int COL_NOTIF_ID = 0;
    static final int COL_NOTIF_TYPE_NAME = 1;
    static final int COL_NOTIF_TITLE = 2;
    static final int COL_NOTIF_PRIORITY = 3;
    static final int COL_NOTIF_DESC = 4;
    static final int COL_NOTIF_TIME = 5;
    static final int COL_NOTIF_AUTHOR = 6;
    static final String[] DETAILS_COLUMNS = {
            ZotifyContract.NotificationEntry._ID,
            ZotifyContract.NotificationEntry.COLUMN_NOTIF_TYPE_NAME,
            ZotifyContract.NotificationEntry.COLUMN_NOTIF_TITLE,
            ZotifyContract.NotificationEntry.COLUMN_NOTIF_PRIORITY,
            ZotifyContract.NotificationEntry.COLUMN_NOTIF_DESC,
            ZotifyContract.NotificationEntry.COLUMN_NOTIF_TIME,
            ZotifyContract.NotificationEntry.COLUMN_NOTIF_AUTHOR
    };
    private static final int DETAIL_LOADER = 0;

    public DetailsActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_details, container, false);
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Intent intent = getActivity().getIntent();
        if (intent == null) {
            return null;
        }

        return new CursorLoader(getActivity(),
                intent.getData(),
                DETAILS_COLUMNS,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }

        TextView titleView = (TextView) getView().findViewById(R.id.title_view);
        TextView typeView = (TextView) getView().findViewById(R.id.type_text_view);
        TextView priorityView = (TextView) getView().findViewById(R.id.priority_text_view);
        TextView descView = (TextView) getView().findViewById(R.id.desc_view);
        TextView timeView = (TextView) getView().findViewById(R.id.time_text);
        TextView authorView = (TextView) getView().findViewById(R.id.author_text);

        String title = data.getString(COL_NOTIF_TITLE);
        String desc = data.getString(COL_NOTIF_DESC);
        String priority = Utilities.getPriorityString(data.getString(COL_NOTIF_PRIORITY));
        String type = data.getString(COL_NOTIF_TYPE_NAME);
        String timeStr = data.getString(COL_NOTIF_TIME);
        String author = data.getString(COL_NOTIF_AUTHOR);

        timeStr = Utilities.timeNormalized(timeStr, false);

        titleView.setText(title);
        descView.setText(desc);
        priorityView.setText(priority);
        typeView.setText(type);
        timeView.setText(timeStr);
        authorView.setText(author);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_details_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();


        if (id == R.id.delete_item) {

            final Uri uri = getActivity().getIntent().getData();

            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

            alert.setTitle("Delete");
            alert.setMessage("Do you want delete this notification?");
            alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    getActivity().getContentResolver().delete(uri, null, null);
                    NavUtils.navigateUpFromSameTask(getActivity());
                }
            });
            alert.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alert.show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
