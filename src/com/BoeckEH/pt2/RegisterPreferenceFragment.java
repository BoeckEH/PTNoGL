package com.BoeckEH.pt2;

import android.os.Bundle;
import android.preference.PreferenceFragment;
 
public class RegisterPreferenceFragment extends PreferenceFragment {
  
@Override
public void onCreate(Bundle savedInstanceState) {
super.onCreate(savedInstanceState);
	addPreferencesFromResource(R.xml.preferences_register);
}

}