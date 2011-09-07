package de.inovex.app.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;
import de.inovex.app.R;

public class ContactsService extends IntentService {
	static private final String TAG = "ContactsService";
	static private Long inovexGroupId;

	static public long getInovexGroupId(ContentResolver contentResolver) {
		if (inovexGroupId != null) return inovexGroupId;

		// inovex gruppe finden
		Cursor cursor = contentResolver.query(
				ContactsContract.Groups.CONTENT_URI
				, null // projection
				, ContactsContract.Groups.TITLE+"='inovex'" // selection
				, null // selectionArgs
				, null // sortOrder
		);
		if (cursor.moveToFirst()) {
			long id = cursor.getLong(cursor.getColumnIndex(ContactsContract.Groups._ID));
			cursor.close();
			return inovexGroupId = id;
		} else {
			cursor.close();
			// falls diese nicht existiert, anlegen
			ContentValues values = new ContentValues(1);
			values.put(ContactsContract.Groups.TITLE, "inovex");
			Uri uri = contentResolver.insert(ContactsContract.Groups.CONTENT_URI, values);
			cursor = contentResolver.query(uri, null, ContactsContract.Groups.TITLE+"='inovex'", null, null);
			if (cursor.moveToFirst()) {
				long id = cursor.getLong(cursor.getColumnIndex(ContactsContract.Groups._ID));
				cursor.close();
				return inovexGroupId = id;
			} else {
				throw new IllegalStateException();
			}
		}
	}

	private Handler handler;

	public ContactsService() {
		super(TAG);
	}

	private void importContacts() throws JsonParseException, JsonMappingException, IOException {
		// TODO im 2. schritt werden die daten von der server api geholt
		InputStream is = getResources().openRawResource(R.raw.contacts_dump);
		ObjectMapper mapper = new ObjectMapper();
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> allContacts = mapper.readValue(is, List.class);
		Log.i(TAG, "contacts: "+allContacts.size());
	}

	private void insertNewContact(String email, String accountName, String displayName, String phoneNumber){
		ContentResolver cr = getContentResolver();

		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

		// account
		ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
			.withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, email)
			.withValue(ContactsContract.RawContacts.ACCOUNT_NAME, accountName)
			.build());

		// display name
		ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
			.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
			.withValue(ContactsContract.Data.MIMETYPE,
					ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
			.withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, displayName)
			.build());

		// phone number
		ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
				.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
				.withValue(ContactsContract.Data.MIMETYPE,
						ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
				.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber)
				.withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_HOME)
				.build());

		// inovex group
		ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
				.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
				.withValue(ContactsContract.Data.MIMETYPE,
						ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE)
				.withValue(ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID, getInovexGroupId(getContentResolver()))
				.build());

		try {
			cr.applyBatch(ContactsContract.AUTHORITY, ops);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// import contacts
		handler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), "start importing contacts", Toast.LENGTH_SHORT).show();
			}
		});

		synchronized (this) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		try {
			importContacts();
		} catch (Exception e) {
			e.printStackTrace();
		}
		//insertNewContact(null, null, "neu neu33", "1245");
		getContentResolver().notifyChange(ContactsContract.Contacts.CONTENT_URI, null);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		handler = new Handler();
		return super.onStartCommand(intent, flags, startId);
	}
}

