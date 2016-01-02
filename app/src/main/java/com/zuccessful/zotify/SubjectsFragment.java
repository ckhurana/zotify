package com.zuccessful.zotify;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.zuccessful.zotify.data.ZotifyContract;

/**
 * Created by Chirag Khurana on 01-Sep-15.
 */
public class SubjectsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private ListView zotifyListView;
    public static ZotifyAdapter mZotifyAdapter;
    private static final int ZOTIFY_LOADER = 0;

    public SubjectsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        mZotifyAdapter = new ZotifyAdapter(getActivity(), null, 0);
        zotifyListView = (ListView) view.findViewById(R.id.list_view);
        zotifyListView.setAdapter(mZotifyAdapter);
        zotifyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);

                Intent intent = new Intent(getActivity(), DetailsActivity.class);
                intent.setData(ZotifyContract.NotificationEntry.buildNotifyUriWithId(cursor.getLong(GeneralFragment.COL_NOTIF_ID)));
                startActivity(intent);
            }
        });

        zotifyListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        zotifyListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                final int checkedCount = zotifyListView.getCheckedItemCount();
                mode.setTitle(checkedCount + " Selected");
                mZotifyAdapter.toggleSelection(position);
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.menu_selection, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.delete_items:
                        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

                        alert.setTitle("Delete");
                        alert.setMessage("Do you want to delete the selected notification(s)?");
                        alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Uri uri;
                                SparseBooleanArray selected = mZotifyAdapter.getmSelectedItemsIds();
                                for (int i = selected.size() - 1; i >= 0; i--) {
                                    if (selected.valueAt(i)) {
                                        Cursor cursor = (Cursor) mZotifyAdapter.getItem(selected.keyAt(i));
                                        uri = ZotifyContract.NotificationEntry.buildNotifyUriWithId(cursor.getLong(GeneralFragment.COL_NOTIF_ID));
                                        mZotifyAdapter.remove(uri);
                                    }
                                }
                                getLoaderManager().restartLoader(ZOTIFY_LOADER, null, SubjectsFragment.this);
                                mode.finish();
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
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                mZotifyAdapter.removeSelection();
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(ZOTIFY_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = ZotifyContract.NotificationEntry.CONTENT_URI.buildUpon().appendPath(ZotifyContract.SUBJECTS).build();
        return new CursorLoader(getActivity(), uri, GeneralFragment.NOTIF_COLUMNS, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mZotifyAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //mZotifyAdapter.swapCursor(null);
    }
}

