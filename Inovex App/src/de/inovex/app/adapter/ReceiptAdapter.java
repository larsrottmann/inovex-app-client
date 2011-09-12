package de.inovex.app.adapter;

import java.lang.ref.SoftReference;
import java.util.Date;
import java.util.HashMap;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import de.inovex.app.R;
import de.inovex.app.activities.ListReceiptActivity;
import de.inovex.app.drawable.FastBitmapDrawable;
import de.inovex.app.provider.DataUtilities;
import de.inovex.app.provider.InovexContentProvider;

public class ReceiptAdapter extends CursorAdapter {

	private static final String[] PROJECTION_IDS_AND_TITLE = new String[] { InovexContentProvider.Columns.ID, InovexContentProvider.Columns.DESCRIPTION, InovexContentProvider.Columns.TYPE,
			InovexContentProvider.Columns.IMAGE_PATH_URI, InovexContentProvider.Columns.CREATED };

	private LayoutInflater mInflater;
	private int mIndexType;
	private int mIndexId;
	private int mIndexCreated;
	private int mIndexImageUri;
	private FastBitmapDrawable mDefaultThumbnail;
	private FastBitmapDrawable NULL_DRAWABLE = new FastBitmapDrawable((Bitmap)null);

	private final HashMap<String, SoftReference<FastBitmapDrawable>> mThumbCache = new HashMap<String, SoftReference<FastBitmapDrawable>>();

	private Drawable getCachedReceipt(String id, Drawable defaulThumb) {
		FastBitmapDrawable drawable = null;

		SoftReference<FastBitmapDrawable> reference = mThumbCache.get(id);
		if (reference != null) {
			drawable = reference.get();
		}

		if (drawable == null) {
			Log.i("Receipts", "Cache miss!");
			Bitmap bitmap = DataUtilities.loadThumbnail(id);
			if (bitmap == null) {
				drawable = NULL_DRAWABLE;
			} else {
				drawable = new FastBitmapDrawable(bitmap);
			}

			mThumbCache.put(id, new SoftReference<FastBitmapDrawable>(drawable));
		}

        return drawable == NULL_DRAWABLE ? defaulThumb : drawable;
	}
	

	public ReceiptAdapter(ListReceiptActivity activity) {
		super(activity, activity.managedQuery(InovexContentProvider.CONTENT_URI_RECEIPTS, PROJECTION_IDS_AND_TITLE, null, null, InovexContentProvider.Columns.CREATED + " desc"), true);

		final Cursor c = getCursor();

		mIndexCreated = c.getColumnIndex(InovexContentProvider.Columns.CREATED);
		mIndexId = c.getColumnIndex(InovexContentProvider.Columns.ID);
		mIndexType = c.getColumnIndex(InovexContentProvider.Columns.TYPE);
		mIndexId = c.getColumnIndex(InovexContentProvider.Columns.ID);
		mIndexImageUri = c.getColumnIndex(InovexContentProvider.Columns.IMAGE_PATH_URI);
		mInflater = LayoutInflater.from(activity);		
		mDefaultThumbnail = new FastBitmapDrawable( BitmapFactory.decodeResource(activity.getResources(), R.drawable.receipt_icon));
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ReceiptViewHolder holder = (ReceiptViewHolder) view.getTag();

		CharSequence formattedDate = DateFormat.format("MMM dd, yyyy k:mm", new Date(cursor.getLong(mIndexCreated)));
		holder.mThumbnail.setText(formattedDate);
		
		String id = cursor.getString(mIndexId);
		Drawable thumb = getCachedReceipt(id, mDefaultThumbnail);
		Log.i("ReceiptAdapter","setCompundDrawable");
		holder.mThumbnail.setCompoundDrawablesWithIntrinsicBounds(null,thumb, null,null);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		final View view = mInflater.inflate(R.layout.receipt_item, parent, false);

		ReceiptViewHolder holder = new ReceiptViewHolder(view);

		view.setTag(holder);

		return view;
	}
}
