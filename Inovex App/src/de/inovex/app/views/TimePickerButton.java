package de.inovex.app.views;

import java.util.Calendar;
import java.util.Date;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;

public class TimePickerButton extends Button {
    

	Calendar mCalendar;
    private final static String m12 = "h:mm:ss aa";
    private final static String m24 = "k:mm:ss";
    private FormatChangeObserver mFormatChangeObserver;

    private Runnable mTicker;
    private Handler mHandler;
    private boolean mTickerStopped = false;

	private int mHour;
	private int mMinute;
	private Date mCustomDate = new Date();
	private boolean useCustomTime=false;
	
	public synchronized void setUseCustomTime(boolean val){
		useCustomTime = val;
	}
	public synchronized boolean getUseCustomTime(){
		return useCustomTime;
	}
    
	private TimePickerDialog.OnTimeSetListener mTimeSetListener =
		    new TimePickerDialog.OnTimeSetListener() {
		        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			        	mHour = hourOfDay;
			            mMinute = minute;
			            mCustomDate.setHours(hourOfDay);
			            mCustomDate.setMinutes(minute);
			            setUseCustomTime(true);
	                    setText(DateFormat.format(mFormat, mCustomDate));
	                    invalidate();						
		        }
		    };
    
    String mFormat;

    public TimePickerButton(Context context) {
        super(context);
        initClock();
    }

    public TimePickerButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initClock();
    }
    
    public long getTime(){
    	Date d;
    	if (getUseCustomTime()){
    		d = new Date();
    		d.setHours(mHour);
    		d.setMinutes(mMinute);
    	} else {
    		d = mCalendar.getTime();
    	}
    	return d.getTime();
    }
    public int getHours(){
    	if (getUseCustomTime()){
    		return mHour;
    	} else {
    		return mCalendar.getTime().getHours();
    	}
    }
    public int getMinutes(){
    	if (getUseCustomTime()){
    		return mMinute;
    	} else {
    		return mCalendar.getTime().getMinutes();
    	}
    }
    
    
    private void initClock() {
    	
    	this.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				boolean is24HourView = mFormat==m24;
				int hour,minute;
				if(getUseCustomTime()){
					hour = mHour;
					minute = mMinute;
				} else {
					hour = mCalendar.getTime().getHours();
					minute= mCalendar.getTime().getMinutes();
				}
				
				 TimePickerDialog dialog = new TimePickerDialog(getContext(), mTimeSetListener, hour, minute, is24HourView);
				 dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getContext().getText(android.R.string.cancel),  (DialogInterface.OnClickListener) null);			
				 dialog.setButton(DialogInterface.BUTTON_NEUTRAL, getContext().getText(de.inovex.app.R.string.current_time), new DialogInterface.OnClickListener() {
						
					@Override
					public void onClick(DialogInterface dialog, int which) {
						startShowingCurrentTime();
					}
				});

				 dialog.show();
			}
		});


        if (mCalendar == null) {
            mCalendar = Calendar.getInstance();
        }

        mFormatChangeObserver = new FormatChangeObserver();
        getContext().getContentResolver().registerContentObserver(
                Settings.System.CONTENT_URI, true, mFormatChangeObserver);

        setFormat();
    }

    @Override
    protected void onAttachedToWindow() {
        mTickerStopped = false;
        super.onAttachedToWindow();
        mHandler = new Handler();
        startShowingCurrentTime();

    }

    synchronized public void startShowingCurrentTime(){
    	setUseCustomTime(false);
        /**
         * requests a tick on the next hard-second boundary
         */
        mTicker = new Runnable() {
                public void run() {

                    if (mTickerStopped) return;
                    if (getUseCustomTime()) return;
                    mCalendar.setTimeInMillis(System.currentTimeMillis());
                    setText(DateFormat.format(mFormat, mCalendar));
                    invalidate();
                    long now = SystemClock.uptimeMillis();
                    long next = now + (1000 - now % 1000);
                    mHandler.postAtTime(mTicker, next);
                }
            };
        mTicker.run();    	
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mTickerStopped = true;
    }

    /**
     * Pulls 12/24 mode from system settings
     */
    private boolean get24HourMode() {
        return android.text.format.DateFormat.is24HourFormat(getContext());
    }

    private void setFormat() {
        if (get24HourMode()) {
            mFormat = m24;
        } else {
            mFormat = m12;
        }
    }

    private class FormatChangeObserver extends ContentObserver {
        public FormatChangeObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            setFormat();
        }
    }
}
