package de.inovex.app.adapter;

import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.RelativeLayout;
import de.inovex.app.R;
import de.inovex.app.activities.ListJourneyActivity;
import de.inovex.app.provider.InovexContentProvider;

public class JourneyAdapter extends CursorAdapter {
	
    private static final String[] PROJECTION_IDS_AND_TITLE = new String[] {
        InovexContentProvider.Columns.ID, 
        InovexContentProvider.Columns.START_DATE,
        InovexContentProvider.Columns.END_DATE,
        InovexContentProvider.Columns.START_LOCATION,
        InovexContentProvider.Columns.DESTINATION,
        InovexContentProvider.Columns.DESCRIPTION,
        InovexContentProvider.Columns.TYPE
    };

    private LayoutInflater mInflater;
    private int mIndexStartDate;
    private int mIndexEndDate;
    private int mIndexStartLocation;
    private int mIndexDestination;
    private int mIndexDescription;
    private int mIndexType;
    private int mIndexId;
    

	public JourneyAdapter(ListJourneyActivity activity) {
        super(activity, activity.managedQuery(InovexContentProvider.CONTENT_URI_JOURNEYS,
                PROJECTION_IDS_AND_TITLE,
                null, null, InovexContentProvider.Columns.START_DATE + " desc"), true);

        final Cursor c = getCursor();
        
        mIndexStartDate = c.getColumnIndex(InovexContentProvider.Columns.START_DATE);
        mIndexEndDate = c.getColumnIndex(InovexContentProvider.Columns.END_DATE);
        mIndexStartLocation = c.getColumnIndex(InovexContentProvider.Columns.START_LOCATION);
        mIndexDestination = c.getColumnIndex(InovexContentProvider.Columns.DESTINATION);
        mIndexDescription = c.getColumnIndex(InovexContentProvider.Columns.DESCRIPTION);
        mIndexType = c.getColumnIndex(InovexContentProvider.Columns.TYPE);
        mIndexId = c.getColumnIndex(InovexContentProvider.Columns.ID);
        mInflater = LayoutInflater.from(activity);

	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		JourneyViewHolder holder = (JourneyViewHolder) view.getTag();
		
		
		String type = cursor.getString(mIndexType);
		holder.type.setText(InovexContentProvider.Types.getDisplayStringFromType(context, type));
		CharSequence formattedDate = DateFormat.format("MMM dd, yy k:mm", new Date(cursor.getLong(mIndexStartDate)));
		holder.startDate.setText(formattedDate);
		holder.start.setText(cursor.getString(mIndexStartLocation));
		formattedDate = DateFormat.format("MMM dd, yy k:mm", new Date(cursor.getLong(mIndexEndDate)));
		holder.endDate.setText(formattedDate);		
		holder.destination.setText(cursor.getString(mIndexDestination));
		String descr = cursor.getString(mIndexDescription);
		if (descr!=null && descr.length()!=0) {
			holder.description.setVisibility(View.VISIBLE);
			holder.description.setText(descr);		
		} else {
			holder.description.setVisibility(View.GONE);			
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final RelativeLayout view = (RelativeLayout) mInflater.inflate(R.layout.journey_list_element, parent, false);

        JourneyViewHolder holder = new JourneyViewHolder(view);

        view.setTag(holder);

        return view;
	}

//	@Override
//	public Object getItem(int position) {
//		Cursor c = getCursor();
//		c.moveToPosition(position);
//		int id = c.getInt(mIndexId);
//		Uri
//		return super.getItem(position);
//	}
}
