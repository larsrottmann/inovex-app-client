package de.inovex.app.provider.contact_contracts;

import android.net.Uri;

/**
 * extra contact fields
 */
public final class ExtraDataKinds {
	public final static class Inovex {
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/inovex";

		/**
		 * TYPE: string
		 */
		public static final String SKILLS = "data1";

		/**
		 * TYPE: string
		 */
		public static final String PHOTO_URL = "data2";

		public static final String PHOTO_CHANGED = "data3";

		public static final Uri NOTIFICATION_URI = Uri.parse("content://inovex.contacts/");
	}

	private ExtraDataKinds() {}
}
