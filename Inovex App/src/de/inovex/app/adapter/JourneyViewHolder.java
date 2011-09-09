package de.inovex.app.adapter;

import android.view.View;
import android.widget.TextView;
import de.inovex.app.R;

public class JourneyViewHolder {

	public JourneyViewHolder(View view) {
		type = (TextView) view.findViewById(R.id.textview_type);
		date = (TextView) view.findViewById(R.id.textview_date);
		start = (TextView) view.findViewById(R.id.textview_start_place);
		destination = (TextView) view.findViewById(R.id.textview_destination);
		description = (TextView) view.findViewById(R.id.textview_description);
	}

	TextView type;
	TextView date;
	TextView start;
	TextView destination;
	TextView description;
}
