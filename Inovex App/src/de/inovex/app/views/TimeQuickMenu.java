package de.inovex.app.views;

import java.util.Calendar;
import java.util.Date;

import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.ContentProviderClient;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.RemoteException;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ViewAnimator;
import de.inovex.app.R;
import de.inovex.app.provider.DataUtilities;
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
	private TimePickerButton mTimePickerButtonStart;
	private TimePickerButton mTimePickerButtonEnd;
	private ViewAnimator mViewAnimatorNewTime;
	private Uri mCurrentTimeEntry;

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
		mTimePickerButtonStart = (TimePickerButton) findViewById(R.id.timepicker_enter_start_time);
		mTimePickerButtonEnd = (TimePickerButton) findViewById(R.id.timepicker_enter_end_time);
		mViewAnimatorNewTime = (ViewAnimator) findViewById(R.id.viewanimator_new_time);

		mTimePickerButtonStart.setTextPrefix(getContext().getText(R.string.enter_start_time).toString() + " ");
		mTimePickerButtonStart.setFormat("k:mm");
		mTimePickerButtonStart.setTimeSetCallbackListener(new OnTimeSetListener() {
			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				Date date = new Date();
				date.setHours(hourOfDay);
				date.setMinutes(minute);
				try {
					//TODO: remove Mock
					mCurrentTimeEntry = DataUtilities.saveTime(getContext(), "Kick off", "Mobile B2E", date, null, InovexContentProvider.Types.TIME, -1);
				} catch (RemoteException e) {
					Toast.makeText(getContext(), getContext().getText(R.string.error_saving_time), Toast.LENGTH_LONG);
					e.printStackTrace();
				}
				updateView();
			}
		});

		mTimePickerButtonEnd.setTextPrefix(getContext().getText(R.string.enter_end_time).toString() + " ");
		mTimePickerButtonEnd.setFormat("k:mm");
		mTimePickerButtonEnd.setTimeSetCallbackListener(new OnTimeSetListener() {
			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				Date date = new Date();
				date.setHours(hourOfDay);
				date.setMinutes(minute);
				try {
					//TODO: remove Mock
					int updateCount = DataUtilities.updateTime(getContext(), mCurrentTimeEntry, "Kick off", "Mobile B2E", null, date, InovexContentProvider.Types.TIME, -1);
					System.out.println("update count: " + updateCount);
					mCurrentTimeEntry = null;
				} catch (RemoteException e) {
					Toast.makeText(getContext(), getContext().getText(R.string.error_saving_time), Toast.LENGTH_LONG);
					e.printStackTrace();
				}
				updateView();
			}
		});

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
		if (mCurrentTimeEntry != null) {
			mViewAnimatorNewTime.setDisplayedChild(1);
		} else {
			mViewAnimatorNewTime.setDisplayedChild(0);
		}
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
				formattedMorningStartTime = DateFormat.format("k:mm", new Date(holder.morningStartTime));
			}
			mTextViewMorningStartTime.setText(formattedMorningStartTime);
			CharSequence formattedMorningEndTime;
			if (holder.morningEndTime == 0) {
				formattedMorningEndTime = noTime;
			} else {
				formattedMorningEndTime = DateFormat.format("k:mm", new Date(holder.morningEndTime));
			}
			mTextViewMorningEndTime.setText(formattedMorningEndTime);
			CharSequence formattedNoonStartTime;
			if (holder.noonStartTime == 0) {
				formattedNoonStartTime = noTime;
			} else {
				formattedNoonStartTime = DateFormat.format("k:mm", new Date(holder.noonStartTime));
			}
			mTextViewNoonStartTime.setText(formattedNoonStartTime);
			CharSequence formattedNoonEndTime;
			if (holder.noonEndTime == 0) {
				formattedNoonEndTime = noTime;
			} else {
				formattedNoonEndTime = DateFormat.format("k:mm", new Date(holder.noonEndTime));
			}
			mTextViewNoonEndTime.setText(formattedNoonEndTime);

			// calculate times
			Calendar cal = Calendar.getInstance();
			String formattedSaldoTime = noTime;
			String formattedBreakTime = noTime;
			String formattedTotalTime = noTime;
			if (holder.morningStartTime != 0 && holder.morningEndTime != 0) {
				long totalTime = holder.morningEndTime - holder.morningStartTime;
				cal.setTimeInMillis(totalTime);
				formattedTotalTime = cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE);
				long saldoTime = totalTime - 8 * 60 * 60 * 1000;
				if (saldoTime < 0) {
					saldoTime *= -1;
					formattedSaldoTime = "-";
					mTextViewSaldoTime.setTextColor(0xffaa1111);
				} else {
					formattedSaldoTime = "+";
					mTextViewSaldoTime.setTextColor(0xff11aa11);
				}
				cal.setTimeInMillis(saldoTime);
				formattedSaldoTime += DateFormat.format("k:mm", cal).toString();
				if (holder.noonStartTime != 0) {
					long breakTime = holder.noonStartTime - holder.morningEndTime;
					cal.setTimeInMillis(breakTime);
					formattedBreakTime = cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE);
					if (holder.noonEndTime != 0) {
						totalTime = (holder.morningEndTime - holder.morningStartTime) + (holder.noonEndTime - holder.noonStartTime);
						cal.setTimeInMillis(totalTime);
						formattedTotalTime = cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE);
						saldoTime = totalTime - 8 * 60 * 60 * 1000;
						if (saldoTime < 0) {
							saldoTime *= -1;
							formattedSaldoTime = "-";
							mTextViewSaldoTime.setTextColor(0xffaa1111);
						} else {
							formattedSaldoTime = "+";
							mTextViewSaldoTime.setTextColor(0xff11aa11);
						}
						cal.setTimeInMillis(saldoTime);
						formattedSaldoTime += DateFormat.format("k:mm", cal).toString();
					}
				}
			}
			mTextViewSaldoTime.setText(formattedSaldoTime);
			mTextViewBreakTime.setText(formattedBreakTime);
			mTextViewTotalTime.setText(formattedTotalTime);
		}
	}

	private TimeDataHolder getTodaysTimes() {
		ContentProviderClient client = getContext().getContentResolver().acquireContentProviderClient(InovexContentProvider.CONTENT_URI_TIMES);

		try {
			TimeDataHolder holder = new TimeDataHolder();
			String selection = Columns.START_DATE + ">? AND " + Columns.START_DATE + "<?";
			Date fromDate = new Date();
			fromDate.setHours(0);
			fromDate.setMinutes(0);
			fromDate.setSeconds(0);
			Date toDate = new Date();
			toDate.setDate(toDate.getDate() + 1);
			toDate.setHours(0);
			toDate.setMinutes(0);
			toDate.setSeconds(0);
			String[] selectionArgs = { "" + fromDate.getTime(), "" + toDate.getTime() };
			Cursor c = client.query(InovexContentProvider.CONTENT_URI_TIMES, PROJECTION, selection, selectionArgs, Columns.START_DATE + " ASC");
			Log.d(TAG, "Todays time records: " + c.getCount());
			boolean hasData = c.moveToFirst();
			if (!hasData) {
				c.close();
				return holder;
			}
			int startDateIndex = c.getColumnIndex(Columns.START_DATE);
			int endDateIndex = c.getColumnIndex(Columns.END_DATE);
			holder.morningStartTime = c.getLong(startDateIndex);
			holder.morningEndTime = c.getLong(endDateIndex);
			if (c.moveToNext()) {
				holder.noonStartTime = c.getLong(startDateIndex);
				holder.noonEndTime = c.getLong(endDateIndex);
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
