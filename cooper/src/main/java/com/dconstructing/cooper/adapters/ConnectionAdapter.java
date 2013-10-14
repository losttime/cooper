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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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

        RelativeLayout indicatorContainer = (RelativeLayout)view.findViewById(R.id.connection_status);
        ProgressBar progressBar = (ProgressBar)indicatorContainer.findViewById(R.id.progressBar);

        MainActivity activity = (MainActivity) context;
        if (MainActivity.isDebuggable) Log.i(TAG, "Map:" + activity.mConnectionsStatus.toString());
        if (activity.mConnectionsStatus.containsKey(entryId)) {
            if (MainActivity.isDebuggable) Log.i(TAG, "Map contains:" + Long.toString(entryId));
            switch(activity.mConnectionsStatus.get(entryId)) {
                case Connection.CONNECTING:
                    indicatorContainer.setBackgroundColor(Color.argb(10,255,255,0));
                    progressBar.setVisibility(View.VISIBLE);
                    break;
                case Connection.CONNECTED:
                    indicatorContainer.setBackgroundColor(Color.argb(10,0,255,0));
                    progressBar.setVisibility(View.INVISIBLE);
                    break;
                case Connection.DISCONNECTING:
                    indicatorContainer.setBackgroundColor(Color.argb(10,255,255,0));
                    progressBar.setVisibility(View.VISIBLE);
                    break;
                default:
                    indicatorContainer.setBackgroundColor(Color.argb(10,255,0,0));
                    progressBar.setVisibility(View.INVISIBLE);
                    break;
            }
        } else {
            if (MainActivity.isDebuggable) Log.i(TAG, "Map does not contain:" + Long.toString(entryId));
            indicatorContainer.setBackgroundColor(Color.argb(10,255,0,0));
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final View view = mInflater.inflate(R.layout.connection_list_item, parent, false);
        return view;
    }

}