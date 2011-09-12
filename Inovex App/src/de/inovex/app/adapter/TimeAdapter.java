package de.inovex.app.adapter;

import java.util.Date;

import de.inovex.app.R;
import de.inovex.app.activities.ListTimeActivity;
import de.inovex.app.provider.InovexContentProvider;
import android.content.Context;
import android.database.Cursor;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.RelativeLayout;

public class TimeAdapter extends CursorAdapter {

	private static final String TAG = "TimeAdapter";

	private static final String[] PROJECTION_IDS_AND_TITLE = new String[] {
        InovexContentProvider.Columns.ID, 
        InovexContentProvider.Columns.START_DATE,
        InovexContentProvider.Columns.END_DATE,
        InovexContentProvider.Columns.DESCRIPTION
	};

    private LayoutInflater mInflater;
    private int mIndexStartDate;
    private int mIndexEndDate;
    private int mIndexDescription;

	public TimeAdapter(ListTimeActivity activity) {
		super(activity, activity.managedQuery(InovexContentProvider.CONTENT_URI_TIMES, PROJECTION_IDS_AND_TITLE, null,
				null, InovexContentProvider.Columns.START_DATE + " desc"), true);

		final Cursor c = getCursor();
        mIndexStartDate = c.getColumnIndex(InovexContentProvider.Columns.START_DATE);
        mIndexEndDate = c.getColumnIndex(InovexContentProvider.Columns.END_DATE);
        mIndexDescription = c.getColumnIndex(InovexContentProvider.Columns.DESCRIPTION);

        mInflater = LayoutInflater.from(activity);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TimeViewHolder holder = (TimeViewHolder) view.getTag();
		CharSequence formattedDate = DateFormat.format("dd.MM.yyyy", new Date(cursor.getLong(mIndexStartDate)));
		holder.date.setText(formattedDate);
		CharSequence formattedStartTime = DateFormat.format("hh:mm", new Date(cursor.getLong(mIndexStartDate)));
		holder.startTime.setText(formattedStartTime);
		CharSequence formattedEndTime = DateFormat.format("hh:mm", new Date(cursor.getLong(mIndexEndDate)));
		holder.endTime.setText(formattedEndTime);
		Log.d(TAG, "end time: " + cursor.getLong(mIndexEndDate) + ", start time: " 
				+ cursor.getLong(mIndexStartDate) + ", total time: " + (cursor.getLong(mIndexEndDate) - cursor.getLong(mIndexStartDate)));
		CharSequence formattedTotalTime = DateFormat.format("hh:mm", new Date(cursor.getLong(mIndexEndDate) - cursor.getLong(mIndexStartDate)));
		holder.totalTime.setText(formattedTotalTime);
		holder.description.setText(cursor.getString(mIndexDescription));		
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		final RelativeLayout view = (RelativeLayout) mInflater.inflate(R.layout.time_list_element, parent, false);
		TimeViewHolder holder = new TimeViewHolder(view);
		view.setTag(holder);
		return view;
	}
}
