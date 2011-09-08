package de.inovex.app.activities;

import de.inovex.app.R;
import de.inovex.app.adapter.TimeAdapter;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class ListTimeActivity extends ListActivity {

	private TimeAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
		setContentView(R.layout.time_list);
	    mAdapter = new TimeAdapter(this);
	    setListAdapter(mAdapter);
		ImageButton b = (ImageButton) findViewById(R.id.imagebutton_new_content);
		b.setVisibility(View.VISIBLE);
		b.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(ListTimeActivity.this, NewTimeActivity.class);
				startActivity(i);
			}
		});
	}
}
