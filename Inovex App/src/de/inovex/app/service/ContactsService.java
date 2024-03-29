package de.inovex.app.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.xml.sax.SAXException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;
import de.inovex.app.provider.contact_contracts.ExtraDataKinds;
import de.inovex.app.service.InovexPortalAPI.Employee;

public class ContactsService extends IntentService {
	static private class ImportContactOperations {
		static final int INSERT_RAW_CONTACT = 			1;
		static final int UPDATE_STRUCTURED_NAME = 		2;
		static final int UPDATE_ORGANIZATION = 			4;
		static final int INSERT_PHONE_NUMBER_MOBILE = 	32;
		static final int UPDATE_PHONE_NUMBER_MOBILE = 	64;
		static final int INSERT_PHONE_NUMBER_WORK = 	128;
		static final int UPDATE_PHONE_NUMBER_WORK = 	256;
		static final int INSERT_PHONE_NUMBER_HOME = 	512;
		static final int UPDATE_PHONE_NUMBER_HOME = 	1024;
		static final int UPDATE_EMAIL_ADDRESS = 		2048;
		static final int UPDATE_INOVEX = 				4096;
		static final int UPDATE_INOVEX_PHOTO_URL = 		8192;
		static final int INSERT_INOVEX = 				16384;
	}

	static private final int ACTION_IMPORT_CONTACTS = 1;
	static public final int ACTION_IMPORT_CONTACT_PHOTOS = 2;

	static private final String TAG = "ContactsService";

	private Handler handler;
	private InovexPortalAPI api;

	public ContactsService() {
		super(TAG);
	}

	private int checkImportOperations(Cursor cursorOrganization, Employee contact) {
		if (cursorOrganization.moveToFirst()) {
			int r = 0;
			int contactId = cursorOrganization.getInt(cursorOrganization.getColumnIndex(ContactsContract.CommonDataKinds.Organization.CONTACT_ID));

			// check lob symbol location
			String curSymbol = cursorOrganization.getString(cursorOrganization.getColumnIndex(ContactsContract.CommonDataKinds.Organization.SYMBOL));
			String curLob = cursorOrganization.getString(cursorOrganization.getColumnIndex(ContactsContract.CommonDataKinds.Organization.DEPARTMENT));
			String curLocation = cursorOrganization.getString(cursorOrganization.getColumnIndex(ContactsContract.CommonDataKinds.Organization.OFFICE_LOCATION));
			String curCompany = cursorOrganization.getString(cursorOrganization.getColumnIndex(ContactsContract.CommonDataKinds.Organization.COMPANY));
			if (
					curSymbol == null || !curSymbol.equals(contact.symbol)
					|| curLob == null || !curLob.equals(contact.lob)
					|| curLocation == null || !curLocation.equals(contact.location)
					|| curCompany == null || !curCompany.equals("inovex GmbH")
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
						curGivenName == null || !curGivenName.equals(contact.givenName)
						|| curFamilyName == null || !curFamilyName.equals(contact.familyName)
						|| curDisplayName == null || !curDisplayName.equals(contact.givenName+' '+contact.familyName)
				) {
					r |= ImportContactOperations.UPDATE_STRUCTURED_NAME;
				}
			}
			cursor.close();

			// check phone mobile
			if (contact.numberMobile != null) {
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
					if (curNumber == null || !curNumber.equals(contact.numberMobile)) {
						r |= ImportContactOperations.UPDATE_PHONE_NUMBER_MOBILE;
					}
				} else {
					r |= ImportContactOperations.INSERT_PHONE_NUMBER_MOBILE;
				}
				cursor.close();
			}

			// check phone work
			if (contact.numberWork != null) {
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
					if (curNumber == null || !curNumber.equals(contact.numberWork)) {
						r |= ImportContactOperations.UPDATE_PHONE_NUMBER_WORK;
					}
				} else {
					r |= ImportContactOperations.INSERT_PHONE_NUMBER_WORK;
				}
				cursor.close();
			}

			// check phone home
			if (contact.numberHome != null) {
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
					if (curNumber == null || !curNumber.equals(contact.numberHome)) {
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
				if (curAddress == null || !curAddress.equals(contact.emailAddress)) {
					r |= ImportContactOperations.UPDATE_EMAIL_ADDRESS;
				}
			}
			cursor.close();

			// check inovex (skills)
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
				String curPhotoUrl = cursor.getString(cursor.getColumnIndex(ExtraDataKinds.Inovex.PHOTO_URL));
				if (
						curSkills == null
						|| !curSkills.equals(contact.skills)
				) {
					r |= ImportContactOperations.UPDATE_INOVEX;
				}
				if (contact.photoUrl != null && !contact.photoUrl.equals(curPhotoUrl)) {
					r |= ImportContactOperations.UPDATE_INOVEX|ImportContactOperations.UPDATE_INOVEX_PHOTO_URL;
				}
			} else {
				r |= ImportContactOperations.INSERT_INOVEX;
			}
			cursor.close();

			return r;
		} else {
			// alles insert
			int r = ImportContactOperations.INSERT_RAW_CONTACT|ImportContactOperations.INSERT_INOVEX;
			if (contact.photoUrl != null) {
				r |= ImportContactOperations.UPDATE_INOVEX_PHOTO_URL;
			}
			if (contact.numberMobile != null) {
				r |= ImportContactOperations.INSERT_PHONE_NUMBER_MOBILE;
			}
			if (contact.numberWork != null) {
				r |= ImportContactOperations.INSERT_PHONE_NUMBER_WORK;
			}
			if (contact.numberHome != null) {
				r |= ImportContactOperations.INSERT_PHONE_NUMBER_HOME;
			}
			return r;
		}
	}

	private byte[] compressBitmap(Bitmap bmp) throws IOException {
		Matrix matrix = new Matrix();

		int width = bmp.getWidth();
		int height = bmp.getHeight();
		int newHeight = 80;
		float scale = ((float) newHeight) / height;

		matrix.postScale(scale, scale);
		Bitmap resizedBitmap = Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true);

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		resizedBitmap.compress(CompressFormat.JPEG, 50, os);
		byte[] r = os.toByteArray();
		os.close();
		return r;
	}

	private void importContactPhoto() {
		Log.i(TAG, "start import contact photos");

		// alle inovex-data holen wo photo changed
		Cursor cursor = getContentResolver().query(
				ContactsContract.Data.CONTENT_URI
				, null // projection
				, ContactsContract.Data.MIMETYPE+"= ? AND "+
					ExtraDataKinds.Inovex.PHOTO_CHANGED+"= ?"
				, new String[] { // selectionArgs
					ExtraDataKinds.Inovex.CONTENT_ITEM_TYPE
					, "1"
				}
				, null // sortOrder
		);

		while (cursor.moveToNext()) {
			int rawContactId = cursor.getInt(cursor.getColumnIndex(ContactsContract.Data.CONTACT_ID));
			String photoUrl = cursor.getString(cursor.getColumnIndex(ExtraDataKinds.Inovex.PHOTO_URL));

			// inovex data updaten
			Builder builder = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI);
			builder.withSelection(
					ContactsContract.Data.RAW_CONTACT_ID+"=? AND "+ContactsContract.Data.MIMETYPE+"=?"
					, new String[] {
							String.valueOf(rawContactId)
							, ExtraDataKinds.Inovex.CONTENT_ITEM_TYPE
					}
			);

			ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
			ops.add(builder
					.withValue(ExtraDataKinds.Inovex.PHOTO_CHANGED, "0")
					.build());

			// photo insert/update
			// check for photo
			Cursor photoCursor = getContentResolver().query(
					ContactsContract.Data.CONTENT_URI
					, null // projection
					, ContactsContract.Data.MIMETYPE+"= ? AND "+ContactsContract.CommonDataKinds.Photo.CONTACT_ID+"= ?" // selection
					, new String[] { // selectionArgs
						ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
						, String.valueOf(rawContactId)
					}
					, null // sortOrder
			);

			if (!photoCursor.moveToFirst()) { // existiert noch nicht
				builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
					.withValue(ContactsContract.Data.MIMETYPE,
						ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
					.withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
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
			photoCursor.close();

			try {
				Log.i(TAG, "download photo: "+photoUrl);
				InputStream photoStream = api.downloadPhoto(photoUrl);

				// scale, compress
				Bitmap bmp = BitmapFactory.decodeStream(photoStream);
				if (bmp == null) {
					Log.e(TAG, "photo could not be decoded.");
				} else {
					byte[] photoData = compressBitmap(bmp);

					ops.add(builder
							.withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, photoData)
							.build());
				}
				getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		cursor.close();

		Log.i(TAG, "finished import contact photos");
	}

	private void importContacts() throws JsonParseException, JsonMappingException, IOException, IllegalStateException, SAXException, ParserConfigurationException {
		ContentResolver cr = getContentResolver();
		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

		for (Employee contact : api.getAllEmployees()) {
			boolean insert = insertUpdateContact(cr, ops, contact);

			// bei insert, sofort transaction committen
			if (insert) {
				try {
					cr.applyBatch(ContactsContract.AUTHORITY, ops);
				} catch (Exception e) {
					e.printStackTrace();
				}
				ops.clear();
			}
		}
		// zum schluss go
		if (! ops.isEmpty()) {
			try {
				cr.applyBatch(ContactsContract.AUTHORITY, ops);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private boolean insertUpdateContact(ContentResolver cr, ArrayList<ContentProviderOperation> ops, Employee contact) {
		// bestehenden finden
		Cursor cursor = cr.query(
				ContactsContract.Data.CONTENT_URI
				, null // projection
				, ContactsContract.Data.MIMETYPE+"= ? AND "+ContactsContract.CommonDataKinds.Organization.SYMBOL+"=?" // selection
				, new String[] { // selectionArgs
					ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE
					, contact.symbol
				}
				, null // sortOrder
		);
		int rawContactId=0;
		int operations;
		if (cursor.moveToFirst()) {
			rawContactId = cursor.getInt(cursor.getColumnIndex(ContactsContract.Data.RAW_CONTACT_ID));
		}
		operations = checkImportOperations(cursor, contact);

		cursor.close();
		if (operations == 0) return false;
		Log.i(TAG, "operations="+operations+" on "+rawContactId);

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
		String displayName = contact.givenName+' '+contact.familyName;
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
				.withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, contact.givenName)
				.withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, contact.familyName);
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
					.withValue(ContactsContract.CommonDataKinds.Organization.DEPARTMENT, contact.lob)
					.withValue(ContactsContract.CommonDataKinds.Organization.SYMBOL, contact.symbol)
					.withValue(ContactsContract.CommonDataKinds.Organization.TYPE, ContactsContract.CommonDataKinds.Organization.TYPE_WORK)
					.withValue(ContactsContract.CommonDataKinds.Organization.OFFICE_LOCATION, contact.location)
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
								, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
								, String.valueOf(ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
						}
				);
			}
			ops.add(builder
					.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contact.numberMobile)
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
					.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contact.numberWork)
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
					.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contact.numberHome)
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
					.withValue(ContactsContract.CommonDataKinds.Email.DATA, contact.emailAddress)
					.build());
		}

		// inovex extra (skills), photoMD5
		if ((operations & (ImportContactOperations.INSERT_INOVEX | ImportContactOperations.UPDATE_INOVEX)) > 0) {
			Builder builder;
			if ((operations & ImportContactOperations.INSERT_INOVEX) > 0) {
				builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
					.withValue(ContactsContract.Data.MIMETYPE,
							ExtraDataKinds.Inovex.CONTENT_ITEM_TYPE);
				if ((operations & ImportContactOperations.INSERT_RAW_CONTACT) > 0) {
					builder.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0);
				} else {
					builder.withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
				}
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
			if ((operations & ImportContactOperations.UPDATE_INOVEX_PHOTO_URL) > 0) {
				builder.withValue(ExtraDataKinds.Inovex.PHOTO_CHANGED, "1");
			} else {
				builder.withValue(ExtraDataKinds.Inovex.PHOTO_CHANGED, "0");
			}
			ops.add(builder
					.withValue(ExtraDataKinds.Inovex.SKILLS, contact.skills)
					.withValue(ExtraDataKinds.Inovex.PHOTO_URL, contact.photoUrl)
					.build());
		}

		Log.i(TAG, "[ops] inserting/updating contact: "+displayName);
		return (operations|ImportContactOperations.INSERT_RAW_CONTACT)>0;//true => new contact
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String user = prefs.getString("username", "");
		String pwd = prefs.getString("password", "");
		api = new InovexPortalAPI(user, pwd);

		if (intent.getIntExtra("action", ACTION_IMPORT_CONTACTS) == ACTION_IMPORT_CONTACTS) {
			// import contacts
			handler.post(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(getApplicationContext(), "start importing all contacts", Toast.LENGTH_LONG).show();
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
		} else if (intent.getIntExtra("action", ACTION_IMPORT_CONTACTS) == ACTION_IMPORT_CONTACT_PHOTOS) {
			importContactPhoto();
		}

		getContentResolver().notifyChange(ExtraDataKinds.Inovex.NOTIFICATION_URI, null);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		handler = new Handler();
		return super.onStartCommand(intent, flags, startId);
	}
}

