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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;
import de.inovex.app.R;
import de.inovex.app.provider.contact_contracts.ExtraDataKinds;

public class ContactsService extends IntentService {
	static private class ImportContactOperations {
		static final int INSERT_RAW_CONTACT = 			1;
		static final int UPDATE_STRUCTURED_NAME = 		2;
		static final int UPDATE_ORGANIZATION = 			4;
		static final int INSERT_PHOTO = 				8;
		static final int UPDATE_PHOTO =					16;
		static final int INSERT_PHONE_NUMBER_MOBILE = 	32;
		static final int UPDATE_PHONE_NUMBER_MOBILE = 	64;
		static final int INSERT_PHONE_NUMBER_WORK = 	128;
		static final int UPDATE_PHONE_NUMBER_WORK = 	256;
		static final int INSERT_PHONE_NUMBER_HOME = 	512;
		static final int UPDATE_PHONE_NUMBER_HOME = 	1024;
		static final int UPDATE_EMAIL_ADDRESS = 		2048;
		static final int UPDATE_INOVEX = 				4096;
	}

	static private final String TAG = "ContactsService";

	private Handler handler;

	public ContactsService() {
		super(TAG);
	}

	private int checkImportOperations(Cursor cursorOrganization, String givenName, String familyName, String symbol, String lob, String location, String photoMD5, String numberMobile, String numberWork, String numberHome, String emailAddress, String skills) {
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

			// check given family display name
			Cursor cursor = getContentResolver().query(
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
						|| !curDisplayName.equals(givenName+' '+familyName)
				) {
					r |= ImportContactOperations.UPDATE_STRUCTURED_NAME;
				}
			}
			cursor.close();

			// check phone mobile
			if (numberMobile != null) {
				cursor = getContentResolver().query(
						ContactsContract.Data.CONTENT_URI
						, null // projection
						, ContactsContract.Data.MIMETYPE+"= ? AND "+
							ContactsContract.CommonDataKinds.Phone.CONTACT_ID+"= ? AND "+
							ContactsContract.CommonDataKinds.Phone.TYPE+"=?" // selection
						, new String[] { // selectionArgs
							ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
							, String.valueOf(contactId)
							, String.valueOf(ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
						}
						, null // sortOrder
				);
				if (cursor.moveToFirst()) {
					String curNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
					if (!curNumber.equals(numberMobile)) {
						r |= ImportContactOperations.UPDATE_PHONE_NUMBER_MOBILE;
					}
				} else {
					r |= ImportContactOperations.INSERT_PHONE_NUMBER_MOBILE;
				}
				cursor.close();
			}

			// check phone work
			if (numberWork != null) {
				cursor = getContentResolver().query(
						ContactsContract.Data.CONTENT_URI
						, null // projection
						, ContactsContract.Data.MIMETYPE+"= ? AND "+
							ContactsContract.CommonDataKinds.Phone.CONTACT_ID+"= ? AND "+
							ContactsContract.CommonDataKinds.Phone.TYPE+"=?" // selection
						, new String[] { // selectionArgs
							ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
							, String.valueOf(contactId)
							, String.valueOf(ContactsContract.CommonDataKinds.Phone.TYPE_WORK)
						}
						, null // sortOrder
				);
				if (cursor.moveToFirst()) {
					String curNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
					if (!curNumber.equals(numberWork)) {
						r |= ImportContactOperations.UPDATE_PHONE_NUMBER_WORK;
					}
				} else {
					r |= ImportContactOperations.INSERT_PHONE_NUMBER_WORK;
				}
				cursor.close();
			}

			// check phone home
			if (numberHome != null) {
				cursor = getContentResolver().query(
						ContactsContract.Data.CONTENT_URI
						, null // projection
						, ContactsContract.Data.MIMETYPE+"= ? AND "+
							ContactsContract.CommonDataKinds.Phone.CONTACT_ID+"= ? AND "+
							ContactsContract.CommonDataKinds.Phone.TYPE+"=?" // selection
						, new String[] { // selectionArgs
							ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
							, String.valueOf(contactId)
							, String.valueOf(ContactsContract.CommonDataKinds.Phone.TYPE_HOME)
						}
						, null // sortOrder
				);
				if (cursor.moveToFirst()) {
					String curNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
					if (!curNumber.equals(numberHome)) {
						r |= ImportContactOperations.UPDATE_PHONE_NUMBER_HOME;
					}
				} else {
					r |= ImportContactOperations.INSERT_PHONE_NUMBER_HOME;
				}
				cursor.close();
			}

			// check email address
			cursor = getContentResolver().query(
					ContactsContract.Data.CONTENT_URI
					, null // projection
					, ContactsContract.Data.MIMETYPE+"= ? AND "+
						ContactsContract.CommonDataKinds.Email.CONTACT_ID+"= ? AND "+
						ContactsContract.CommonDataKinds.Email.TYPE+"=?" // selection
					, new String[] { // selectionArgs
						ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE
						, String.valueOf(contactId)
						, String.valueOf(ContactsContract.CommonDataKinds.Email.TYPE_WORK)
					}
					, null // sortOrder
			);
			if (cursor.moveToFirst()) {
				String curAddress = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
				if (!curAddress.equals(emailAddress)) {
					r |= ImportContactOperations.UPDATE_EMAIL_ADDRESS;
				}
			}
			cursor.close();

			// check inovex (skills)
			// get photoMD5
			String curPhotoMD5 = null;
			cursor = getContentResolver().query(
					ContactsContract.Data.CONTENT_URI
					, null // projection
					, ContactsContract.Data.MIMETYPE+"= ? AND "+
						ContactsContract.Data.CONTACT_ID+"= ?"
					, new String[] { // selectionArgs
						ExtraDataKinds.Inovex.CONTENT_ITEM_TYPE
						, String.valueOf(contactId)
					}
					, null // sortOrder
			);
			if (cursor.moveToFirst()) {
				String curSkills = cursor.getString(cursor.getColumnIndex(ExtraDataKinds.Inovex.SKILLS));
				curPhotoMD5 = cursor.getString(cursor.getColumnIndex(ExtraDataKinds.Inovex.PHOTO_MD5));
				if (
						!curSkills.equals(skills)
						|| (photoMD5 != null && !photoMD5.equals(curPhotoMD5))
				) {
					r |= ImportContactOperations.UPDATE_INOVEX;
				}
			}
			cursor.close();

			// check for photo
			if (photoMD5 != null && !photoMD5.equals(curPhotoMD5)) {
				cursor = getContentResolver().query(
						ContactsContract.Data.CONTENT_URI
						, null // projection
						, ContactsContract.Data.MIMETYPE+"= ? AND "+ContactsContract.CommonDataKinds.Photo.CONTACT_ID+"= ?" // selection
						, new String[] { // selectionArgs
							ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
							, String.valueOf(contactId)
						}
						, null // sortOrder
				);
				if (cursor.moveToFirst()) {
					r |= ImportContactOperations.UPDATE_PHOTO;
				} else {
					r |= ImportContactOperations.INSERT_PHOTO;
				}
				cursor.close();
			}

			return r;
		} else {
			// alles insert
			int r = ImportContactOperations.INSERT_RAW_CONTACT;
			if (photoMD5 != null) {
				r |= ImportContactOperations.INSERT_PHOTO;
			}
			if (numberMobile != null) {
				r |= ImportContactOperations.INSERT_PHONE_NUMBER_MOBILE;
			}
			if (numberWork != null) {
				r |= ImportContactOperations.INSERT_PHONE_NUMBER_WORK;
			}
			if (numberHome != null) {
				r |= ImportContactOperations.INSERT_PHONE_NUMBER_HOME;
			}
			return r;
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
					, (String) contact.get("numberMobile")
					, (String) contact.get("numberWork")
					, (String) contact.get("numberHome")
					, (String) contact.get("emailAddress")
					, (String) contact.get("skills")
			);
		}
	}

	private void insertUpdateContact(String givenName, String familyName, String symbol, String lob, String location, String photoMD5, String numberMobile, String numberWork, String numberHome, String emailAddress, String skills) {
		// bestehenden finden
		ContentResolver cr = getContentResolver();
		Cursor cursor = cr.query(
				ContactsContract.Data.CONTENT_URI
				, null // projection
				, ContactsContract.Data.MIMETYPE+"= ? AND "+ContactsContract.CommonDataKinds.Organization.SYMBOL+"=?" // selection
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
		operations = checkImportOperations(cursor, givenName, familyName, symbol, lob, location, photoMD5, numberMobile, numberWork, numberHome, emailAddress, skills);

		if (operations == 0) return;
		Log.i(TAG, "operations="+operations);

		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

		// raw contact
		if ((operations & ImportContactOperations.INSERT_RAW_CONTACT) > 0) {
			// account ermitteln
			Account[] accs = AccountManager.get(this).getAccounts();
			String name, type;
			if (accs.length == 0) {
				name = type = null;
			} else {
				// 1. account nehmen
				name = accs[0].name;
				type = accs[0].type;
			}

			ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
				.withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, type)
				.withValue(ContactsContract.RawContacts.ACCOUNT_NAME, name)
				.build());
		}

		// display given family name
		String displayName = givenName+' '+familyName;
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

		// phone number mobile
		if ((operations & (ImportContactOperations.INSERT_PHONE_NUMBER_MOBILE | ImportContactOperations.UPDATE_PHONE_NUMBER_MOBILE)) > 0) {
			Builder builder;
			if ((operations & ImportContactOperations.INSERT_PHONE_NUMBER_MOBILE) > 0) {
				builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
					.withValue(ContactsContract.Data.MIMETYPE,
						ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
					.withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
				if ((operations & ImportContactOperations.INSERT_RAW_CONTACT) > 0) {
					builder.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0);
				} else {
					builder.withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
				}
			} else {
				builder = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI);
				builder.withSelection(
						ContactsContract.Data.RAW_CONTACT_ID+"=? AND "+
						ContactsContract.Data.MIMETYPE+"=? AND "+
						ContactsContract.CommonDataKinds.Phone.TYPE+"=?"
						, new String[] {
								String.valueOf(rawContactId)
								, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
								, String.valueOf(ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
						}
				);
			}
			ops.add(builder
					.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, numberMobile)
					.build());
		}

		// phone number work
		if ((operations & (ImportContactOperations.INSERT_PHONE_NUMBER_WORK | ImportContactOperations.UPDATE_PHONE_NUMBER_WORK)) > 0) {
			Builder builder;
			if ((operations & ImportContactOperations.INSERT_PHONE_NUMBER_WORK) > 0) {
				builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
					.withValue(ContactsContract.Data.MIMETYPE,
						ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
					.withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_WORK);
				if ((operations & ImportContactOperations.INSERT_RAW_CONTACT) > 0) {
					builder.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0);
				} else {
					builder.withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
				}
			} else {
				builder = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI);
				builder.withSelection(
						ContactsContract.Data.RAW_CONTACT_ID+"=? AND "+
						ContactsContract.Data.MIMETYPE+"=? AND "+
						ContactsContract.CommonDataKinds.Phone.TYPE+"=?"
						, new String[] {
								String.valueOf(rawContactId)
								, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
								, String.valueOf(ContactsContract.CommonDataKinds.Phone.TYPE_WORK)
						}
				);
			}
			ops.add(builder
					.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, numberWork)
					.build());
		}

		// phone number home
		if ((operations & (ImportContactOperations.INSERT_PHONE_NUMBER_HOME | ImportContactOperations.UPDATE_PHONE_NUMBER_HOME)) > 0) {
			Builder builder;
			if ((operations & ImportContactOperations.INSERT_PHONE_NUMBER_HOME) > 0) {
				builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
					.withValue(ContactsContract.Data.MIMETYPE,
						ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
					.withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_HOME);
				if ((operations & ImportContactOperations.INSERT_RAW_CONTACT) > 0) {
					builder.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0);
				} else {
					builder.withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
				}
			} else {
				builder = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI);
				builder.withSelection(
						ContactsContract.Data.RAW_CONTACT_ID+"=? AND "+
						ContactsContract.Data.MIMETYPE+"=? AND "+
						ContactsContract.CommonDataKinds.Phone.TYPE+"=?"
						, new String[] {
								String.valueOf(rawContactId)
								, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
								, String.valueOf(ContactsContract.CommonDataKinds.Phone.TYPE_HOME)
						}
				);
			}
			ops.add(builder
					.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, numberHome)
					.build());
		}

		// email address
		if ((operations & (ImportContactOperations.INSERT_RAW_CONTACT | ImportContactOperations.UPDATE_EMAIL_ADDRESS)) > 0) {
			Builder builder;
			if ((operations & ImportContactOperations.INSERT_RAW_CONTACT) > 0) {
				builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
					.withValue(ContactsContract.Data.MIMETYPE,
						ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
					.withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
					.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0);
			} else {
				builder = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI);
				builder.withSelection(
						ContactsContract.Data.RAW_CONTACT_ID+"=? AND "+
						ContactsContract.Data.MIMETYPE+"=? AND "+
						ContactsContract.CommonDataKinds.Email.TYPE+"=?"
						, new String[] {
								String.valueOf(rawContactId)
								, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE
								, String.valueOf(ContactsContract.CommonDataKinds.Email.TYPE_WORK)
						}
				);
			}
			ops.add(builder
					.withValue(ContactsContract.CommonDataKinds.Email.DATA, emailAddress)
					.build());
		}

		// inovex extra (skills), photoMD5
		if ((operations & (ImportContactOperations.INSERT_RAW_CONTACT | ImportContactOperations.UPDATE_INOVEX)) > 0) {
			Builder builder;
			if ((operations & ImportContactOperations.INSERT_RAW_CONTACT) > 0) {
				builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
					.withValue(ContactsContract.Data.MIMETYPE,
						ExtraDataKinds.Inovex.CONTENT_ITEM_TYPE)
					.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0);
			} else {
				builder = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI);
				builder.withSelection(
						ContactsContract.Data.RAW_CONTACT_ID+"=? AND "+
						ContactsContract.Data.MIMETYPE+"=?"
						, new String[] {
								String.valueOf(rawContactId)
								, ExtraDataKinds.Inovex.CONTENT_ITEM_TYPE
						}
				);
			}
			ops.add(builder
					.withValue(ExtraDataKinds.Inovex.SKILLS, skills)
					.withValue(ExtraDataKinds.Inovex.PHOTO_MD5, photoMD5)
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

