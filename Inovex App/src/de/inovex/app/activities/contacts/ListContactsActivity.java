package de.inovex.app.activities.contacts;

import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import de.inovex.app.R;
import de.inovex.app.adapter.ContactsAdapter;
import de.inovex.app.provider.contact_contracts.ExtraDataKinds;
import de.inovex.app.service.ContactsService;

public class ListContactsActivity extends Activity {
	private static final String TAG = "ListContactsActivity";
	private ListView listContacts;

	private void importContacts() {
		// nicht jedes mal importieren
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		long lastImport = prefs.getLong("lastImport", 0);
		long now = new Date().getTime();
		if (now - lastImport > 24*60*60*1000) { // 24h cache
			Intent serviceIntent = new Intent(this, ContactsService.class);
			startService(serviceIntent);
			// import contact photos
			serviceIntent.putExtra("action", ContactsService.ACTION_IMPORT_CONTACT_PHOTOS);
			startService(serviceIntent);

			Editor editor = prefs.edit();
			editor.putLong("lastImport", now);
			editor.commit();
		}
	}

	private void initList() {
		listContacts = (ListView) findViewById(R.id.list_contacts);
		listContacts.setFastScrollEnabled(true);
		listContacts.setAdapter(new ContactsAdapter(this, R.layout.contacts_item, null));
		listContacts.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int arg2, long arg3) {
				Intent intent = new Intent(getBaseContext(), ViewContactActivity.class);
				intent.putExtra("contactId", (Integer) view.getTag());
				intent.putExtra("displayName", ((TextView) view.findViewById(R.id.contacts_item_title)).getText());
				startActivity(intent);
			}
		});
	}

	private void loadContacts(String filter) {
		String selection;
		String[] selectionArgs;
		if (filter == null) {
			selection = ContactsContract.Data.MIMETYPE+"=?";
			selectionArgs = new String[] { // selectionArgs
				ExtraDataKinds.Inovex.CONTENT_ITEM_TYPE
			};
		} else {
			selection = ContactsContract.Data.MIMETYPE+"= ? AND "+
				ContactsContract.Data.DISPLAY_NAME+" LIKE ?";
			selectionArgs = new String[] { // selectionArgs
				ExtraDataKinds.Inovex.CONTENT_ITEM_TYPE
				, "%"+filter+"%"
			};
		}
		Cursor cursor = getContentResolver().query(
				ContactsContract.Data.CONTENT_URI
				, null // projection
				, selection
				, selectionArgs
				, ContactsContract.Data.DISPLAY_NAME // sortOrder
		);
		cursor.setNotificationUri(getContentResolver(), ExtraDataKinds.Inovex.NOTIFICATION_URI);
		startManagingCursor(cursor);

		/*while (cursor.moveToNext()) {
			Log.i(TAG, "---------------- entry ---------------");
			Log.i(TAG, "DisplayName:         "+cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME)));
			Log.i(TAG, "--------------------------------------");
		}

		cursor.moveToFirst();
		*/

		((CursorAdapter) listContacts.getAdapter()).changeCursor(cursor);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contacts);

		initList();

		// import contacts
		importContacts();

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
