package com.zuccessful.zotify;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

/**
 * Created by Chirag Khurana on 01-Sep-15.
 */
public class ZotifyAdapter extends CursorAdapter {
    public ZotifyAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_main, parent, false);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView titleView = (TextView) view.findViewById(R.id.title_text);
        TextView typeView = (TextView) view.findViewById(R.id.type_alpha);
        TextView priorityView = (TextView) view.findViewById(R.id.priority);
        TextView timeView = (TextView) view.findViewById(R.id.timeStamp);

        String title = cursor.getString(GeneralFragment.COL_NOTIF_TITLE);
        String priorityCode = cursor.getString(GeneralFragment.COL_NOTIF_PRIORITY);
        String typeName = cursor.getString(GeneralFragment.COL_NOTIF_TYPE_NAME);
        String timeStamp = cursor.getString(GeneralFragment.COL_NOTIF_TIME);

        String time_type = String.format(context.getString(R.string.format_date_type), Utilities.timeNormalized(timeStamp, true), typeName);

        String priority = String.valueOf(Utilities.getPriorityString(priorityCode).charAt(0)).toUpperCase();

        titleView.setText(title);
        priorityView.setText(priority);
        typeView.setText(String.valueOf(typeName.charAt(0)).toUpperCase());
        timeView.setText(time_type);
    }
}
