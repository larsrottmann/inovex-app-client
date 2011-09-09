package de.inovex.app.activities;

import java.util.Date;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import de.inovex.app.R;
import de.inovex.app.adapter.LocationSpinnerAdapter;
import de.inovex.app.provider.DataUtilities;
import de.inovex.app.provider.InovexContentProvider;
import de.inovex.app.provider.InovexContentProvider.Types;
import de.inovex.app.views.DateTimePicker;

public class NewJourneyActivity extends Activity {
	
	
	private AutoCompleteTextView mDescriptionTextView;
	private RadioGroup mJourneyTypeRadioGroup;
	private DateTimePicker mStartDatePicker;
	private DateTimePicker mEndDatePicker;
	private Spinner mStartLocationPicker;
	private Spinner mEndLocationPicker;
	private Uri mDataUri = null;
	
	private String mCurrentStartLocationString;
	private String mCurrentEndLocationString;

	private class OnStartLocationSelectedListener implements OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
			mCurrentStartLocationString = parent.getItemAtPosition(pos).toString();
		}

		public void onNothingSelected(AdapterView<?> parent) {
			mCurrentStartLocationString = parent.getItemAtPosition(0).toString();
		}
	}
	private class OnEndLocationSelectedListener implements OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
			mCurrentEndLocationString = parent.getItemAtPosition(pos).toString();
		}

		public void onNothingSelected(AdapterView<?> parent) {
			mCurrentEndLocationString = parent.getItemAtPosition(0).toString();
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_journey);

		mDescriptionTextView = (AutoCompleteTextView) findViewById(R.id.textView_description);
		mStartLocationPicker = (Spinner) findViewById(R.id.spinner_pick_start_location);
		mEndLocationPicker = (Spinner) findViewById(R.id.spinner_pick_end_location);
		mJourneyTypeRadioGroup = (RadioGroup) findViewById(R.id.radioGroupType);
		mStartDatePicker = (DateTimePicker) findViewById(R.id.dateTimePicker_start_time);
		mEndDatePicker = (DateTimePicker) findViewById(R.id.dateTimePicker_end_time);
		
		mEndLocationPicker.setAdapter(new LocationSpinnerAdapter(this));
		mEndLocationPicker.setOnItemSelectedListener(new OnEndLocationSelectedListener());
		mStartLocationPicker.setAdapter(new LocationSpinnerAdapter(this));
		mStartLocationPicker.setOnItemSelectedListener(new OnStartLocationSelectedListener());
		
		Button b = (Button) findViewById(R.id.buttonOk);
		b.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				save();
				finish();
			}
		});
		
		if (getIntent()!=null && getIntent().getData()!=null){
			Cursor c = managedQuery(getIntent().getData(), null, null,null, null);
			if (c.moveToFirst()){
				mDataUri = getIntent().getData();
				fillFields(c);
				c.close();
			}
		}

	}

	private void fillFields(Cursor c){
		int index = c.getColumnIndex(InovexContentProvider.Columns.DESCRIPTION);
		String descr = c.getString(index);
		index = c.getColumnIndex(InovexContentProvider.Columns.START_DATE);
		long startTime = c.getLong(index);
		index = c.getColumnIndex(InovexContentProvider.Columns.END_DATE);
		long endTime = c.getLong(index);
		index = c.getColumnIndex(InovexContentProvider.Columns.START_LOCATION);
		String startLocation = c.getString(index);
		index = c.getColumnIndex(InovexContentProvider.Columns.DESTINATION);
		String endLocation = c.getString(index);
		index = c.getColumnIndex(InovexContentProvider.Columns.TYPE);
		String type = c.getString(index);
		
		if (type.equals(InovexContentProvider.Types.JOURNEY_CONTINUATION)){
			mJourneyTypeRadioGroup.check(R.id.radioContinuation);
		} else if (type.equals(InovexContentProvider.Types.JOURNEY_END)){
			mJourneyTypeRadioGroup.check(R.id.radioReturn);
		} else if (type.equals(InovexContentProvider.Types.JOURNEY_START)){
			mJourneyTypeRadioGroup.check(R.id.radioArrival);
		}
		
		mDescriptionTextView.setText(descr);
		mStartDatePicker.setTime(startTime);
		mEndDatePicker.setTime(endTime);
		
		LocationSpinnerAdapter adapter = (LocationSpinnerAdapter) mStartLocationPicker.getAdapter();
		int id = adapter.getPosition(startLocation);
		mStartLocationPicker.setSelection(id);
		
		 adapter = (LocationSpinnerAdapter) mEndLocationPicker.getAdapter();
		id = adapter.getPosition(endLocation);
		mEndLocationPicker.setSelection(id);
	}
	
	private void save() {
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
		}

		Date startDate = new Date(mStartDatePicker.getTime());
		Date endDate = new Date(mEndDatePicker.getTime());
		int parentId = -1;
		try {
			if (mDataUri==null){
				DataUtilities.saveJourney(this,mCurrentStartLocationString, mCurrentEndLocationString, type, desc, startDate,endDate, parentId);				
			} else {
				DataUtilities.updateJourney(this,mDataUri,mCurrentStartLocationString, mCurrentEndLocationString, type, desc, startDate,endDate, parentId);				
			}
		} catch (RemoteException e) {
			Toast.makeText(this, getText(R.string.error_saving_journey), Toast.LENGTH_LONG);
			e.printStackTrace();
		}
	}



}
