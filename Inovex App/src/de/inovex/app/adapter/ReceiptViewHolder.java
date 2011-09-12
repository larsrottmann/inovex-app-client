package de.inovex.app.adapter;

import android.view.View;
import android.widget.TextView;
import de.inovex.app.R;

public class ReceiptViewHolder {

	TextView mThumbnail;
	public ReceiptViewHolder(View v){
		mThumbnail = (TextView) v.findViewById(R.id.textView_receipt_thumbnail);	
	}
}
