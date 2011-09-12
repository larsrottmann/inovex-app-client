package de.inovex.app.adapter;

import java.util.Calendar;
import java.util.Date;

import de.inovex.app.R;
import de.inovex.app.activities.ListTimeActivity;
import de.inovex.app.provider.InovexContentProvider;
import de.inovex.app.util.DateUtil;
import android.content.Context;
import android.database.Cursor;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
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
		Date startDate = new Date(cursor.getLong(mIndexStartDate));
		Date endDate = new Date(cursor.getLong(mIndexEndDate));
		String formattedTotalTime = DateUtil.getFormattedTimeSaldo(startDate, endDate);
		CharSequence formattedDate = DateFormat.format("dd.MM.yyyy", startDate);
		holder.date.setText(formattedDate);
		CharSequence formattedStartTime = DateFormat.format("k:mm", startDate);
		holder.startTime.setText(formattedStartTime);
		CharSequence formattedEndTime = DateFormat.format("k:mm", endDate);
		holder.endTime.setText(formattedEndTime);
		Log.d(TAG, "end time: " + endDate + ", start time: " + startDate + ", total time: " + formattedTotalTime);
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
