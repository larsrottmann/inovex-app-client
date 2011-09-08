package de.inovex.app.adapter;

import java.util.Date;

import de.inovex.app.R;
import de.inovex.app.activities.ListTimeActivity;
import de.inovex.app.provider.InovexContentProvider;
import android.content.Context;
import android.database.Cursor;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.RelativeLayout;

public class TimeAdapter extends CursorAdapter {

	private static final String[] PROJECTION_IDS_AND_TITLE = new String[] {
        InovexContentProvider.Columns.ID, 
        InovexContentProvider.Columns.DATE,
        InovexContentProvider.Columns.START_TIME,
        InovexContentProvider.Columns.END_TIME,
        InovexContentProvider.Columns.DESCRIPTION
	};

    private LayoutInflater mInflater;
    private int mIndexDate;
    private int mIndexStartTime;
    private int mIndexEndTime;
    private int mIndexDescription;

	public TimeAdapter(ListTimeActivity activity) {
		super(activity, activity.managedQuery(InovexContentProvider.CONTENT_URI_TIMES, PROJECTION_IDS_AND_TITLE, null,
				null, InovexContentProvider.Columns.DATE + " desc"), true);

		final Cursor c = getCursor();
        mIndexDate = c.getColumnIndex(InovexContentProvider.Columns.DATE);
        mIndexStartTime = c.getColumnIndex(InovexContentProvider.Columns.START_TIME);
        mIndexEndTime = c.getColumnIndex(InovexContentProvider.Columns.END_TIME);
        mIndexDescription = c.getColumnIndex(InovexContentProvider.Columns.DESCRIPTION);

        mInflater = LayoutInflater.from(activity);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TimeViewHolder holder = (TimeViewHolder) view.getTag();
		CharSequence formattedDate = DateFormat.format("MMM dd, yyyy", new Date(cursor.getLong(mIndexDate)));
		holder.date.setText(formattedDate);
		CharSequence formattedStartTime = DateFormat.format("h:mmaa", new Date(cursor.getLong(mIndexStartTime)));
		holder.startTime.setText(formattedStartTime);
		CharSequence formattedEndTime = DateFormat.format("h:mmaa", new Date(cursor.getLong(mIndexEndTime)));
		holder.endTime.setText(formattedEndTime);
		CharSequence formattedTotalTime = DateFormat.format("h:mmaa", new Date(cursor.getLong(mIndexEndTime) - cursor.getLong(mIndexStartTime)));
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
