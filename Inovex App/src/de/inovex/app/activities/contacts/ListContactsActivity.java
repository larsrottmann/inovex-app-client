package de.inovex.app.activities.contacts;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;
import de.inovex.app.R;
import de.inovex.app.service.ContactsService;

public class ListContactsActivity extends Activity {
	private static final String TAG = "ListContactsActivity";
	private ListView listContacts;

	private void initList() {
		listContacts = (ListView) findViewById(R.id.list_contacts);
		listContacts.setAdapter(new ResourceCursorAdapter(
				this
				, R.layout.contacts_item
				, null
		) {
			@Override
			public void bindView(View view, Context context, Cursor cursor) {
				String displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

				// query organization data
				Cursor oCur = getContentResolver().query(
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
				oCur = getContentResolver().query(
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
		});
	}

	private void loadContacts(String filter) {
		String selection;
		String[] selectionArgs;
		if (filter == null) {
			selection = ContactsContract.Data.MIMETYPE+"= ? AND "+
				ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID+"= ?";
			selectionArgs = new String[] { // selectionArgs
				ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE
				, String.valueOf(ContactsService.getInovexGroupId(getContentResolver()))
			};
		} else {
			selection = ContactsContract.Data.MIMETYPE+"= ? AND "+
				ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID+"= ? AND "+
				ContactsContract.Data.DISPLAY_NAME+" LIKE ?";
			selectionArgs = new String[] { // selectionArgs
				ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE
				, String.valueOf(ContactsService.getInovexGroupId(getContentResolver()))
				, "%"+filter+"%"
			};
		}
		Cursor cursor = getContentResolver().query(
				ContactsContract.Data.CONTENT_URI
				, null // projection
				, selection
				, selectionArgs
				, null // sortOrder
		);
		cursor.setNotificationUri(getContentResolver(), ContactsContract.Contacts.CONTENT_URI);

		while (cursor.moveToNext()) {
			Log.i(TAG, "---------------- entry ---------------");
			Log.i(TAG, "DisplayName:         "+cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME)));
			Log.i(TAG, "--------------------------------------");
		}

		cursor.moveToFirst();
		((CursorAdapter) listContacts.getAdapter()).changeCursor(cursor);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contacts);

		initList();

		// import
		Intent serviceIntent = new Intent(this, ContactsService.class);
		startService(serviceIntent);

		// show/hide search ui elements
		String query = getIntent().getStringExtra("query");
		TextView searchLabel = (TextView) findViewById(R.id.contacts_search_label);
		View showAll = findViewById(R.id.contacts_show_all);
		searchLabel.setVisibility(query==null?View.GONE:View.VISIBLE);
		showAll.setVisibility(query==null?View.GONE:View.VISIBLE);
		if (query != null) {
			searchLabel.setText(getResources().getString(R.string.search_results_for, query));
			showAll.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent i = new Intent(getBaseContext(), ListContactsActivity.class);
					startActivity(i);
				}
			});
		}
		loadContacts(query);
	}
}
