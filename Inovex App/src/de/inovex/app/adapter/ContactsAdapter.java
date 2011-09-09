package de.inovex.app.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.AlphabetIndexer;
import android.widget.ImageView;
import android.widget.ResourceCursorAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;
import de.inovex.app.R;

public class ContactsAdapter extends ResourceCursorAdapter implements SectionIndexer {
	private AlphabetIndexer alphaIndexer;

	public ContactsAdapter(Context arg0, int arg1, Cursor arg2) {
		super(arg0, arg1, arg2);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		// contact id im tag vom view speichern
		view.setTag(cursor.getInt(cursor.getColumnIndex(ContactsContract.Data.CONTACT_ID)));

		String displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

		// query organization data
		Cursor oCur = context.getContentResolver().query(
				ContactsContract.Data.CONTENT_URI
				, null // projection
				, ContactsContract.Data.MIMETYPE+"= ? AND "+ContactsContract.Data.CONTACT_ID+"= ?" // selection
				, new String[] { // selectionArgs
					ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE
					, cursor.getString(cursor.getColumnIndex(ContactsContract.Data.CONTACT_ID))
				}
				, null // sortOrder
		);

		String lob, location;
		if (oCur.moveToFirst()) {
			lob = oCur.getString(oCur.getColumnIndex(ContactsContract.CommonDataKinds.Organization.DEPARTMENT));
			location = oCur.getString(oCur.getColumnIndex(ContactsContract.CommonDataKinds.Organization.OFFICE_LOCATION));
		} else {
			lob = location = "";
		}
		oCur.close();

		TextView tvTitle = (TextView) view.findViewById(R.id.contacts_item_title);
		tvTitle.setText(displayName);

		TextView tvDetails = (TextView) view.findViewById(R.id.contacts_item_details);
		tvDetails.setText(lob+", "+location);

		// photo
		oCur = context.getContentResolver().query(
				ContactsContract.Data.CONTENT_URI
				, null // projection
				, ContactsContract.Data.MIMETYPE+"= ? AND "+ContactsContract.Data.CONTACT_ID+"= ?" // selection
				, new String[] { // selectionArgs
					ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
					, cursor.getString(cursor.getColumnIndex(ContactsContract.Data.CONTACT_ID))
				}
				, null // sortOrder
		);
		ImageView imgView = (ImageView) view.findViewById(R.id.contacts_item_image);
		if (oCur.moveToFirst()) {
			byte[] data = oCur.getBlob(oCur.getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO));
			if (data != null && data.length > 0) {
				imgView.setImageBitmap(BitmapFactory.decodeByteArray(data, 0, data.length));
			} else {
				imgView.setImageResource(R.drawable.ic_contact_picture);
			}
		} else {
			imgView.setImageResource(R.drawable.ic_contact_picture);
		}
		oCur.close();
	}

	@Override
	public void changeCursor(Cursor cursor) {
		super.changeCursor(cursor);
		if (cursor != null) {
			alphaIndexer = new AlphabetIndexer(cursor, cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME), " ABCDEFGHIJKLMNOPQRSTUVWXYZ");
		}
	}

	@Override
	public int getPositionForSection(int section) {
		if (alphaIndexer == null) return 0;
		return alphaIndexer.getPositionForSection(section);
	}

	@Override
	public int getSectionForPosition(int position) {
		if (alphaIndexer == null) return 0;
		return alphaIndexer.getSectionForPosition(position);
	}

	@Override
	public Object[] getSections() {
		if (alphaIndexer == null) return null;
		return alphaIndexer.getSections();
	}
}
