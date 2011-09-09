package de.inovex.app.provider.contact_contracts;

/**
 * extra contact fields
 */
public final class ExtraDataKinds {
	public final class Inovex {
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/inovex";

		/**
		 * TYPE: string
		 */
		public static final String SKILLS = "data1";

		/**
		 * TYPE: string
		 */
		public static final String PHOTO_MD5 = "data2";
	}

	private ExtraDataKinds() {}
}
