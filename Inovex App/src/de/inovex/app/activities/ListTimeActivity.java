package de.inovex.app.activities;

import de.inovex.app.adapter.TimeAdapter;
import android.app.ListActivity;
import android.os.Bundle;

public class ListTimeActivity extends ListActivity {

	private TimeAdapter mAdapter;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    mAdapter = new TimeAdapter(this);
	    setListAdapter(mAdapter);
	}
}
