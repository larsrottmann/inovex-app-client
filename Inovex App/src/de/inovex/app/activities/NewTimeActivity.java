package de.inovex.app.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import de.inovex.app.R;

public class NewTimeActivity extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.new_time);

		Button b = (Button) findViewById(R.id.buttonOk);
		b.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				save();
				finish();
			}
		});
	}

	private void save() {
		
	}
}
