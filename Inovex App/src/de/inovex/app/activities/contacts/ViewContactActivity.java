package de.inovex.app.activities.contacts;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;
import de.inovex.app.R;
import de.inovex.app.provider.contact_contracts.ExtraDataKinds;

public class ViewContactActivity extends Activity {
	private int contactId;
	private ResourceCursorAdapter listAdapter;

	private void initList() {
		ListView list = (ListView) findViewById(R.id.view_contact_list);
		list.setAdapter(listAdapter = new ResourceCursorAdapter(this, R.layout.view_contact_item, null) {
			@Override
			public void bindView(View view, Context context, Cursor cursor) {
				TextView tvTitle = (TextView) view.findViewById(R.id.view_contact_item_title);
				TextView tvDetails = (TextView) view.findViewById(R.id.view_contact_item_details);

				View btnCall = view.findViewById(R.id.view_contact_item_call);
				View btnSms = view.findViewById(R.id.view_contact_item_sms);
				View btnEmail = view.findViewById(R.id.view_contact_item_email);

				btnCall.setVisibility(View.GONE);
				btnSms.setVisibility(View.GONE);
				btnEmail.setVisibility(View.GONE);

				String mimetype = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.MIMETYPE));
				if (mimetype.equals(ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)) {
					// Organization
					tvTitle.setText(R.string.organization);
					tvDetails.setText(
							cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.DEPARTMENT))+", "
							+ cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.OFFICE_LOCATION))
					);
				} else if (mimetype.equals(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)) {
					// Phone
					final String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

					btnCall.setVisibility(View.VISIBLE);
					btnCall.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							Intent intent = new Intent(Intent.ACTION_CALL);
							intent.setData(Uri.parse("tel:"+number));
							startActivity(intent);
						}
					});
					btnSms.setVisibility(View.VISIBLE);
					btnSms.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							Intent sendIntent = new Intent(Intent.ACTION_VIEW);
							sendIntent.setData(Uri.parse("sms:"+number));
							startActivity(sendIntent);
						}
					});

					String type;
					switch (cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE))) {
					case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
						type = getResources().getString(R.string.mobile);
						break;
					case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
						type = getResources().getString(R.string.work);
						break;
					case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
						type = getResources().getString(R.string.home);
						break;
					default:
						type = "";
					}
					tvTitle.setText(getResources().getString(R.string.call_type, type));
					tvDetails.setText(number);
				} else if (mimetype.equals(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)) {
					// Email
					final String emailAddress = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));

					btnEmail.setVisibility(View.VISIBLE);
					btnEmail.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							Intent emailIntent = new Intent(Intent.ACTION_SEND);
							emailIntent.setType("message/rfc822");
							emailIntent.putExtra(Intent.EXTRA_EMAIL, emailAddress);
							startActivity(Intent.createChooser(emailIntent, getResources().getString(R.string.select_email_app)));
						}
					});

					String type;
					switch (cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE))) {
					case ContactsContract.CommonDataKinds.Email.TYPE_WORK:
						type = getResources().getString(R.string.work);
						break;
					case ContactsContract.CommonDataKinds.Email.TYPE_HOME:
						type = getResources().getString(R.string.home);
						break;
					default:
						type = "";
					}
					tvTitle.setText(getResources().getString(R.string.email_type, type));
					tvDetails.setText(emailAddress);
				} else if (mimetype.equals(ExtraDataKinds.Inovex.CONTENT_ITEM_TYPE)) {
					// Inovex
					tvTitle.setText(R.string.skills);
					tvDetails.setText(cursor.getString(cursor.getColumnIndex(ExtraDataKinds.Inovex.SKILLS)));
				}
			}
		});
	}

	/**
	 * photo + display name
	 */
	private void loadHeader(String displayName) {
		// displayName
		TextView tv = (TextView) findViewById(R.id.view_content_title);
		tv.setText(displayName);

		// photo
		Cursor cur = getContentResolver().query(
				ContactsContract.Data.CONTENT_URI
				, null // projection
				, ContactsContract.Data.MIMETYPE+"= ? AND "+ContactsContract.Data.CONTACT_ID+"= ?" // selection
				, new String[] { // selectionArgs
					ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
					, String.valueOf(contactId)
				}
				, null // sortOrder
		);
		ImageView imgView = (ImageView) findViewById(R.id.view_contact_image);
		if (cur.moveToFirst()) {
			byte[] data = cur.getBlob(cur.getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO));
			if (data != null && data.length > 0) {
				imgView.setImageBitmap(BitmapFactory.decodeByteArray(data, 0, data.length));
			} else {
				imgView.setImageResource(R.drawable.ic_contact_picture);
			}
		} else {
			imgView.setImageResource(R.drawable.ic_contact_picture);
		}
		cur.close();
	}

	private void loadListItems() {
		Cursor cursor = getContentResolver().query(
				ContactsContract.Data.CONTENT_URI
				, null // projection
				, "("+
					ContactsContract.Data.MIMETYPE+"=? OR " +
					ContactsContract.Data.MIMETYPE+"=? OR " +
					ContactsContract.Data.MIMETYPE+"=? OR " +
					ContactsContract.Data.MIMETYPE+"=?" +
						") AND "+
					ContactsContract.Data.CONTACT_ID+"=?"
				, new String[] { // selectionArgs
						ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE
						, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
						, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE
						, ExtraDataKinds.Inovex.CONTENT_ITEM_TYPE
						, String.valueOf(contactId)
				}
				, null // sortOrder
		);
		startManagingCursor(cursor);
		listAdapter.changeCursor(cursor);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_contact);

		contactId = getIntent().getIntExtra("contactId", -1);
		String displayName = getIntent().getStringExtra("displayName");
		if (contactId == -1) {
			finish();
			return;
		}

		loadHeader(displayName);

		initList();
		loadListItems();
	}
}
