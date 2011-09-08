package de.inovex.app.activities;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import de.inovex.app.R;
import de.inovex.app.adapter.JourneyAdapter;

public class ListJourneyActivity extends ListActivity {
	
	private JourneyAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.journey_list);
		mAdapter = new JourneyAdapter(this);
		setListAdapter(mAdapter);		
		ImageButton b = (ImageButton) findViewById(R.id.imagebutton_new_content);
		b.setVisibility(View.VISIBLE);
		b.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(ListJourneyActivity.this, NewJourneyActivity.class);
				startActivity(i);
			}
		});
	}
}
