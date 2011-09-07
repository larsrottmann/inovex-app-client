package de.inovex.app.activities;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import de.inovex.app.R;
import de.inovex.app.adapter.JourneyAdapter;

public class ListJourneyActivity extends ListActivity {
	
	private JourneyAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mAdapter = new JourneyAdapter(this);
		setListAdapter(mAdapter);		
		ImageButton b = (ImageButton) findViewById(R.id.imagebutton_new_content);
		b.setVisibility(View.VISIBLE);
	}
}
