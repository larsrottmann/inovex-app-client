package de.inovex.app.views;

import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;

public class DatePickerButton extends Button implements OnDateSetListener {
	Calendar mCalendar = Calendar.getInstance();
    private final static String mFormatString = "MMM dd, yyyy";
	
    public DatePickerButton(Context context) {
        super(context);
    }

	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
		
		mCalendar.set(year,monthOfYear, dayOfMonth);
		CharSequence formattedDate = DateFormat.format(mFormatString, mCalendar);
		setText(formattedDate);
	}
    

	public void setTime(long millis){
		mCalendar.setTimeInMillis(millis);
		CharSequence formattedDate = DateFormat.format(mFormatString, mCalendar);
		setText(formattedDate);
	}
	
    public long getTime(){
    	return mCalendar.getTimeInMillis();
    }
    public DatePickerButton(Context context, AttributeSet attrs) {
        super(context, attrs);
		CharSequence formattedDate = DateFormat.format(mFormatString, mCalendar);
		setText(formattedDate);

    	this.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				 DatePickerDialog dialog = new DatePickerDialog(getContext(),DatePickerButton.this,mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH),mCalendar.get(Calendar.DAY_OF_MONTH));
				 dialog.show();
			}
		});

    }
}
