package com.BoeckEH.pt2;

import android.preference.PreferenceFragment;

import android.os.Bundle;
 
public class AudioPreferenceFragment extends PreferenceFragment {
  
@Override
public void onCreate(Bundle savedInstanceState) {
super.onCreate(savedInstanceState);
	addPreferencesFromResource(R.xml.preferences_audio);
}

}