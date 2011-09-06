package de.inovex.app.activities;

import java.util.Date;

import android.app.Activity;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioGroup;
import android.widget.Toast;
import de.inovex.app.R;
import de.inovex.app.provider.InovexContentProvider;
import de.inovex.app.provider.InovexContentProvider.Columns;
import de.inovex.app.provider.InovexContentProvider.Types;

public class NewReceiptActivity extends Activity {

	private AutoCompleteTextView mDescriptionTextView;
	private AutoCompleteTextView mDestinationTextView;
	private AutoCompleteTextView mStartTextView;
	private DatePicker mJourneyDatePicker;
	private RadioGroup mJourneyTypeRadioGroup;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_journey);

		mDescriptionTextView = (AutoCompleteTextView) findViewById(R.id.textViewDescription);
		mDestinationTextView = (AutoCompleteTextView) findViewById(R.id.textViewDestination);
		mStartTextView = (AutoCompleteTextView) findViewById(R.id.textViewStart);
		mJourneyDatePicker = (DatePicker) findViewById(R.id.datePicker);
		mJourneyTypeRadioGroup = (RadioGroup) findViewById(R.id.radioGroupType);

		Button b = (Button) findViewById(R.id.buttonOk);
		b.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				save();
				finish();
			}
		});

	}

	private void save() {
		String dest = mDestinationTextView.getText().toString();
		String start = mStartTextView.getText().toString();
		String desc = mDescriptionTextView.getText().toString();
		String type = "";
		switch (mJourneyTypeRadioGroup.getCheckedRadioButtonId()) {
		case R.id.radioArrival:
			type = Types.JOURNEY_START;
			break;
		case R.id.radioContinuation:
			type = Types.JOURNEY_CONTINUATION;
			break;
		case R.id.radioReturn:
			type = Types.JOURNEY_END;
			break;
		default:
			// TODO error handling
		}
		int month = mJourneyDatePicker.getMonth();
		int day = mJourneyDatePicker.getDayOfMonth();
		int year = mJourneyDatePicker.getYear();
		Date date = new Date(year, month, day);
		int parentId = -1;
		try {
			saveJourney(start, dest, type, desc, date, parentId);
		} catch (RemoteException e) {
			Toast.makeText(this, getText(R.string.error_saving_journey), Toast.LENGTH_LONG);
			e.printStackTrace();
		}
	}

	private Uri saveJourney(String start, String destination, String type, String description, Date date, int parentId) throws RemoteException {
		ContentProviderClient client = null;
		try {
			ContentValues v = null;
			v = new ContentValues();
			v.put(Columns.START_LOCATION, start);
			v.put(Columns.DESTINATION, destination);
			v.put(Columns.DESCRIPTION, description);
			v.put(Columns.TYPE, type);
			v.put(Columns.DATE, date.getTime());
			v.put(Columns.PARENT_ID, parentId);

			client = getContentResolver().acquireContentProviderClient(InovexContentProvider.CONTENT_URI);
			return client.insert(InovexContentProvider.CONTENT_URI, v);
		} finally {
			if (client != null) {
				client.release();
			}
		}
	}
}
