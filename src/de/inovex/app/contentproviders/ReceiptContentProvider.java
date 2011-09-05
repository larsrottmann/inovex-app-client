package de.inovex.app.contentproviders;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class ReceiptContentProvider extends ContentProvider {

	private final static String TAG = ReceiptContentProvider.class.toString();
	private static final String AUTHORITY = "de.inovex.app";

	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/journeys");

	public static class Columns {

		public static final String ID = "_id";
		public static final String PARENT_ID = "parent_id";
		public static final String CREATED = "created";
		public static final String DESCRIPTION = "description";
		public static final String START_LOCATION = "start_location";
		public static final String DESTINATION = "destination";
		public static final String TYPE = "type";
	}

	private static final UriMatcher sUriMatcher;
	private static final int JOURNEY = 0;
	private static final int JOURNEYS = 1;

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(AUTHORITY, "journeys/#", JOURNEY);
		sUriMatcher.addURI(AUTHORITY, "journeys", JOURNEYS);
	}

	private static class DBHelper extends SQLiteOpenHelper {

		private static final String TABLE_NAME = "journeys";
		private static final int DATABASE_VERSION = 0;

		private static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " (" + 				
				Columns.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
				Columns.PARENT_ID + " INTEGER DEFAULT -1, " + 
				Columns.CREATED + " INTEGER, " + 
				Columns.START_LOCATION + " TEXT, "+ 
				Columns.DESTINATION + " TEXT, " + 
				Columns.DESCRIPTION + " TEXT, " + 
				Columns.TYPE + " INTEGER );";

		private static final String DATABASE_NAME = "inovex_app";

		DBHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(TABLE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
			onCreate(db);
		}

	}

	private DBHelper dbHelper;

	@Override
	public boolean onCreate() {
		dbHelper = new DBHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(DBHelper.TABLE_NAME);
		String limit = null;

		switch (sUriMatcher.match(uri)) {
		case JOURNEY:
			String id = uri.getLastPathSegment();
			selection = Columns.ID + "=?";
			selectionArgs = new String[] { id };
			break;
		case JOURNEYS:
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// If no sort order is specified use the default
		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = "created DESC";
		} else {
			orderBy = sortOrder;
		}

		// Get the database and run the query
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs,
				Columns.ID, null, orderBy, limit);

		// Tell the cursor what uri to watch, so it knows when its source data
		// changes
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case JOURNEY:
			return "vnd.android.cursor.dir/vnd.ocr.document";
		case JOURNEYS:
			return "vnd.android.cursor.item/vnd.ocr.documents";
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {

		if (values != null) {
			values = new ContentValues(values);
		} else {
			values = new ContentValues();
		}

		Long now = Long.valueOf(System.currentTimeMillis());
		values.put(Columns.CREATED, now);

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		long rowId = db.insert(DBHelper.TABLE_NAME, null, values);
		if (rowId > 0) {
			Uri entryUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
			getContext().getContentResolver().notifyChange(CONTENT_URI, null);
			return entryUri;
		} else {
			throw new SQLException("Failed to insert row into " + uri);
		}
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		int count;
		switch (sUriMatcher.match(uri)) {
		case JOURNEYS:
			count = db.delete(DBHelper.TABLE_NAME, selection, selectionArgs);
			break;
		case JOURNEY:
			String id = uri.getLastPathSegment();
			count = db.delete(DBHelper.TABLE_NAME, Columns.ID + "=?",
					new String[] { id });
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		getContext().getContentResolver().notifyChange(CONTENT_URI, null);
		return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		int count = 0;
		switch (sUriMatcher.match(uri)) {
		case JOURNEYS:
			count = db.update(DBHelper.TABLE_NAME, values, selection,
					selectionArgs);
			break;

		case JOURNEY: {
			String id = uri.getLastPathSegment();
			count = db.update(DBHelper.TABLE_NAME, values, Columns.ID + "=?",
					new String[] { id });
			break;
		}
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

}
