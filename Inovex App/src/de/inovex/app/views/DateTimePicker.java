package de.inovex.app.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.RelativeLayout;
import de.inovex.app.R;

public class DateTimePicker extends RelativeLayout {

	Button mDateButton;
	Button mTimeButton;
	
	public DateTimePicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.date_time_picker, this);
		mDateButton = (Button) findViewById(R.id.buttonDate);
		mTimeButton = (Button) findViewById(R.id.buttonTime);
		
	}

	public long getTime() {
		//TODO: implement
		return System.currentTimeMillis();
	}
}
