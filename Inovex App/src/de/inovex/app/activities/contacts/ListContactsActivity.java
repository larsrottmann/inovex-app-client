package de.inovex.app.activities.contacts;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import de.inovex.app.R;
import de.inovex.app.service.ContactsService;

public class ListContactsActivity extends Activity {
	private static final String TAG = "ListContactsActivity";

	private void loadContacts() {
		Cursor cursor = getContentResolver().query(
				ContactsContract.Contacts.CONTENT_URI
				, null // projection
				, null // selection
				, null // selectionArgs
				, null // sortOrder
		);
		while (cursor.moveToNext()) {
			Log.i(TAG, "---------------- entry ---------------");
			Log.i(TAG, "DisplayName:         "+cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
			Log.i(TAG, "IN_VISIBLE_GROUP:    "+cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.IN_VISIBLE_GROUP)));
			// data
			Cursor cursor2 = getContentResolver().query(
					ContactsContract.Data.CONTENT_URI
					, null
					, ContactsContract.Data.CONTACT_ID+"="+cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts._ID))
					, null
					, null
			);
			while (cursor2.moveToNext()) {
				Log.i(TAG, "   MIMETYPE:      "+cursor2.getString(cursor2.getColumnIndex(ContactsContract.Data.MIMETYPE)));
				Log.i(TAG, "   DISPLAY_NAME:      "+cursor2.getString(cursor2.getColumnIndex(ContactsContract.Data.DISPLAY_NAME)));
			}
			// RawContact
			cursor2 = getContentResolver().query(
					ContactsContract.RawContacts.CONTENT_URI
					, null
					, ContactsContract.RawContacts.CONTACT_ID+"="+cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts._ID))
					, null
					, null
			);
			while (cursor2.moveToNext()) {
				Log.i(TAG, "   ACCOUNT_TYPE:      "+cursor2.getString(cursor2.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_TYPE)));
				Log.i(TAG, "   ACCOUNT_NAME:      "+cursor2.getString(cursor2.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_NAME)));
			}
			Log.i(TAG, "--------------------------------------");
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contacts);

		// import
		Intent serviceIntent = new Intent(this, ContactsService.class);
		startService(serviceIntent);

		loadContacts();
	}
}
