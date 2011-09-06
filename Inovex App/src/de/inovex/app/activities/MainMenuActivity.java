package de.inovex.app.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import de.inovex.app.R;
import de.inovex.app.activities.contacts.ListContactsActivity;

public class MainMenuActivity extends Activity {
	private void initButton(final Context c, final Class<?> cls, int id){
		Button b = (Button) findViewById(id);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(c,cls);
				startActivity(i);
			}
		});
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		initButton(this, NewReceiptActivity.class, R.id.button_enter_receipt);
		initButton(this, NewJourneyActivity.class, R.id.button_enter_journey);
		initButton(this, ListJourneyActivity.class, R.id.button_list_journeys);
		initButton(this, ListReceiptActivity.class, R.id.button_list_receipts);
		initButton(this, ListContactsActivity.class, R.id.button_list_contacts);
	}
}
