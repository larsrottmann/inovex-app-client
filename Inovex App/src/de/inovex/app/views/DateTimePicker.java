package de.inovex.app.views;

import java.util.Calendar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.RelativeLayout;
import de.inovex.app.R;

public class DateTimePicker extends RelativeLayout {

	DatePickerButton mDateButton;
	TimePickerButton mTimeButton;
	Calendar mCalendar;
	
	public DateTimePicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.date_time_picker, this);
		mDateButton = (DatePickerButton) findViewById(R.id.buttonDate);
		mTimeButton = (TimePickerButton) findViewById(R.id.buttonTime);
		mCalendar = Calendar.getInstance();
		
	}
	
	public void setTime(long time){
		mDateButton.setTime(time);
		mTimeButton.setTime(time);
	}

	public long getTime() {
		int year = mDateButton.mCalendar.get(Calendar.YEAR);
		int month = mDateButton.mCalendar.get(Calendar.MONTH);
		int day = mDateButton.mCalendar.get(Calendar.DAY_OF_MONTH);
		
		int hours = mTimeButton.getHours();
		int minutes = mTimeButton.getMinutes();

		mCalendar.set(year, month, day, hours, minutes);
		
		return mCalendar.getTimeInMillis();
	}
}
