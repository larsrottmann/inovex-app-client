package de.inovex.app.adapter;

import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.widget.ArrayAdapter;

public class LocationSpinnerAdapter extends ArrayAdapter<String> {

	private static String[] elements = {"Düsseldorf", "Karlsruhe", "Pforzheim", "München","Köln"};
	
	public LocationSpinnerAdapter(Context context){
		super(context,android.R.layout.simple_spinner_item, elements);
		this.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	}
	

	public int getIdByValue(String value){
		for (int id = 0 ; id<elements.length;id++){
			if (elements[id].equals(value)){
				return id;
			}
		}
		return -1;
	}
	
}
