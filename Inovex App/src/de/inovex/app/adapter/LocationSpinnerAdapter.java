package de.inovex.app.adapter;

import de.inovex.app.R;
import android.content.Context;
import android.widget.ArrayAdapter;

public class LocationSpinnerAdapter extends ArrayAdapter<String> {

	private static String[] elements = {"current gps location","D�sseldorf", "Karlsruhe", "Pforzheim", "M�nchen","K�ln"};
	
	public LocationSpinnerAdapter(Context context){
		super(context,android.R.layout.simple_spinner_item, elements);
		this.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	}
	

}
