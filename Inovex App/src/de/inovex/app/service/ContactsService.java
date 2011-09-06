package de.inovex.app.service;

import java.util.ArrayList;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Handler;
import android.provider.ContactsContract;
import android.widget.Toast;

public class ContactsService extends IntentService {
	private static final String TAG = "ContactsService";
	private Handler handler;

	public ContactsService() {
		super(TAG);
	}

	private void insertNewContact(String email, String accountName, String displayName, String phoneNumber){
		ContentResolver cr = getContentResolver();

		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
			.withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, email)
			.withValue(ContactsContract.RawContacts.ACCOUNT_NAME, accountName)
			.build());
		ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
			.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
			.withValue(ContactsContract.Data.MIMETYPE,
					ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
			.withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, displayName)
			.build());
		ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
			.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
			.withValue(ContactsContract.Data.MIMETYPE,
					ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
			.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber)
			.withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_HOME)
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
		insertNewContact(null, null, "neu neu22", "124");
		getContentResolver().notifyChange(ContactsContract.Contacts.CONTENT_URI, null);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		handler = new Handler();
		return super.onStartCommand(intent, flags, startId);
	}
}

