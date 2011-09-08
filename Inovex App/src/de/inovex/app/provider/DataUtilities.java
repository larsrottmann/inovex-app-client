package de.inovex.app.provider;

import java.util.Date;

import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.RemoteException;
import de.inovex.app.provider.InovexContentProvider.Columns;

public class DataUtilities {

	private DataUtilities(){
	}
	
	public static Uri saveJourney(Context c, String startLocation, String destination, String type, String description, Date date, int parentId) throws RemoteException {
		ContentProviderClient client = null;
		try {
			ContentValues v = null;
			v = new ContentValues();
			v.put(Columns.START_LOCATION, startLocation);
			v.put(Columns.DESTINATION, destination);
			v.put(Columns.DESCRIPTION, description);
			v.put(Columns.TYPE, Integer.parseInt(type));
			v.put(Columns.START_DATE, date.getTime());
			v.put(Columns.PARENT_ID, parentId);

			client = c.getContentResolver().acquireContentProviderClient(InovexContentProvider.CONTENT_URI);
			return client.insert(InovexContentProvider.CONTENT_URI, v);
		} finally {
			if (client != null) {
				client.release();
			}
		}
	}
}
