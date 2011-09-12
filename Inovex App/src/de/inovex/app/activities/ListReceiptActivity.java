package de.inovex.app.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.GridView;
import de.inovex.app.R;
import de.inovex.app.adapter.ReceiptAdapter;

public class ListReceiptActivity extends Activity {
	
	private GridView mGridView;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.receipt_list);
		mGridView = (GridView) findViewById(R.id.grid_receipts);
		ReceiptAdapter adapter = new ReceiptAdapter(this);
		mGridView.setAdapter(adapter);
	

	}
}
