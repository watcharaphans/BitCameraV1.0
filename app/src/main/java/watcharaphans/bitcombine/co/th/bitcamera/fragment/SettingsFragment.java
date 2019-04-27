package watcharaphans.bitcombine.co.th.bitcamera.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import watcharaphans.bitcombine.co.th.bitcamera.PassScreenActivity;
import watcharaphans.bitcombine.co.th.bitcamera.R;

public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "SettingsFragment";

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // Load the Preferences from the XML file
        addPreferencesFromResource(R.xml.preferences);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        settings.registerOnSharedPreferenceChangeListener(this);
        displayCurrentName(settings);

//        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
//        PreferenceScreen preferenceScreen = getPreferenceScreen();
//        int count = preferenceScreen.getPreferenceCount();
//        for (int i = 0; i < count ; i++) {
//            Preference p = preferenceScreen.getPreference(i);
//            if (!(p instanceof ListPreference)) {
//                String value = sharedPreferences.getString(p.getKey(), "");
//                setPreferenceSummery(p, value);
//            }
//        }

        onSharedPreferenceChanged(getPreferenceScreen().getSharedPreferences(), "flash_mode");

        Preference pref = findPreference("setting_camera");
        pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.contentFragmentMain, new PictureSettingsFragment())
                        .addToBackStack(null)
                        .commit();
                return true;
            }
        });
        Log.i(TAG, "---Set onClick PictureSettingsFragment--- ");

        Preference pref_about = findPreference("about");
        pref_about.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference2) {
                Log.i(TAG, "---Open AboutFragment--- ");

                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.contentFragmentMain, new AboutFragment())
                        .addToBackStack(null)
                        .commit();
                return true;
            }
        });

        Preference pref_changepass = findPreference("changePassword");
        pref_changepass.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference2) {
                Log.i(TAG, "---Open Change Password --- ");

                Intent intent = new Intent(getActivity(), PassScreenActivity.class);
                intent.putExtra("key_password", "change_password");
                startActivity(intent);
                return true;
            }
        });

        Log.i(TAG, "---Set onClick AboutFragment--- ");
    }

    private void setPreferenceSummery(Preference preference, Object value){

        String stringValue = value.toString();
        Toast.makeText(getActivity(), "setPreferenceSummery : "+stringValue, Toast.LENGTH_SHORT).show();

        if (preference instanceof ListPreference){
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list (since they have separate labels/values).
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            //same code in one line
            //int prefIndex = ((ListPreference) preference).findIndexOfValue(value);

            //prefIndex must be is equal or garter than zero because
            //array count as 0 to ....
            if (prefIndex >= 0){
                listPreference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else if (preference instanceof EditTextPreference){
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list (since they have separate labels/values).
            EditTextPreference textPreference = (EditTextPreference) preference;
            String prefIndex = stringValue;
            textPreference.setSummary(stringValue);

        }else{
            // For other preferences, set the summary to the value's simple string representation.
            preference.setSummary(stringValue);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
     //   Toast.makeText(getActivity(), "Pref1 "+key, Toast.LENGTH_SHORT).show();
        Log.d("SettingsFragment", "Pref1 "+key);

        Preference pref = findPreference(key);

        if (pref instanceof ListPreference) {
            ListPreference listPref = (ListPreference) pref;
            pref.setSummary(listPref.getEntry());
            Log.d("SettingsFragment", "Pref2 "+key);

        }

        if(key.equals("edit_ip")){
            displayCurrentName(sharedPreferences);
            Toast.makeText(getActivity(), "แก้ไข IP Address" , Toast.LENGTH_SHORT).show();
        }
    }

    private void displayCurrentName(SharedPreferences sharedPreferences){

        // Set IP
        Preference IpPref = findPreference("edit_ip");
        String msgIP = sharedPreferences.getString("edit_ip","");
        Log.d("SettingsFragment", "----edit_ip: "+msgIP);
        IpPref.setSummary(msgIP);

        // Set Mode IN-OUT
        Preference modePref = findPreference("working_mode");
        String msgInOut = sharedPreferences.getString("working_mode","");
        Log.d("SettingsFragment", "----working_mode 0: "+ msgInOut);

//        TextView txtWorkingMode = (TextView) getView().findViewById(R.id.tvWorkingMode);


        switch(msgInOut) {
            case "mode-in" :
                msgInOut = "ถ่ายรูปขาเข้า";
//                txtWorkingMode.setText("[ขาเข้า]");
                break;
            case "mode-out" :
                msgInOut = "ถ่ายรูปขาออก";
//                txtWorkingMode.setText("[ขาออก]");
                break;
            default:
                msgInOut = "โปรดเลือก";
        }

        Log.d("SettingsFragment", "----working_mode 2: "+ msgInOut);
        modePref.setSummary(msgInOut);

        // Set cameraB , cameraC, cameraD
        //นำค่าที่เลือกมา set summary ไม่ได้ เพราะเป็น checkbox จะเเก็บเป็น boolean
    }

}