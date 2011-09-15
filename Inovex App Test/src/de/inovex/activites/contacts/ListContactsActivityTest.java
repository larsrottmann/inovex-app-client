package de.inovex.activites.contacts;

import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.jayway.android.robotium.solo.Solo;

import de.inovex.app.R;
import de.inovex.app.activities.contacts.ListContactsActivity;

public class ListContactsActivityTest extends ActivityInstrumentationTestCase2<ListContactsActivity> {
	private ListContactsActivity mActivity;
	private Solo solo;

	public ListContactsActivityTest() {
		super("de.inovex.app", ListContactsActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		mActivity = getActivity();
		solo = new Solo(getInstrumentation(), mActivity);
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			// run finalize on solo will bring: inject event permission exception
			//solo.finalize();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		super.tearDown();
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

	public void testSearch() throws Throwable {
		runTestOnUiThread(new Runnable() {
			@Override
			public void run() {
				mActivity.onSearchRequested();
			}
		});
		getInstrumentation().waitForIdleSync();
		solo.sleep(2000);
		solo.enterText(0, "geh");
		solo.sendKey(Solo.ENTER);
		solo.sendKey(Solo.ENTER);
		getInstrumentation().waitForIdleSync();
		solo.sleep(2000);
		assertEquals("Search results for 'geh':", solo.getText(1).getText());

		ListView contacts = (ListView) mActivity.findViewById(R.id.list_contacts);
		assertEquals(2, contacts.getCount());

		// item
		TextView tv = (TextView) contacts.getChildAt(0).findViewById(R.id.contacts_item_title);
		assertEquals("Jan Gehring", tv.getText());
		tv = (TextView) contacts.getChildAt(0).findViewById(R.id.contacts_item_details);
		assertEquals("Operations, Pforzheim", tv.getText());

		View view = mActivity.findViewById(R.id.contacts_search_label);
		assertEquals(View.VISIBLE, view.getVisibility());
		view = mActivity.findViewById(R.id.contacts_show_all);
		assertEquals(View.VISIBLE, view.getVisibility());
	}
}
