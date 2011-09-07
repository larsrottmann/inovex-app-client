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
		for (Map<String, Object> contact : allContacts) {
			insertNewContact(
					(String) contact.get("givenName")
					, (String) contact.get("familyName")
					, (String) contact.get("symbol")
					, (String) contact.get("lob")
					, (String) contact.get("location")
			);
		}
	}

	private void insertNewContact(String givenName, String familyName, String symbol, String lob, String location){
		ContentResolver cr = getContentResolver();

		String displayName = givenName+' '+familyName+" ("+symbol+')';
		// TODO pruefen ob er schon existiert
		// TODO änderungen

		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

		// account
		ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
			.withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null) // TODO auto-detect
			.withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null) // auto-detect
			.build());

		// display given family name
		ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
				.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
				.withValue(ContactsContract.Data.MIMETYPE,
						ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
				.withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, displayName)
				.withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, givenName)
				.withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, familyName)
				.build());

		// symbol, lob, location, company
		ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
				.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
				.withValue(ContactsContract.Data.MIMETYPE,
						ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
				.withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, "inovex GmbH")
				.withValue(ContactsContract.CommonDataKinds.Organization.DEPARTMENT, lob)
				.withValue(ContactsContract.CommonDataKinds.Organization.SYMBOL, symbol)
				.withValue(ContactsContract.CommonDataKinds.Organization.TYPE, ContactsContract.CommonDataKinds.Organization.TYPE_WORK)
				.withValue(ContactsContract.CommonDataKinds.Organization.OFFICE_LOCATION, location)
				.build());

		// phone number
		/*ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
				.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
				.withValue(ContactsContract.Data.MIMETYPE,
						ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
				.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber)
				.withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_HOME)
				.build());
				*/

		// inovex group
		ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
				.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
				.withValue(ContactsContract.Data.MIMETYPE,
						ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE)
				.withValue(ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID, getInovexGroupId(getContentResolver()))
				.build());

		Log.i(TAG, "inserting new contact: "+displayName);
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
			//importContacts();
		} catch (Exception e) {
			e.printStackTrace();
		}

		getContentResolver().notifyChange(ContactsContract.Contacts.CONTENT_URI, null);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		handler = new Handler();
		return super.onStartCommand(intent, flags, startId);
	}
}

