package de.inovex.app.activities.contacts;

import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.CursorAdapter;
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
			}
		});
	}

	private void loadContacts() {
		Cursor cursor = getContentResolver().query(
				ContactsContract.Data.CONTENT_URI
				, null // projection
				, ContactsContract.Data.MIMETYPE+"= ? AND "+ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID+"= ?" // selection
				, new String[] { // selectionArgs
					ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE
					, String.valueOf(ContactsService.getInovexGroupId(getContentResolver()))
				}
				, null // sortOrder
		);
		cursor.setNotificationUri(getContentResolver(), ContactsContract.Contacts.CONTENT_URI);

		Set<Integer> ids = new HashSet<Integer>();
		while (cursor.moveToNext()) {
			Log.i(TAG, "---------------- entry ---------------");
			Log.i(TAG, "DisplayName:         "+cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME)));
			Log.i(TAG, "--------------------------------------");
			ids.add(cursor.getInt(cursor.getColumnIndex(ContactsContract.Data.CONTACT_ID)));
		}

		cursor.moveToFirst();
		((CursorAdapter) listContacts.getAdapter()).changeCursor(cursor);
	}

	private void loadGroups() {
		Cursor cursor = getContentResolver().query(
				ContactsContract.Groups.CONTENT_URI
				, null // projection
				, null // selection
				, null // selectionArgs
				, null // sortOrder
		);
		while (cursor.moveToNext()) {
			Log.i(TAG, "---------------- group ---------------");
			Log.i(TAG, "Title:         "+cursor.getString(cursor.getColumnIndex(ContactsContract.Groups.TITLE)));
			Log.i(TAG, "Notes:    "+cursor.getString(cursor.getColumnIndex(ContactsContract.Groups.NOTES)));
			Log.i(TAG, "--------------------------------------");
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contacts);

		initList();

		// import
		Intent serviceIntent = new Intent(this, ContactsService.class);
		startService(serviceIntent);

		loadContacts();
		loadGroups();
	}
}
