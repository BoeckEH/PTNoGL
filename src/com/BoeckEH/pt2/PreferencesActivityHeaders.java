package com.BoeckEH.pt2;

import java.util.List;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class PreferencesActivityHeaders extends PreferenceActivity {
	   /**
     * Populate the activity with the top-level headers.
     */
    @Override
    public void onBuildHeaders(List<Header> target) {
     loadHeadersFromResource(R.xml.preferences_headers, target);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
	{
    		super.onCreate(savedInstanceState);
    		
	}
    

}
