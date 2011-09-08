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
import android.widget.Toast;
import de.inovex.app.R;
import de.inovex.app.provider.InovexContentProvider;
import de.inovex.app.provider.InovexContentProvider.Columns;
import de.inovex.app.provider.InovexContentProvider.Types;
import de.inovex.app.views.DateTimePicker;

public class NewTimeActivity extends Activity {

	private AutoCompleteTextView mDescriptionTextView;
	private AutoCompleteTextView mProjectTextView;
	private DateTimePicker mStartDateTimePicker;
	private DateTimePicker mEndDateTimePicker;
	private Button mStartDateButton;
	private Button mStartTimeButton;
	private Button mEndDateButton;
	private Button mEndTimeButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.new_time);

	    mDescriptionTextView = (AutoCompleteTextView) findViewById(R.id.textViewDescription);
	    mProjectTextView = (AutoCompleteTextView) findViewById(R.id.textViewProject);
	    mStartDateTimePicker = (DateTimePicker) findViewById(R.id.dateTimePickerStartTime);
	    mEndDateTimePicker  = (DateTimePicker) findViewById(R.id.dateTimePickerEndTime);

		Button b = (Button) findViewById(R.id.buttonOk);
	    b.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				save();
				finish();
			}
		});
	}

	private void save() {
		String desc = mDescriptionTextView.getText().toString();
		String project = mProjectTextView.getText().toString();
		long startTime = mStartDateTimePicker.getTime();
		long endTime = mEndDateTimePicker.getTime();

		Date startDate = new Date(startTime);
		Date endDate = new Date(endTime);
		int parentId = -1;

		try {
			saveTime(desc, project, startDate, endDate, Types.TIME, parentId);
		} catch (RemoteException re) {
			Toast.makeText(this, getText(R.string.error_saving_time), Toast.LENGTH_LONG);
			re.printStackTrace();
		}
	}

	private Uri saveTime(String desc, String project, Date startDate, Date endDate, String type, int parentId) throws RemoteException {
		ContentProviderClient client = null;
		try {
			ContentValues v = new ContentValues();
			v.put(Columns.DESCRIPTION, desc);
			v.put(Columns.PROJECT, project);
			v.put(Columns.DATE, startDate.getTime());
			v.put(Columns.START_TIME, startDate.getTime());
			v.put(Columns.END_TIME, endDate.getTime());
			v.put(Columns.TYPE, Integer.parseInt(type));
			v.put(Columns.PARENT_ID, parentId);

			client = getContentResolver().acquireContentProviderClient(InovexContentProvider.CONTENT_URI);
			return client.insert(InovexContentProvider.CONTENT_URI, v);
		} finally {
			if (client != null)
				client.release();
		}
	}
}
