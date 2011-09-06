package de.inovex.app.activities;

import de.inovex.app.adapter.JourneyAdapter;
import android.app.ListActivity;
import android.os.Bundle;

public class ListJourneyActivity extends ListActivity {
	
	private JourneyAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mAdapter = new JourneyAdapter(this);
		setListAdapter(mAdapter);		
	}
}
