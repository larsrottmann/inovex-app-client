package de.inovex.app.views;

import java.util.Date;

import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.os.RemoteException;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;
import de.inovex.app.R;
import de.inovex.app.dialogs.LocationTimePickerDialog;
import de.inovex.app.dialogs.LocationTimePickerDialog.OnLocationTimeSetListener;
import de.inovex.app.provider.DataUtilities;
import de.inovex.app.provider.InovexContentProvider;
import de.inovex.app.provider.InovexContentProvider.Columns;
import de.inovex.app.provider.InovexContentProvider.Types;

public class JourneyQuickMenu extends RelativeLayout implements OnLocationTimeSetListener {

	private static final String PREF_NAME = "inovex_current_journey";
	private static final String PREF_START_LOCATION = "start_location";
	private static final String PREF_START_DATE = "start_date";
	private static final String PREF_PARENT_ID = "parent_id";
	private static final String PREF_TYPE = "type";
	private static final String[] PROJECTION = new String[] { InovexContentProvider.Columns.DESTINATION, InovexContentProvider.Columns.ID, InovexContentProvider.Columns.START_DATE,
			InovexContentProvider.Columns.END_DATE, InovexContentProvider.Columns.START_LOCATION, InovexContentProvider.Columns.TYPE };

//	private Spinner mCurrentLocation;
//	private TimePickerButton mCurrentTime;
//	private OnLocationSelectedListener mLocationListener = new OnLocationSelectedListener();
	private JourneyContentObserver mJourneyContentObserver = new JourneyContentObserver(new Handler());
	private ViewAnimator mViewAnimator;
//	private String mCurrentLocationString;
	private TextView mTextViewStartPlace;
	private TextView mTextViewDestination;
	private TextView mTextViewStartDate;
	private TextView mTextViewEndDate;
//	private TextSwitcher mTextSwitcherTimeLabel;
//	private TextSwitcher mTextSwitcherLocationLabel;
	private TextSwitcher mTextSwitcherJourneyType;
	private String mCurrentType;
	
	private static class JourneyDataHolder {
		String type;
		long startdate;
		long enddate;
		String startLocation;
		String destination;
	}

//	private class OnLocationSelectedListener implements OnItemSelectedListener {
//
//		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
//			mCurrentLocationString = parent.getItemAtPosition(pos).toString();
//		}
//
//		public void onNothingSelected(AdapterView<?> parent) {
//			mCurrentLocationString = parent.getItemAtPosition(0).toString();
//		}
//	}

	public class JourneyContentObserver extends ContentObserver {
		public JourneyContentObserver(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			updateView();
		}
	}

	public JourneyQuickMenu(final Context activity, AttributeSet attrs) {
		super(activity, attrs);
		LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.journey_quick_menu, this);

		mViewAnimator = (ViewAnimator) findViewById(R.id.viewanimator_new_journey);
//		mCurrentTime = (TimePickerButton) findViewById(R.id.timepicker_start_time);
//		mTextSwitcherLocationLabel = (TextSwitcher) findViewById(R.id.textSwitcher_location_label);
//		mTextSwitcherTimeLabel = (TextSwitcher) findViewById(R.id.textSwitcher_time_label);
		mTextSwitcherJourneyType = (TextSwitcher) findViewById(R.id.textSwitcher_type_of_journey);
		mTextViewStartPlace = (TextView) findViewById(R.id.textview_start_place);
		mTextViewDestination = (TextView) findViewById(R.id.textview_destination);
		mTextViewStartDate = (TextView) findViewById(R.id.textView_start_time);
		mTextViewEndDate = (TextView) findViewById(R.id.textView_end_time);

//		mCurrentLocation = (Spinner) findViewById(R.id.spinner_pick_location);
//		mCurrentLocation.setAdapter(new LocationSpinnerAdapter(activity));
//		mCurrentLocation.setOnItemSelectedListener(mLocationListener);

		updateView();
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		getContext().getContentResolver().registerContentObserver(InovexContentProvider.CONTENT_URI, true, mJourneyContentObserver);
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		getContext().getContentResolver().unregisterContentObserver(mJourneyContentObserver);
	}

	private void updateView() {
		if (isJourneyStoredInPreferences()) {
			// user is currently on a journey
//			mTextSwitcherLocationLabel.setText(getContext().getText(R.string.end_place));
//			mTextSwitcherTimeLabel.setText(getContext().getText(R.string.end_time));

			mViewAnimator.setDisplayedChild(2);
			initFinishJourneyButton();
			showLastJourney();
		} else {
//			mTextSwitcherLocationLabel.setText(getContext().getText(R.string.starting_place));
//			mTextSwitcherTimeLabel.setText(getContext().getText(R.string.start_time));
			String type = getTypeOfLastJourney();
			if (type.equals(Types.JOURNEY_END)) {
				// user can start a new journey
				mViewAnimator.setDisplayedChild(0);
				initStartJourneyButton();
				showLastJourney();
			} else if (type.equals(Types.JOURNEY_START) || type.equals(Types.JOURNEY_CONTINUATION)) {
				// user can start a continuation or return journey
				mViewAnimator.setDisplayedChild(1);
				initReturnJourneyButton();
				initContinuationJourneyButton();
				showLastJourney();
			}
		}
	}

	private void showLastJourney() {
		if (isJourneyStoredInPreferences()) {
			final SharedPreferences prefs = getContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
			final String startLocation = prefs.getString(PREF_START_LOCATION, "");
			long time = prefs.getLong(PREF_START_DATE, 0);
			String type = prefs.getString(PREF_TYPE, "");
			CharSequence formattedDate = DateFormat.format("MMM dd, k:mm", new Date(time));
			mTextViewStartDate.setText(formattedDate);
			mTextViewStartPlace.setText(startLocation);
			mTextViewDestination.setText("?");
			mTextViewEndDate.setVisibility(View.INVISIBLE);
			CharSequence typeString = InovexContentProvider.Types.getDisplayStringFromType(getContext(), type);
			mTextSwitcherJourneyType.setText(typeString);
		} else {
			JourneyDataHolder holder = getLastJourney();
			if (holder != null) {
				CharSequence formattedDate = DateFormat.format("MMM dd, k:mm", new Date(holder.startdate));
				mTextViewStartDate.setText(formattedDate);
				mTextViewStartPlace.setText(holder.startLocation);
				mTextViewDestination.setText(holder.destination);
				formattedDate = DateFormat.format("MMM dd, k:mm", new Date(holder.enddate));
				mTextViewEndDate.setVisibility(View.VISIBLE);
				mTextViewEndDate.setText(formattedDate);
				CharSequence typeString = InovexContentProvider.Types.getDisplayStringFromType(getContext(), holder.type);
				mTextSwitcherJourneyType.setText(typeString);
			}
		}
	}

	private void initReturnJourneyButton() {
		Button b = (Button) findViewById(R.id.button_start_return_journey);
		b.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				long time = mCurrentTime.getTime();
//				String location = mCurrentLocationString;
//				saveJourneyIntoPreferences(time, location, InovexContentProvider.Types.JOURNEY_END);
//				updateView();
				mCurrentType = InovexContentProvider.Types.JOURNEY_END;
				LocationTimePickerDialog dialog = new LocationTimePickerDialog(getContext(), JourneyQuickMenu.this,LocationTimePickerDialog.DEPARTURE);
				dialog.show();				
			}
		});
	}

	private void initContinuationJourneyButton() {
		Button b = (Button) findViewById(R.id.button_start_continuation_journey);
		b.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				long time = mCurrentTime.getTime();
//				String location = mCurrentLocationString;
//				saveJourneyIntoPreferences(time, location, InovexContentProvider.Types.JOURNEY_CONTINUATION);
//				updateView();
				mCurrentType = InovexContentProvider.Types.JOURNEY_CONTINUATION;
				LocationTimePickerDialog dialog = new LocationTimePickerDialog(getContext(), JourneyQuickMenu.this,LocationTimePickerDialog.DEPARTURE);
				dialog.show();				

			}
		});
	}

	private void initStartJourneyButton() {
		Button b = (Button) findViewById(R.id.button_start_new_journey);
		b.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				long time = mCurrentTime.getTime();
//				String location = mCurrentLocationString;
//				saveJourneyIntoPreferences(time, location, InovexContentProvider.Types.JOURNEY_START);
//				updateView();
				mCurrentType = InovexContentProvider.Types.JOURNEY_START;
				LocationTimePickerDialog dialog = new LocationTimePickerDialog(getContext(), JourneyQuickMenu.this,LocationTimePickerDialog.DEPARTURE);
				dialog.show();				
			}
		});
	}

	private void initFinishJourneyButton() {

		Button b = (Button) findViewById(R.id.button_finish_journey);
		b.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				LocationTimePickerDialog dialog = new LocationTimePickerDialog(getContext(), JourneyQuickMenu.this,LocationTimePickerDialog.ARRIVAL);
				dialog.show();				

				
			}
		});
	}

	private void saveJourneyIntoPreferences(long startTime, String startLocation, String type) {
		SharedPreferences prefs = getContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putLong(PREF_START_DATE, startTime);
		editor.putString(PREF_START_LOCATION, startLocation);
		editor.putString(PREF_TYPE, type);
		editor.apply();
	}

	private boolean isJourneyStoredInPreferences() {
		SharedPreferences prefs = getContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		return (prefs.contains(PREF_START_LOCATION));
	}

	private JourneyDataHolder getLastJourney() {
		ContentProviderClient client = getContext().getContentResolver().acquireContentProviderClient(InovexContentProvider.CONTENT_URI_JOURNEYS);

		try {
			Cursor c = client.query(InovexContentProvider.CONTENT_URI_JOURNEYS, PROJECTION, null, null, Columns.START_DATE + " DESC");
			boolean hasData = c.moveToFirst();
			if (!hasData) {
				c.close();
				return null;
			}
			int index = c.getColumnIndex(Columns.TYPE);
			JourneyDataHolder holder = new JourneyDataHolder();
			holder.type = c.getString(index);
			index = c.getColumnIndex(Columns.START_DATE);
			holder.startdate = c.getLong(index);
			index = c.getColumnIndex(Columns.START_LOCATION);
			holder.startLocation = c.getString(index);
			index = c.getColumnIndex(Columns.DESTINATION);
			holder.destination = c.getString(index);
			index = c.getColumnIndex(Columns.END_DATE);
			holder.enddate = c.getLong(index);
			c.close();
			return holder;

		} catch (RemoteException e) {
			e.printStackTrace();
		} finally {
			client.release();
		}
		return null;

	}

	private String getTypeOfLastJourney() {
		JourneyDataHolder holder = getLastJourney();
		if (holder != null && holder.type != null) {
			return holder.type;
		}
		return InovexContentProvider.Types.JOURNEY_END;
	}

	@Override
	public void onTimeLocationSet(String location, int hourOfDay, int minute, int type) {
		if (type==LocationTimePickerDialog.ARRIVAL) {
			final SharedPreferences prefs = getContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
			final Date startDate = new Date(prefs.getLong(PREF_START_DATE, 1));
			final String startLocation = prefs.getString(PREF_START_LOCATION, "");
			final String journeyType = prefs.getString(PREF_TYPE, "");
			final int parentId = prefs.getInt(PREF_PARENT_ID, -1);

			Date endDate = new Date();
			endDate.setHours(hourOfDay);
			endDate.setMinutes(minute);
			try {
				DataUtilities.saveJourney(getContext(), startLocation, location, journeyType, "", startDate, endDate, parentId);
			} catch (RemoteException e) {
				Toast.makeText(getContext(), getContext().getText(R.string.error_saving_journey), Toast.LENGTH_LONG);
				e.printStackTrace();
			}
			prefs.edit().clear().apply();
			updateView();
		} else if (type == LocationTimePickerDialog.DEPARTURE) {
			Date startDate = new Date();
			startDate.setHours(hourOfDay);
			startDate.setMinutes(minute);
			saveJourneyIntoPreferences(startDate.getTime(), location, mCurrentType);
			updateView();
		}
	}

}
