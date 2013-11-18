package org.frozennas;

import android.app.Dialog;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;
import ar.com.daidalos.afiledialog.FileChooserDialog;
import java.io.File;

public class SettingsFragment extends PreferenceFragment {
	private EditTextPreference encfsvolumefile_preference;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try{
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        
        encfsvolumefile_preference = (EditTextPreference) findPreference("encfsvolumefile_preference");
        
        encfsvolumefile_preference.setOnPreferenceClickListener (
        		new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference arg0) {

						FileChooserDialog dialog = new FileChooserDialog(getActivity());
						dialog.setCanCreateFiles(false);
						dialog.addListener(new FileChooserDialog.OnFileSelectedListener() {

							@Override
							public void onFileSelected(Dialog source, File file) {
								encfsvolumefile_preference.getEditText().setText(file.getAbsolutePath());
								source.dismiss();			
					         }

							//Should never be called, cause setCanCreateFiles(false)
							@Override
					         public void onFileSelected(Dialog source, File folder, String name) {
								encfsvolumefile_preference.getEditText().setText(folder.getAbsolutePath() + "/" + name);
					            source.dismiss();}
					     });						
						
					    dialog.show();						
						return true;
					}
				}
                );                 	
         
        } catch (Exception e) {
        	Log.v(Constants.TAG, e.getMessage());
        }
    }
	
}
