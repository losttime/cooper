package com.dconstructing.cooper.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.dconstructing.cooper.MainActivity;
import com.dconstructing.cooper.R;
import com.dconstructing.cooper.database.CooperOpenHelper;
import com.dconstructing.cooper.objects.Connection;

/**
 * Created by dcox on 10/10/13.
 */
public class ConnectionAdapter extends CursorAdapter {

    public final String TAG = getClass().getSimpleName();

    private Cursor mCursor;
    private Context mContext;
    private final LayoutInflater mInflater;


    public ConnectionAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
        mInflater = LayoutInflater.from(context);
        mContext = context;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        long entryId = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
        if (MainActivity.isDebuggable) Log.i(TAG, "Item created:" + Long.toString(entryId));

        TextView mainText = (TextView)view.findViewById(android.R.id.text1);
        mainText.setText(cursor.getString(cursor.getColumnIndex(CooperOpenHelper.USERNAME_FIELD_NAME)));

        TextView secondaryText = (TextView)view.findViewById(android.R.id.text2);
        secondaryText.setText(cursor.getString(cursor.getColumnIndex(CooperOpenHelper.HOST_FIELD_NAME)));

        MainActivity activity = (MainActivity) context;
        if (MainActivity.isDebuggable) Log.i(TAG, "Map:" + activity.mConnectionsStatus.toString());
        if (activity.mConnectionsStatus.containsKey(entryId)) {
            if (MainActivity.isDebuggable) Log.i(TAG, "Map contains:" + Long.toString(entryId));
            switch(activity.mConnectionsStatus.get(entryId)) {
                case Connection.CONNECTING:
                    view.setBackgroundColor(Color.argb(10,255,255,0));
                    break;
                case Connection.CONNECTED:
                    view.setBackgroundColor(Color.argb(10,0,255,0));
                    break;
                case Connection.DISCONNECTING:
                    view.setBackgroundColor(Color.argb(10,255,255,0));
                    break;
                default:
                    view.setBackgroundColor(Color.argb(10,255,0,0));
                    break;
            }
        } else {
            if (MainActivity.isDebuggable) Log.i(TAG, "Map does not contain:" + Long.toString(entryId));
            view.setBackgroundColor(Color.argb(10,255,0,0));
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final View view = mInflater.inflate(android.R.layout.simple_list_item_2, parent, false);
        return view;
    }

}