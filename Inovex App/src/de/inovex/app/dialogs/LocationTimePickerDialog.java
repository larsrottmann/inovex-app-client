package de.inovex.app.dialogs;

import java.util.Calendar;
import java.util.Date;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import de.inovex.app.R;
import de.inovex.app.adapter.LocationSpinnerAdapter;

public class LocationTimePickerDialog extends AlertDialog  implements OnClickListener{
	
	    /**
	     * The callback interface used to indicate the user is done filling in
	     * the time (they clicked on the 'Set' button).
	     */
	    public interface OnLocationTimeSetListener {

	        /**
	         * @param view The view associated with this listener.
	         * @param hourOfDay The hour that was set.
	         * @param minute The minute that was set.
	         */
	        void onTimeLocationSet(String location, int hourOfDay, int minute, int type);
	    }
	    
	    public static final int DEPARTURE = 0;
	    public static final int ARRIVAL = 1;

	    private static final String HOUR = "hour";
	    private static final String MINUTE = "minute";
	    private static final String IS_24_HOUR = "is24hour";
	    
	    private final TimePicker mTimePicker;
	    private final Spinner mLocationPicker;
	    private final TextView mTextViewTimeLabel;
	    private final TextView mTextViewLocationLabel;
	    private final int mType;
	    private final OnLocationTimeSetListener mCallback;
	    private final Calendar mCalendar;
	    private final java.text.DateFormat mDateFormat;
	    private String mCurrentLocationString;
		private OnLocationSelectedListener mLocationListener = new OnLocationSelectedListener();

	    
	    
	    int mInitialHourOfDay;
	    int mInitialMinute;
	    boolean mIs24HourView;
	    
		public class OnLocationSelectedListener implements OnItemSelectedListener {

			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				mCurrentLocationString = parent.getItemAtPosition(pos).toString();
			}

			public void onNothingSelected(AdapterView<?> parent) {
				mCurrentLocationString = parent.getItemAtPosition(0).toString();
			}
		}



	    /**
	     * @param context Parent.
	     * @param theme the theme to apply to this dialog
	     * @param callBack How parent is notified.
	     * @param hourOfDay The initial hour.
	     * @param minute The initial minute.
	     * @param is24HourView Whether this is a 24 hour view, or AM/PM.
	     */
	    public LocationTimePickerDialog(Context context, OnLocationTimeSetListener callBack, int labelType ) {
	        super(context);
	        mType = labelType;
	        Date d = new Date();
	        mCallback = callBack;
	        mInitialHourOfDay = d.getHours();
	        mInitialMinute = d.getMinutes();
	        mIs24HourView =  android.text.format.DateFormat.is24HourFormat(getContext());
	        mDateFormat = DateFormat.getTimeFormat(context);
	        mCalendar = Calendar.getInstance();
	        updateTitle(mInitialHourOfDay, mInitialMinute);
	        
	        setButton(DialogInterface.BUTTON_POSITIVE, context.getText(R.string.ok),this);
	        setButton(DialogInterface.BUTTON_NEGATIVE, context.getText(android.R.string.cancel), (OnClickListener) null);
	       // setIcon(R.drawable.ic_dialog_time);
	        
	        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        View view = inflater.inflate(R.layout.location_time_picker_dialog, null);
	        setView(view);
	        mTimePicker = (TimePicker) view.findViewById(R.id.timePicker);
	        mLocationPicker = (Spinner) view.findViewById(R.id.spinner_pick_location);
	        mTextViewTimeLabel = (TextView) view.findViewById(R.id.textView_time_label);
	        mTextViewLocationLabel = (TextView) view.findViewById(R.id.textView_location_label);

			mLocationPicker.setAdapter(new LocationSpinnerAdapter(context));
			mLocationPicker.setOnItemSelectedListener(mLocationListener);
	        
	        // initialize state
	        mTimePicker.setCurrentHour(mInitialHourOfDay);
	        mTimePicker.setCurrentMinute(mInitialMinute);
	        mTimePicker.setIs24HourView(mIs24HourView);
	        
	        if (labelType==ARRIVAL){
	        	mTextViewLocationLabel.setText(R.string.end_place);
	        	mTextViewTimeLabel.setText(R.string.end_time);
	        } else if(labelType==DEPARTURE) {
	        	mTextViewLocationLabel.setText(R.string.starting_place);
	        	mTextViewTimeLabel.setText(R.string.start_time);	        	
	        }	    
	    }
	    
	    @Override
	    protected void onStart() {
	    	super.onStart();

	    }
	    
	    public void onClick(DialogInterface dialog, int which) {
	        if (mCallback != null) {
	            mTimePicker.clearFocus();
	            mCallback.onTimeLocationSet(mCurrentLocationString, mTimePicker.getCurrentHour(), mTimePicker.getCurrentMinute(), mType);
	        }
	    }

	    
	    public void updateTime(int hourOfDay, int minutOfHour) {
	        mTimePicker.setCurrentHour(hourOfDay);
	        mTimePicker.setCurrentMinute(minutOfHour);
	    }

	    private void updateTitle(int hour, int minute) {
	        mCalendar.set(Calendar.HOUR_OF_DAY, hour);
	        mCalendar.set(Calendar.MINUTE, minute);
	        setTitle(mDateFormat.format(mCalendar.getTime()));
	    }
	    
	    @Override
	    public Bundle onSaveInstanceState() {
	        Bundle state = super.onSaveInstanceState();
	        state.putInt(HOUR, mTimePicker.getCurrentHour());
	        state.putInt(MINUTE, mTimePicker.getCurrentMinute());
	        state.putBoolean(IS_24_HOUR, mTimePicker.is24HourView());
	        return state;
	    }
	    
	    @Override
	    public void onRestoreInstanceState(Bundle savedInstanceState) {
	        super.onRestoreInstanceState(savedInstanceState);
	        int hour = savedInstanceState.getInt(HOUR);
	        int minute = savedInstanceState.getInt(MINUTE);
	        mTimePicker.setCurrentHour(hour);
	        mTimePicker.setCurrentMinute(minute);
	        mTimePicker.setIs24HourView(savedInstanceState.getBoolean(IS_24_HOUR));
	        updateTitle(hour, minute);
	    }


	

}
