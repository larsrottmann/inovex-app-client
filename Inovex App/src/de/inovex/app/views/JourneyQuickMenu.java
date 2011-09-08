package de.inovex.app.views;

import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;
import de.inovex.app.R;
import de.inovex.app.activities.MainMenuActivity;
import de.inovex.app.adapter.LocationSpinnerAdapter;
import de.inovex.app.provider.InovexContentProvider;
import de.inovex.app.provider.InovexContentProvider.Columns;
import de.inovex.app.provider.InovexContentProvider.Types;

public class JourneyQuickMenu extends LinearLayout {

	private static final String PREF_NAME="inovex_current_journey";
	private static final String PREF_START_LOCATION="start_location";
	private static final String PREF_START_DATE="start_date";
	private static final String PREF_TYPE="type";
	private static final String[] PROJECTION = new String[] { InovexContentProvider.Columns.ID, InovexContentProvider.Columns.END_DATE, InovexContentProvider.Columns.TYPE };
	
	private Spinner mCurrentLocation;
	private TimePickerButton mCurrentTime;
	private OnLocationSelectedListener mLocationListener = new OnLocationSelectedListener();
	private ViewAnimator mViewAnimator;
	
	private String mCurrentLocationString;

	public class OnLocationSelectedListener implements OnItemSelectedListener {

	    public void onItemSelected(AdapterView<?> parent,
	        View view, int pos, long id) {
	         mCurrentLocationString = parent.getItemAtPosition(pos).toString();
	    }

	    public void onNothingSelected(AdapterView<?> parent) {
	         mCurrentLocationString = parent.getItemAtPosition(0).toString();
	    }
	}
	    
	public JourneyQuickMenu(final Context activity, AttributeSet attrs) {
		super(activity, attrs);
		LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.journey_quick_menu, this);

		mViewAnimator = (ViewAnimator) findViewById(R.id.viewanimator_new_journey);
		mCurrentTime = (TimePickerButton) findViewWithTag(R.id.timepicker_start_time);
		
		mCurrentLocation = (Spinner) findViewById(R.id.spinner_pick_location);
		mCurrentLocation.setAdapter(new LocationSpinnerAdapter(activity));
		mCurrentLocation.setOnItemSelectedListener(mLocationListener);
		
		
		if (unfinishedJourneyPresent(activity)) {
			//user is currently on a journey
			mViewAnimator.setDisplayedChild(0);
			showUnfinishedJourney(activity);
		} else {
			String type = getTypeOfLastJourney(activity);
			if (type.equals(Types.JOURNEY_END) ) {
				// user can start a new journey
				mViewAnimator.setDisplayedChild(0);
				initStartJourneyButton();
			} else if (type.equals(Types.JOURNEY_START) || type.equals(Types.JOURNEY_CONTINUATION)) {
				// user can start a continuation or return journey
				mViewAnimator.setDisplayedChild(1);
				showLastJourney(activity);
				initReturnJourneyButton();
				initContinuationJourneyButton();
			}
		}
	}
	private void initReturnJourneyButton(){
		Button b = (Button) findViewById(R.id.button_start_return_journey);
		b.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				
			}
		});		
	}
	private void initContinuationJourneyButton(){
		Button b = (Button) findViewById(R.id.button_start_continuation_journey);
		b.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				
			}
		});		
	}
	private void initStartJourneyButton(){
		Button b = (Button) findViewById(R.id.button_start_new_journey);
		b.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				
			}
		});
	}
	
	private void showLastJourney(Context context){
		
	}

	private void showUnfinishedJourney(final Context context) {
		SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		String startDate = prefs.getString(PREF_START_DATE, "");
		String startLocation = prefs.getString(PREF_START_LOCATION, "");
		TextView info = (TextView) findViewById(R.id.textview_start_journey);
		info.setText(startDate + " - " + startLocation);
		Button b = (Button) findViewById(R.id.button_end_current_journey);
		b.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Toast.makeText(context, "popup with current location and time", Toast.LENGTH_LONG).show();				
			}
		});
	}
	
	private boolean unfinishedJourneyPresent(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		return  (prefs.contains(PREF_START_LOCATION));
	}

	private String getTypeOfLastJourney(Context context) {
		ContentProviderClient client = context.getContentResolver().acquireContentProviderClient(InovexContentProvider.CONTENT_URI_JOURNEYS);

		try {
			Cursor c = client.query(InovexContentProvider.CONTENT_URI_JOURNEYS, PROJECTION, null, null, Columns.START_DATE + " DESC");
			boolean hasData = c.moveToFirst();
			if (!hasData) {
				return InovexContentProvider.Types.JOURNEY_END;
			}
			int index = c.getColumnIndex(Columns.TYPE);
			String type = c.getString(index);
			c.close();
			return type;

		} catch (RemoteException e) {
			e.printStackTrace();
		} finally {
			client.release();
		}
		return InovexContentProvider.Types.JOURNEY_END;
	}

}
