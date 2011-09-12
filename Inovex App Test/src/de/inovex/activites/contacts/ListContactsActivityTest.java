package de.inovex.activites.contacts;

import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import de.inovex.app.R;
import de.inovex.app.activities.contacts.ListContactsActivity;

public class ListContactsActivityTest extends ActivityInstrumentationTestCase2<ListContactsActivity> {
	private ListContactsActivity mActivity;

	public ListContactsActivityTest() {
		super("de.inovex.app.activites.contacts", ListContactsActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		mActivity = getActivity();

	}

	public void testAllContacts() {
		ListView contacts = (ListView) mActivity.findViewById(R.id.list_contacts);
		assertEquals(62, contacts.getCount());

		// item
		TextView tv = (TextView) contacts.getChildAt(0).findViewById(R.id.contacts_item_title);
		assertEquals("Alexander Gabert", tv.getText());
		tv = (TextView) contacts.getChildAt(0).findViewById(R.id.contacts_item_details);
		assertEquals("BI, Pforzheim", tv.getText());

		View view = mActivity.findViewById(R.id.contacts_search_label);
		assertEquals(View.GONE, view.getVisibility());
		view = mActivity.findViewById(R.id.contacts_show_all);
		assertEquals(View.GONE, view.getVisibility());
	}

	public void testSearch() throws InterruptedException {
		// suche button

		// text eingeben
		synchronized (this) {
			Thread.sleep(5000);
		}
		EditText ed = (EditText) mActivity.getCurrentFocus();
		ed.setText("geh");
		synchronized (this) {
			Thread.sleep(5000);
		}
		// list item prüfen
		// search label
		// search button
	}
}
