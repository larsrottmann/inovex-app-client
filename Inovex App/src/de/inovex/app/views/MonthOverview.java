package de.inovex.app.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import de.inovex.app.R;

public class MonthOverview extends DockableView {
	private static final String[] tasks = {"Dominik Helleberg hat morgen Geburtstag ","Für deine Anreise nach Düsseldorf vom 14.06 fehlt die Rückreise!","Am Montag den 05.09 hast du noch keine Zeit erfasst","inovex Newsletter August 2011","inovex NewsLetter Juli 2011", "Renard Wellnitz hat heute Geburtstag","Ein Beleg vom 08.06 ist keiner Reise zugeordnet", "Du hast letzten Monat 3 Überstunden gemacht"};
	private final ListView mListView;

	public MonthOverview(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.overview_main_menu, this);
		mListView = (ListView) findViewById(R.id.listview_tasks);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1,tasks);
		mListView.setAdapter(adapter);
	}
}
