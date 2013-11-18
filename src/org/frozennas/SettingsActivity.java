package org.frozennas;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;

public class SettingsActivity extends PreferenceActivity {
	
	@Override
	 public void onCreate(Bundle savedInstanceState) {
	  super.onCreate(savedInstanceState);
	  Log.v(Constants.TAG, "Starting SettingsFragment");
	  getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
	 }

}
