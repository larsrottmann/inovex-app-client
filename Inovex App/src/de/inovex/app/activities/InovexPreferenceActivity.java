package de.inovex.app.activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import de.inovex.app.R;

public class InovexPreferenceActivity extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
}
