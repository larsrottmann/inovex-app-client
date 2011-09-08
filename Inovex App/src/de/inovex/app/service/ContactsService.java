package de.inovex.app.service;

import java.io.ByteArrayOutputStream;
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
import android.content.ContentProviderOperation.Builder;
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
	static private class ImportContactOperations {
		static final int INSERT_RAW_CONTACT = 			1;
		static final int UPDATE_STRUCTURED_NAME = 		2;
		static final int UPDATE_ORGANIZATION = 			4;
		static final int INSERT_GROUP_MEMBERSHIP = 		8;
		static final int INSERT_PHOTO = 				16;
		static final int UPDATE_PHOTO =					32;
	}

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

	private int checkImportOperations(Cursor cursorOrganization, String givenName, String familyName, String symbol, String lob, String location, String photoMD5) {
		if (cursorOrganization.moveToFirst()) {
			int r = 0;
			int contactId = cursorOrganization.getInt(cursorOrganization.getColumnIndex(ContactsContract.CommonDataKinds.Organization.CONTACT_ID));

			// check lob symbol location
			String curSymbol = cursorOrganization.getString(cursorOrganization.getColumnIndex(ContactsContract.CommonDataKinds.Organization.SYMBOL));
			String curLob = cursorOrganization.getString(cursorOrganization.getColumnIndex(ContactsContract.CommonDataKinds.Organization.DEPARTMENT));
			String curLocation = cursorOrganization.getString(cursorOrganization.getColumnIndex(ContactsContract.CommonDataKinds.Organization.OFFICE_LOCATION));
			String curCompany = cursorOrganization.getString(cursorOrganization.getColumnIndex(ContactsContract.CommonDataKinds.Organization.COMPANY));
			if (
					!curSymbol.equals(symbol)
					|| !curLob.equals(lob)
					|| !curLocation.equals(location)
					|| !curCompany.equals("inovex GmbH")
			) {
				r |= ImportContactOperations.UPDATE_ORGANIZATION;
			}
			cursorOrganization.close();

			// check inovex group
			Cursor cursor = getContentResolver().query(
					ContactsContract.Data.CONTENT_URI
					, null // projection
					, ContactsContract.Data.MIMETYPE+"= ? AND "+ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID+"= ? AND "+ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID+"= ?" // selection
					, new String[] { // selectionArgs
						ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE
						, String.valueOf(ContactsService.getInovexGroupId(getContentResolver()))
						, String.valueOf(contactId)
					}
					, null // sortOrder
			);
			if (!cursor.moveToFirst()) {
				r |= ImportContactOperations.INSERT_GROUP_MEMBERSHIP;
			}
			cursor.close();

			// check given family display name
			cursor = getContentResolver().query(
					ContactsContract.Data.CONTENT_URI
					, null // projection
					, ContactsContract.Data.MIMETYPE+"= ? AND "+ContactsContract.CommonDataKinds.StructuredName.CONTACT_ID+"= ?" // selection
					, new String[] { // selectionArgs
						ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
						, String.valueOf(contactId)
					}
					, null // sortOrder
			);
			if (cursor.moveToFirst()) {
				String curGivenName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));
				String curFamilyName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));
				String curDisplayName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME));
				if (
						!curGivenName.equals(givenName)
						|| !curFamilyName.equals(familyName)
						|| !curDisplayName.equals(givenName+' '+familyName+" ("+symbol+')')
				) {
					r |= ImportContactOperations.UPDATE_STRUCTURED_NAME;
				}
			}

			// check for photo
			if (photoMD5 != null) {
				cursor = getContentResolver().query(
						ContactsContract.Data.CONTENT_URI
						, null // projection
						, ContactsContract.Data.MIMETYPE+"= ? AND "+ContactsContract.CommonDataKinds.StructuredName.CONTACT_ID+"= ?" // selection
						, new String[] { // selectionArgs
							ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
							, String.valueOf(contactId)
						}
						, null // sortOrder
				);
				if (cursor.moveToFirst()) {
					String curPhotoMD5 = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Photo.SYNC1)); // im SYNC1 feld wird die md5 gespeichert
					if (!photoMD5.equals(curPhotoMD5)) {
						r |= ImportContactOperations.UPDATE_PHOTO;
					}
				} else {
					r |= ImportContactOperations.INSERT_PHOTO;
				}
			}

			return r;
		} else {
			// alles insert
			return ImportContactOperations.INSERT_GROUP_MEMBERSHIP | ImportContactOperations.INSERT_RAW_CONTACT | ImportContactOperations.INSERT_PHOTO;
		}
	}

	private byte[] getBytesFromInputStream(InputStream is) throws IOException {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();

		int b;
		while ((b = is.read()) != -1) {
			outStream.write(b);
		}
		byte[] r = outStream.toByteArray();
		outStream.close();

		return r;
	}

	private void importContacts() throws JsonParseException, JsonMappingException, IOException {
		// TODO im 2. schritt werden die daten von der server api geholt
		InputStream is = getResources().openRawResource(R.raw.contacts_dump);
		ObjectMapper mapper = new ObjectMapper();
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> allContacts = mapper.readValue(is, List.class);
		for (Map<String, Object> contact : allContacts) {
			insertUpdateContact(
					(String) contact.get("givenName")
					, (String) contact.get("familyName")
					, (String) contact.get("symbol")
					, (String) contact.get("lob")
					, (String) contact.get("location")
					, (String) contact.get("photoMD5")
			);
		}
	}

	private void insertUpdateContact(String givenName, String familyName, String symbol, String lob, String location, String photoMD5) {
		// bestehenden finden
		ContentResolver cr = getContentResolver();
		Cursor cursor = cr.query(
				ContactsContract.Data.CONTENT_URI
				, null // projection
				, ContactsContract.Data.MIMETYPE+"= ? AND "+ContactsContract.CommonDataKinds.Organization.SYMBOL+"= ?" // selection
				, new String[] { // selectionArgs
					ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE
					, symbol
				}
				, null // sortOrder
		);
		int rawContactId=0;
		int operations;
		if (cursor.moveToFirst()) {
			rawContactId = cursor.getInt(cursor.getColumnIndex(ContactsContract.Data.RAW_CONTACT_ID));
		}
		operations = checkImportOperations(cursor, givenName, familyName, symbol, lob, location, photoMD5);

		if (operations == 0) return;

		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

		// account
		if ((operations & ImportContactOperations.INSERT_RAW_CONTACT) > 0) {
			ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
				.withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null) // TODO auto-detect
				.withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null) // auto-detect
				.build());
		}

		// display given family name
		String displayName = givenName+' '+familyName+" ("+symbol+')';
		if ((operations & (ImportContactOperations.INSERT_RAW_CONTACT | ImportContactOperations.UPDATE_STRUCTURED_NAME)) > 0) {
			Builder builder;
			if ((operations & ImportContactOperations.INSERT_RAW_CONTACT) > 0) {
				builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
				builder.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0);
				builder.withValue(ContactsContract.Data.MIMETYPE,
						ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
			} else {
				builder = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI);
				builder.withSelection(
						ContactsContract.Data.RAW_CONTACT_ID+"=? AND "+ContactsContract.Data.MIMETYPE+"=?"
						, new String[] {
								String.valueOf(rawContactId)
								, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
						}
				);
			}
			builder.withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, displayName)
				.withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, givenName)
				.withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, familyName);
			ops.add(builder.build());
		}

		// symbol, lob, location, company
		if ((operations & (ImportContactOperations.INSERT_RAW_CONTACT | ImportContactOperations.UPDATE_ORGANIZATION)) > 0) {
			Builder builder;
			if ((operations & ImportContactOperations.INSERT_RAW_CONTACT) > 0) {
				builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
				builder.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0);
				builder.withValue(ContactsContract.Data.MIMETYPE,
						ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE);
			} else {
				builder = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI);
				builder.withSelection(
						ContactsContract.Data.RAW_CONTACT_ID+"=? AND "+ContactsContract.Data.MIMETYPE+"=?"
						, new String[] {
								String.valueOf(rawContactId)
								, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE
						}
				);
			}
			ops.add(builder
					.withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, "inovex GmbH")
					.withValue(ContactsContract.CommonDataKinds.Organization.DEPARTMENT, lob)
					.withValue(ContactsContract.CommonDataKinds.Organization.SYMBOL, symbol)
					.withValue(ContactsContract.CommonDataKinds.Organization.TYPE, ContactsContract.CommonDataKinds.Organization.TYPE_WORK)
					.withValue(ContactsContract.CommonDataKinds.Organization.OFFICE_LOCATION, location)
					.build());
		}

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
		if ((operations & ImportContactOperations.INSERT_GROUP_MEMBERSHIP) > 0) {
			ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
					.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
					.withValue(ContactsContract.Data.MIMETYPE,
							ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE)
					.withValue(ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID, getInovexGroupId(getContentResolver()))
					.build());
		}

		// photo
		if ((operations & (ImportContactOperations.INSERT_PHOTO | ImportContactOperations.UPDATE_PHOTO)) > 0) {
			InputStream is = getResources().openRawResource(R.raw.testimg);
			try {
				byte[] photoData = getBytesFromInputStream(is);

				Builder builder;
				if ((operations & ImportContactOperations.INSERT_PHOTO) > 0) {
					builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
						.withValue(ContactsContract.Data.MIMETYPE,
							ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE);
					if ((operations & ImportContactOperations.INSERT_RAW_CONTACT) > 0) {
						builder.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0);
					} else {
						builder.withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
					}
				} else {
					builder = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI);
					builder.withSelection(
							ContactsContract.Data.RAW_CONTACT_ID+"=? AND "+ContactsContract.Data.MIMETYPE+"=?"
							, new String[] {
									String.valueOf(rawContactId)
									, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
							}
					);
				}
				ops.add(builder
						.withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, photoData)
						.withValue(ContactsContract.CommonDataKinds.Photo.SYNC1, photoMD5)
						.build());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		Log.i(TAG, "inserting/updating contact: "+displayName);
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
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		try {
			importContacts();
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

