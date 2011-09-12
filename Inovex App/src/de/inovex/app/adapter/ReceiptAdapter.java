package de.inovex.app.adapter;

import java.lang.ref.SoftReference;
import java.util.Date;
import java.util.HashMap;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import de.inovex.app.R;
import de.inovex.app.activities.ListReceiptActivity;
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
	private BitmapDrawable mDefaultThumbnail;
	private BitmapDrawable NULL_DRAWABLE = new BitmapDrawable((Bitmap)null);

	private final HashMap<String, SoftReference<BitmapDrawable>> mThumbCache = new HashMap<String, SoftReference<BitmapDrawable>>();

	private Drawable getCachedReceipt(String id, Drawable defaulThumb) {
		BitmapDrawable drawable = null;

		SoftReference<BitmapDrawable> reference = mThumbCache.get(id);
		if (reference != null) {
			drawable = reference.get();
		}

		if (drawable == null) {
			Bitmap bitmap = DataUtilities.loadThumbnail(id);
			if (bitmap == null) {
				drawable = NULL_DRAWABLE;
			} else {
				drawable = new BitmapDrawable(bitmap);
			}

			mThumbCache.put(id, new SoftReference<BitmapDrawable>(drawable));
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
		mDefaultThumbnail = new BitmapDrawable( BitmapFactory.decodeResource(activity.getResources(), R.drawable.receipt_icon));
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ReceiptViewHolder holder = (ReceiptViewHolder) view.getTag();

		CharSequence formattedDate = DateFormat.format("MMM dd, yyyy k:mm", new Date(cursor.getLong(mIndexCreated)));
		holder.mThumbnail.setText(formattedDate);
		
		String id = cursor.getString(mIndexId);
		Drawable thumb = getCachedReceipt(id, mDefaultThumbnail);
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
