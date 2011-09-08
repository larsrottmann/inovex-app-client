package de.inovex.app.activities;

import java.util.Date;

import de.inovex.app.R;
import de.inovex.app.provider.DataUtilities;
import de.inovex.app.provider.InovexContentProvider;
import de.inovex.app.provider.InovexContentProvider.Columns;
import de.inovex.app.provider.InovexContentProvider.Types;
import android.app.Activity;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

public class NewJourneyActivity extends Activity {
	
	
	private AutoCompleteTextView mDescriptionTextView;
	private AutoCompleteTextView mDestinationTextView;
	private AutoCompleteTextView mStartTextView;
	private RadioGroup mJourneyTypeRadioGroup;
	private Button mDatePickButton;
	private Button mTimePickButton;
	private Button mJourneyPickButton;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_journey);

		mDescriptionTextView = (AutoCompleteTextView) findViewById(R.id.textViewDescription);
		mDestinationTextView = (AutoCompleteTextView) findViewById(R.id.textViewDestination);
		mStartTextView = (AutoCompleteTextView) findViewById(R.id.textViewStart);
		//mJourneyDatePicker = (DatePicker) findViewById(R.id.datePicker);
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
//		int month = mJourneyDatePicker.getMonth();
//		int day = mJourneyDatePicker.getDayOfMonth();
//		int year = mJourneyDatePicker.getYear();
		Date date = new Date(0, 0, 0);
		int parentId = -1;
		try {
			DataUtilities.saveJourney(this,start, dest, type, desc, date, parentId);
		} catch (RemoteException e) {
			Toast.makeText(this, getText(R.string.error_saving_journey), Toast.LENGTH_LONG);
			e.printStackTrace();
		}
	}


}
