package de.inovex.app.adapter;

import android.view.View;
import android.widget.TextView;
import de.inovex.app.R;

public class JourneyViewHolder {
	
	public JourneyViewHolder(View view) {
		type = (TextView) view.findViewById(R.id.textViewType);
		date = (TextView) view.findViewById(R.id.textViewDate);
		start = (TextView) view.findViewById(R.id.textViewStart);
		destination = (TextView) view.findViewById(R.id.textViewDestination);
		description = (TextView) view.findViewById(R.id.textViewDescription);
	}
	
	TextView type;
	TextView date;
	TextView start;
	TextView destination;
	TextView description;
}
