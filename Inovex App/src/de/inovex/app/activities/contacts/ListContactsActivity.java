package de.inovex.app.activities.contacts;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import de.inovex.app.R;
import de.inovex.app.service.ContactsService;

public class ListContactsActivity extends Activity {
	private static final String TAG = "ListContactsActivity";
	private ListView listContacts;

	private void initList() {
		listContacts = (ListView) findViewById(R.id.list_contacts);
		listContacts.setAdapter(new SimpleCursorAdapter(
				this
				, R.layout.contacts_item
				, null
				, new String[] {
						ContactsContract.Contacts.DISPLAY_NAME
						, ContactsContract.Contacts.DISPLAY_NAME
				}
				, new int[] {
						R.id.contacts_item_title
						, R.id.contacts_item_details
				}
		));
	}

	private void loadContacts() {
		Cursor cursor = getContentResolver().query(
				ContactsContract.Data.CONTENT_URI
				, null // projection
				, ContactsContract.Data.MIMETYPE+"='"+ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE+"' AND "+ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID+"="+ContactsService.getInovexGroupId(getContentResolver()) // selection
				, null // selectionArgs
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
