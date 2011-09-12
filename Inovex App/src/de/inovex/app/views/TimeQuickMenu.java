package de.inovex.app.views;

import de.inovex.app.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class TimeQuickMenu extends RelativeLayout {

	private TextView mTextViewSaldoTime;
	private TextView mTextViewBreakTime;
	private TextView mTextViewTotalTime;
	private TextView mTextViewMorningStartTime;
	private TextView mTextViewMorningEndTime;
	private TextView mTextViewNoonStartTime;
	private TextView mTextViewNoonEndTime;
	private TimePickerButton mTimePickerButton;

	public TimeQuickMenu(final Context activity, AttributeSet attrs) {
		super(activity, attrs);
		LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.time_quick_menu, this);

		mTextViewSaldoTime = (TextView) findViewById(R.id.textview_saldo_time);
		mTextViewBreakTime = (TextView) findViewById(R.id.textview_break_time);
		mTextViewTotalTime = (TextView) findViewById(R.id.textview_total_time);
		mTextViewMorningStartTime = (TextView) findViewById(R.id.textView_morning_start_time);
		mTextViewMorningEndTime = (TextView) findViewById(R.id.textView_morning_end_time);
		mTextViewNoonStartTime = (TextView) findViewById(R.id.textView_noon_start_time);
		mTextViewNoonEndTime = (TextView) findViewById(R.id.textView_noon_end_time);
		mTimePickerButton = (TimePickerButton) findViewById(R.id.timepicker_enter_time);

		mTimePickerButton.setTextPrefix(getContext().getText(R.string.enter_time).toString() + " ");
		mTimePickerButton.setFormat("hh:mm");
	}
}
