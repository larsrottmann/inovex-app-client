package de.inovex.app.provider;

import java.util.Date;

import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.RemoteException;
import android.widget.Toast;
import de.inovex.app.R;
import de.inovex.app.provider.InovexContentProvider.Columns;

public class DataUtilities {

	private DataUtilities(){
	}
	
	public static Uri saveTime(Context c, String desc, String project, Date startDate, Date endDate, String type, int parentId) throws RemoteException {
		ContentProviderClient client = null;
		try {
			ContentValues v = new ContentValues();
			v.put(Columns.DESCRIPTION, desc);
			v.put(Columns.PROJECT, project);
			v.put(Columns.START_DATE, startDate.getTime());
			v.put(Columns.END_DATE, endDate.getTime());
			v.put(Columns.TYPE, Integer.valueOf(type));
			v.put(Columns.PARENT_ID, parentId);

			client = c.getContentResolver().acquireContentProviderClient(InovexContentProvider.CONTENT_URI);
			return client.insert(InovexContentProvider.CONTENT_URI, v);
		} finally {
			if (client != null)
				client.release();
		}
	}

	
	public static int updateJourney(Context c, Uri data,String startLocation, String destination, String type, String description, Date startDate,Date endDate, int parentId) throws RemoteException {
		ContentProviderClient client = null;
		try {
			ContentValues v = makeContentValues(startLocation, destination, type, description, startDate, endDate, parentId);

			client = c.getContentResolver().acquireContentProviderClient(InovexContentProvider.CONTENT_URI);
			return client.update(data, v, null, null);
		} finally {
			if (client != null) {
				client.release();
			}
		}

	}

	private static ContentValues makeContentValues(String startLocation, String destination, String type, String description, Date startDate,Date endDate, int parentId) {
		ContentValues v = null;
		v = new ContentValues();
		v.put(Columns.START_LOCATION, startLocation);
		v.put(Columns.DESTINATION, destination);
		v.put(Columns.DESCRIPTION, description);
		v.put(Columns.TYPE, Integer.parseInt(type));
		v.put(Columns.START_DATE, startDate.getTime());
		v.put(Columns.END_DATE, endDate.getTime());
		v.put(Columns.PARENT_ID, parentId);
		return v;
	}
	
	public static Uri saveJourney(Context c, String startLocation, String destination, String type, String description, Date startDate,Date endDate, int parentId) throws RemoteException{
		ContentProviderClient client = null;
		try {
			ContentValues v = makeContentValues(startLocation, destination, type, description, startDate, endDate, parentId);

			client = c.getContentResolver().acquireContentProviderClient(InovexContentProvider.CONTENT_URI);
			Uri result = client.insert(InovexContentProvider.CONTENT_URI, v);
			Toast.makeText(c, c.getText(R.string.success_saving_journey), Toast.LENGTH_LONG).show();
			return result;
		} finally {
			if (client != null) {
				client.release();
			}
		}
	}
}
