package de.inovex.app.adapter;

import de.inovex.app.R;
import android.view.View;
import android.widget.TextView;

public class TimeViewHolder {

	TextView date;
	TextView startTime;
	TextView endTime;
	TextView totalTime;
	TextView description;

	public TimeViewHolder(View view) {
		date = (TextView) view.findViewById(R.id.textview_date);
		startTime = (TextView) view.findViewById(R.id.textview_starttime);
		endTime = (TextView) view.findViewById(R.id.textview_endtime);
		totalTime = (TextView) view.findViewById(R.id.textview_totaltime);
		description = (TextView) view.findViewById(R.id.textview_description);
	}
}
