package de.inovex.app.provider;

import java.util.ArrayList;
import java.util.Arrays;

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
import de.inovex.app.R;

public class InovexContentProvider extends ContentProvider {

	private final static String TAG = InovexContentProvider.class.toString();
	private static final String AUTHORITY = "de.inovex.app";

	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
	public static final Uri CONTENT_URI_JOURNEYS = Uri.parse("content://" + AUTHORITY+ "/journeys");
	public static final Uri CONTENT_URI_TIMES = Uri.parse("content://" + AUTHORITY+ "/times");
	public static final Uri CONTENT_URI_RECEIPTS = Uri.parse("content://" + AUTHORITY+ "/receipts");

	public static class Types {
		public static final String JOURNEY_START = "0";
		public static final String JOURNEY_END = "1";
		public static final String JOURNEY_CONTINUATION = "2";
		public static final String RECEIPT = "10";
		public static final String TIME = "20";
		
		public static CharSequence getDisplayStringFromType(Context context, String type){
			if (type.equals(JOURNEY_START)){
				return context.getText(R.string.arrival);
			} else if (type.equals(JOURNEY_END)){
				return context.getText(R.string.return_journey);				
			} else if (type.equals(JOURNEY_CONTINUATION)){
				return context.getText(R.string.continuation_of_journey);				
			} else if (type.equals(RECEIPT)){
				return context.getText(R.string.receipt);				
			} else if (type.equals(TIME)){
				return context.getText(R.string.work_time);
			}
			return null;
		}

	}
	

	public static class Columns {

		public static final String ID = "_id";
		public static final String PARENT_ID = "parent_id";
		public static final String CREATED = "created";
		public static final String END_DATE = "end_date";
		public static final String START_DATE = "start_date";
		public static final String DESCRIPTION = "description";
		public static final String START_LOCATION = "start_location";
		public static final String DESTINATION = "destination";
		public static final String IMAGE_PATH_URI = "image_uri";
		public static final String TYPE = "type";
		public static final String PROJECT = "project";
	}

	private static final UriMatcher sUriMatcher;
	private static final int JOURNEY = 0;
	private static final int JOURNEYS = 1;
	private static final int RECEIPT = 2;
	private static final int RECEIPTS = 3;
	private static final int TIME = 4;
	private static final int TIMES = 5;
	private static final int ANY = -1;

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(AUTHORITY, "journeys/#", JOURNEY);
		sUriMatcher.addURI(AUTHORITY, "journeys", JOURNEYS);
		sUriMatcher.addURI(AUTHORITY, "receipts/#", RECEIPT);
		sUriMatcher.addURI(AUTHORITY, "receipts", RECEIPTS);
		sUriMatcher.addURI(AUTHORITY, "times/#", TIME);
		sUriMatcher.addURI(AUTHORITY, "times", TIMES);
	}

	private static class DBHelper extends SQLiteOpenHelper {

		private static final String TABLE_NAME = "data";
		private static final int DATABASE_VERSION = 5;

		private static final String 
			TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " (" 
		
					+ Columns.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ Columns.PARENT_ID + " INTEGER DEFAULT -1, "
				    + Columns.CREATED + " INTEGER, "
				    + Columns.START_DATE + " INTEGER, "
					+ Columns.END_DATE + " INTEGER, "
				    + Columns.START_LOCATION + " TEXT, "
					+ Columns.DESTINATION + " TEXT, "
				    + Columns.DESCRIPTION + " TEXT, "
				    + Columns.IMAGE_PATH_URI + " TEXT, "
				    + Columns.PROJECT + " TEXT, "
				    + Columns.TYPE + " INTEGER );";

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
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
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
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(DBHelper.TABLE_NAME);
		String limit = null;
		if (selectionArgs==null){
			selectionArgs = new String[]{};
		}
		ArrayList<String> args = new ArrayList<String>(Arrays.asList(selectionArgs));
		int type = sUriMatcher.match(uri);

		switch (type) {
			case JOURNEY:
			case JOURNEYS:
			case RECEIPT:
			case RECEIPTS:
			case TIME:
			case TIMES:
				selection = modifySelectionForType(uri, selection, args, type);
				break;
			case ANY:
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

		Log.d(TAG, "DB query projection: " + Arrays.asList(projection) + ", selection: " + selection + ", selectionArgs: " + args +
				", orderBy: " + orderBy + ", limit: " + limit);
		// Get the database and run the query
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, args.toArray(new String[] {}), Columns.ID, null, orderBy, limit);

		// Tell the cursor what uri to watch, so it knows when its source data
		// changes
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case JOURNEY:
			return "vnd.android.cursor.dir/vnd.inovex.journey";
		case JOURNEYS:
			return "vnd.android.cursor.item/vnd.ocr.journeys";
		case RECEIPT:
			return "vnd.android.cursor.dir/vnd.inovex.receipt";
		case RECEIPTS:
			return "vnd.android.cursor.item/vnd.ocr.receipts";
		case TIME:
			return "vnd.android.cursor.dir/vnd.inovex.time";
		case TIMES:
			return "vnd.android.cursor.item/vnd.ocr.times";
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
		if (selectionArgs==null){
			selectionArgs = new String[]{};
		}
		ArrayList<String> args = new ArrayList<String>(Arrays.asList(selectionArgs));
		int count = 0;
		int type = sUriMatcher.match(uri);

		switch (type) {
		case RECEIPTS:
		case JOURNEYS:
		case TIMES:
		case RECEIPT:
		case JOURNEY:
		case TIME:
			selection = modifySelectionForType(uri, selection, args, type);
			break;
		case ANY:
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		count = db.delete(DBHelper.TABLE_NAME, selection, args.toArray(new String[] {}));

		getContext().getContentResolver().notifyChange(uri, null);
		getContext().getContentResolver().notifyChange(CONTENT_URI, null);
		return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		if (selectionArgs==null){
			selectionArgs = new String[]{};
		}
		ArrayList<String> args = new ArrayList<String>(Arrays.asList(selectionArgs));
		int count = 0;
		int type = sUriMatcher.match(uri);
		switch (type) {
		case RECEIPTS:
		case JOURNEYS:
		case TIMES:
		case RECEIPT:
		case JOURNEY:
		case TIME:
			selection = modifySelectionForType(uri, selection, args, type);
			break;
		case ANY:
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		count = db.update(DBHelper.TABLE_NAME, values, selection, args.toArray(new String[] {}));
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	private String modifySelectionForType(Uri uri, String orgSelection, ArrayList<String> list, int type) {
		String selection = "";
		String id = "";
		switch (type) {
		case RECEIPTS:
			if (orgSelection == null) {
				selection = Columns.TYPE + " =?";
			} else {
				selection = orgSelection + " AND " + Columns.TYPE + "=?";
			}
			list.add(Types.RECEIPT);
			break;
		case TIMES:
			if (orgSelection == null) {
				selection = Columns.TYPE + " =?";
			} else {
				selection = orgSelection + " AND " + Columns.TYPE + "=?";
			}
			list.add(Types.TIME);
			break;
		case JOURNEYS:
			if (orgSelection == null) {
				selection = Columns.TYPE + " <?";
			} else {
				selection = orgSelection + " AND " + Columns.TYPE + "<?";
			}
			list.add(Types.RECEIPT);
			break;
		case RECEIPT:
		case TIME:
		case JOURNEY:
			id = uri.getLastPathSegment();
			selection = Columns.ID + "=?";
			list.add(id);
			break;
		}
		return selection;
	}

}
