package de.inovex.app.provider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.os.Environment;
import android.os.RemoteException;
import android.widget.Toast;
import de.inovex.app.R;
import de.inovex.app.provider.InovexContentProvider.Columns;

public class DataUtilities {

	final static String EXTERNAL_APP_DIRECTORY = "inovex";
	private final static String CACHE_DIRECTORY = EXTERNAL_APP_DIRECTORY + "/thumbnails";

	private DataUtilities() {
	}

	public static Uri saveTime(Context c, String desc, String project, Date startDate, Date endDate, String type, int parentId) throws RemoteException {
		ContentProviderClient client = null;
		try {
			ContentValues v = makeContentValues(null, null, InovexContentProvider.Types.TIME, desc, startDate, endDate, null, project, parentId);

			client = c.getContentResolver().acquireContentProviderClient(InovexContentProvider.CONTENT_URI);
			return client.insert(InovexContentProvider.CONTENT_URI_TIMES, v);
		} finally {
			if (client != null)
				client.release();
		}
	}

	public static int updateTime(Context c, Uri data, String desc, String project, Date startDate, Date endDate, String type, int parentId) throws RemoteException {
		ContentProviderClient client = null;
		try {
			ContentValues v = makeContentValues(null, null, InovexContentProvider.Types.TIME, desc, startDate, endDate, null, project, parentId);

			client = c.getContentResolver().acquireContentProviderClient(InovexContentProvider.CONTENT_URI);
			return client.update(data, v, null, null);
		} finally {
			if (client != null) {
				client.release();
			}
		}

	}

	public static int updateJourney(Context c, Uri data, String startLocation, String destination, String type, String description, Date startDate, Date endDate, int parentId) throws RemoteException {
		ContentProviderClient client = null;
		try {
			ContentValues v = makeContentValues(startLocation, destination, type, description, startDate, endDate, null, null, parentId);

			client = c.getContentResolver().acquireContentProviderClient(InovexContentProvider.CONTENT_URI);
			return client.update(data, v, null, null);
		} finally {
			if (client != null) {
				client.release();
			}
		}

	}

	private static ContentValues makeContentValues(String startLocation, String destination, String type, String description, Date startDate, Date endDate, Uri imageUri, String project, int parentId) {
		ContentValues v = null;
		v = new ContentValues();
		if (startLocation != null) {
			v.put(Columns.START_LOCATION, startLocation);
		}
		if (destination != null) {
			v.put(Columns.DESTINATION, destination);
		}
		if (description != null) {
			v.put(Columns.DESCRIPTION, description);
		}
		v.put(Columns.TYPE, Integer.parseInt(type));
		if (startDate != null) {
			v.put(Columns.START_DATE, startDate.getTime());
		}
		if (endDate != null) {
			v.put(Columns.END_DATE, endDate.getTime());
		}
		v.put(Columns.PARENT_ID, parentId);
		if (imageUri != null) {
			v.put(Columns.IMAGE_PATH_URI, imageUri.toString());
		}
		if (project != null) {
			v.put(Columns.PROJECT, project);
		}
		return v;
	}

	public static Uri saveReceipt(Context c, Uri fileUri, int parentId) throws RemoteException, URISyntaxException, IOException {
		ContentProviderClient client = null;
		try {
			ContentValues v = makeContentValues(null, null, InovexContentProvider.Types.RECEIPT, null, null, null, fileUri, null, parentId);
			client = c.getContentResolver().acquireContentProviderClient(InovexContentProvider.CONTENT_URI);
			Uri result = client.insert(InovexContentProvider.CONTENT_URI_RECEIPTS, v);
			makeThumb(result.getLastPathSegment(), fileUri);
			Toast.makeText(c, c.getText(R.string.success_saving_receipt), Toast.LENGTH_LONG).show();
			return result;
		} finally {
			if (client != null) {
				client.release();
			}
		}
	}

	private static void makeThumb(String id, Uri fileUri) throws URISyntaxException, IOException {
		URI uri;
		uri = new URI(fileUri.toString());
		File file = new File(uri);
		Bitmap thumb = decodeFile(file.getPath(), 150, 150);
		File thumbDir = new File(Environment.getExternalStorageDirectory(), CACHE_DIRECTORY);		
		if (!thumbDir.exists()){
			thumbDir.mkdirs();
		}
		File thumbFile = new File(thumbDir, id);
		FileOutputStream out = new FileOutputStream(thumbFile);
		thumb.compress(CompressFormat.JPEG, 75, out);
		out.close();

	}

	public static Bitmap loadThumbnail(String id) {
		File thumbDir = new File(Environment.getExternalStorageDirectory(), CACHE_DIRECTORY);
		File thumbFile = new File(thumbDir, String.valueOf(id));
		if (thumbFile.exists()) {
			InputStream stream = null;
			try {
				stream = new FileInputStream(thumbFile);
				return BitmapFactory.decodeStream(stream, null, null);
			} catch (FileNotFoundException e) {
				// Ignore
			} finally {
				try {
					if(stream!=null) {
						stream.close();
					}
				} catch (IOException ignore) {}
			}
		} 
		return null;
	}
	
	private static Bitmap decodeFile(String imagePath, int maxWidth, int maxHeight) {
		Bitmap b = null;
		// Decode image size
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;

		BitmapFactory.decodeFile(imagePath, o);
		int scale = determineScaleFactor(o.outWidth, o.outHeight, maxWidth, maxHeight);

		// Decode with inSampleSize
		o.inSampleSize = scale;
		o.inJustDecodeBounds = false;
		b = BitmapFactory.decodeFile(imagePath, o);
		return b;
	}

	private static int determineScaleFactor(int w, int h, int maxWidth, int maxHeight) {
		int scale = 1;
		if (w > maxWidth || h > maxHeight) {
			scale = (int) Math.pow(2, (int) Math.round(Math.log(Math.max(maxWidth, maxHeight) / (double) Math.max(h, w)) / Math.log(0.5)));
		}
		return scale;
	}

	public static Uri saveJourney(Context c, String startLocation, String destination, String type, String description, Date startDate, Date endDate, int parentId) throws RemoteException {
		ContentProviderClient client = null;
		try {
			ContentValues v = makeContentValues(startLocation, destination, type, description, startDate, endDate, null, null, parentId);

			client = c.getContentResolver().acquireContentProviderClient(InovexContentProvider.CONTENT_URI);
			Uri result = client.insert(InovexContentProvider.CONTENT_URI_JOURNEYS, v);
			Toast.makeText(c, c.getText(R.string.success_saving_journey), Toast.LENGTH_LONG).show();
			return result;
		} finally {
			if (client != null) {
				client.release();
			}
		}
	}
}
