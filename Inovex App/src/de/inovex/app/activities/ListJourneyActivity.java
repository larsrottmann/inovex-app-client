package de.inovex.app.activities;

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import de.inovex.app.R;
import de.inovex.app.adapter.JourneyAdapter;
import de.inovex.app.provider.InovexContentProvider;

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
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Uri uri = Uri.withAppendedPath(InovexContentProvider.CONTENT_URI_JOURNEYS, String.valueOf(id));
		Intent i = new Intent(this, NewJourneyActivity.class);
		i.setData(uri);
		startActivity(i);
	}
}
