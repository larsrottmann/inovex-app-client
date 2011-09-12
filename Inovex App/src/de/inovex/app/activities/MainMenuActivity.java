package de.inovex.app.activities;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import de.inovex.app.R;
import de.inovex.app.activities.contacts.ListContactsActivity;
import de.inovex.app.provider.DataUtilities;

public class MainMenuActivity extends Activity {

	private File mReceiptImage;
	private static final int REQUEST_CODE_MAKE_PHOTO=0;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		View contacts = findViewById(R.id.button_list_contacts);
		contacts.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getApplicationContext(), ListContactsActivity.class);
				startActivity(i);
			}
		});
		
		Button b = (Button) findViewById(R.id.button_add_receipt);
		b.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
			    Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
			    String fileName =  "Beleg_"+ DateFormat.format("MMDDyyyy", new Date()) + ".jpg";
			    mReceiptImage = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),  fileName);
			    try {
			    	mReceiptImage.createNewFile();
			    	mReceiptImage.getParentFile().mkdirs();
				} catch (IOException e) {
					Toast.makeText(MainMenuActivity.this, getText(R.string.error_creating_file), Toast.LENGTH_LONG);
					e.printStackTrace();
				}
			    
			    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mReceiptImage));
			    startActivityForResult(intent, REQUEST_CODE_MAKE_PHOTO);			
			}
		});

	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode==RESULT_OK){
			switch(requestCode){
			case REQUEST_CODE_MAKE_PHOTO:
				int parentId = -1;
				try {
					DataUtilities.saveReceipt(this,Uri.fromFile(mReceiptImage), parentId);				
				} catch (RemoteException e) {
					Toast.makeText(this, getText(R.string.error_saving_journey), Toast.LENGTH_LONG);
					e.printStackTrace();
				} catch (URISyntaxException e) {
					Toast.makeText(this, getText(R.string.error_saving_journey), Toast.LENGTH_LONG);
					e.printStackTrace();
				} catch (IOException e) {
					Toast.makeText(this, getText(R.string.error_saving_journey), Toast.LENGTH_LONG);
					e.printStackTrace();
				}
				break;
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_options_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i = null;
		switch (item.getItemId()) {
	    case R.id.list_journeys:
	    	 i = new Intent(this,ListJourneyActivity.class);
	    	 break;
	    case R.id.list_receipts:
	    	 i = new Intent(this,ListReceiptActivity.class);
	    	 break;
	    case R.id.list_times:
	    	 i = new Intent(this,ListTimeActivity.class);
	    	 break;
	    case R.id.menu_item_preferences:
	    	i = new Intent(this, InovexPreferenceActivity.class);
	    	break;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
		startActivity(i);
		return true;
	}
}
