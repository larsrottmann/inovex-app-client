package de.inovex.app.views;

import java.util.Date;

import android.content.ContentProviderClient;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.os.RemoteException;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;
import de.inovex.app.R;
import de.inovex.app.provider.InovexContentProvider;
import de.inovex.app.provider.InovexContentProvider.Columns;

public class TimeQuickMenu extends RelativeLayout {

	private static final String TAG = "TimeQuickMenu";

	private static final String[] PROJECTION = new String[] { InovexContentProvider.Columns.START_DATE, InovexContentProvider.Columns.END_DATE };

	private TimeContentObserver mTimeContentObserver = new TimeContentObserver(new Handler());

	private TextView mTextViewSaldoTime;
	private TextView mTextViewBreakTime;
	private TextView mTextViewTotalTime;
	private TextView mTextViewMorningStartTime;
	private TextView mTextViewMorningEndTime;
	private TextView mTextViewNoonStartTime;
	private TextView mTextViewNoonEndTime;
	private TimePickerButton mTimePickerButton;

	private static class TimeDataHolder {
		long morningStartTime;
		long morningEndTime;
		long noonStartTime;
		long noonEndTime;
	}

	public class TimeContentObserver extends ContentObserver {
		public TimeContentObserver(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			updateView();
		}
	}

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

		updateView();
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		getContext().getContentResolver().registerContentObserver(InovexContentProvider.CONTENT_URI, true, mTimeContentObserver);
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		getContext().getContentResolver().unregisterContentObserver(mTimeContentObserver);
	}

	private void updateView() {
		Log.d(TAG, "updateView()");
		showTimes();
	}

	private void showTimes() {
		TimeDataHolder holder = getTodaysTimes();
		if (holder != null) {
			String noTime = "--:--";
			CharSequence formattedMorningStartTime;
			if (holder.morningStartTime == 0) {
				formattedMorningStartTime = noTime;
			} else {
				formattedMorningStartTime = DateFormat.format("hh:mm", new Date(holder.morningStartTime));
			}
			mTextViewMorningStartTime.setText(formattedMorningStartTime);
			CharSequence formattedMorningEndTime;
			if (holder.morningEndTime == 0) {
				formattedMorningEndTime = noTime;
			} else {
				formattedMorningEndTime = DateFormat.format("hh:mm", new Date(holder.morningEndTime));
			}
			mTextViewMorningEndTime.setText(formattedMorningEndTime);
			CharSequence formattedNoonStartTime;
			if (holder.noonStartTime == 0) {
				formattedNoonStartTime = noTime;
			} else {
				formattedNoonStartTime = DateFormat.format("hh:mm", new Date(holder.noonStartTime));
			}
			mTextViewNoonStartTime.setText(formattedNoonStartTime);
			CharSequence formattedNoonEndTime;
			if (holder.noonEndTime == 0) {
				formattedNoonEndTime = noTime;
			} else {
				formattedNoonEndTime = DateFormat.format("hh:mm", new Date(holder.noonEndTime));
			}
			mTextViewNoonEndTime.setText(formattedNoonEndTime);

			//TODO: calculate times
			mTextViewSaldoTime.setText(noTime);
			mTextViewBreakTime.setText(noTime);
			mTextViewTotalTime.setText(noTime);
		}
	}

	private TimeDataHolder getTodaysTimes() {
		ContentProviderClient client = getContext().getContentResolver().acquireContentProviderClient(InovexContentProvider.CONTENT_URI_TIMES);

		try {
			TimeDataHolder holder = new TimeDataHolder();
			String selection = Columns.START_DATE + ">?";
			Date now = new Date();
			now.setHours(0);
			now.setMinutes(0);
			now.setSeconds(0);
			String[] selectionArgs = { "" + now.getTime() };
			Cursor c = client.query(InovexContentProvider.CONTENT_URI_TIMES, PROJECTION, selection, selectionArgs, Columns.START_DATE + " ASC");
			Log.d(TAG, "Cursor count: " + c.getCount());

			boolean hasData = c.moveToFirst();
			if (!hasData) {
				c.close();
				return holder;
			}
			int startDateIndex = c.getColumnIndex(Columns.START_DATE);
			int endDateIndex = c.getColumnIndex(Columns.END_DATE);
			holder.morningStartTime = c.getInt(startDateIndex);
			holder.morningEndTime = c.getInt(endDateIndex);
			if (c.moveToNext()) {
				holder.noonStartTime = c.getInt(startDateIndex);
				holder.noonEndTime = c.getInt(endDateIndex);
			}
			c.close();
			return holder;
		} catch (RemoteException e) {
			e.printStackTrace();
		} finally {
			client.release();
		}
		return null;
	}
}
