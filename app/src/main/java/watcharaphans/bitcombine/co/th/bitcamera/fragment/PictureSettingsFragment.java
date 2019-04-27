package watcharaphans.bitcombine.co.th.bitcamera.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

import watcharaphans.bitcombine.co.th.bitcamera.MainActivity;
import watcharaphans.bitcombine.co.th.bitcamera.R;

public class PictureSettingsFragment extends PreferenceFragmentCompat implements
        SharedPreferences.OnSharedPreferenceChangeListener{

    private static final String TAG = PictureSettingsFragment.class.getName();
    private Camera mCamera = null;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // Load the Preferences from the XML file
        addPreferencesFromResource(R.xml.picture_preferences);
        SharedPreferences settings2 = PreferenceManager.getDefaultSharedPreferences(getActivity());
        displayCurrentName(settings2);
        listResCamera();

        onSharedPreferenceChanged(getPreferenceScreen().getSharedPreferences(), "image_resolution");

//        Preference pref = findPreference("image_resolution");
//        pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//            @Override
//            public boolean onPreferenceClick(Preference preference) {
//                Toast.makeText(getActivity(), "image_resolution", Toast.LENGTH_SHORT).show();
//
//                return true;
//            }
//        });

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
        Log.d("PictureSettingsFragment", "Pref1 "+key);

        //Preference pref = findPreference(key);

        // Set IP
        Preference ImgQPref = findPreference("image_quality");
        String msgImgQ = sharedPreferences.getString("image_quality","");
        Log.d("PictureSettingsFragment", "----image_quality: "+msgImgQ);
        ImgQPref.setSummary(msgImgQ);

        Preference ImgResPref = findPreference("image_resolution");
        String msgImgRes = sharedPreferences.getString("image_resolution","");
        Log.d("PictureSettingsFragment", "----image_resolution: "+msgImgRes);
        ImgResPref.setSummary(msgImgRes);


//
//        if (pref instanceof ListPreference) {
//            ListPreference listPref = (ListPreference) pref;
//            pref.setSummary(listPref.getEntry());
//            Log.d("PictureSettingsFragment", "Pref2 list "+key);
//
//        } else if (pref instanceof EditTextPreference) {
//            ListPreference listPref = (ListPreference) pref;
//            pref.setSummary(listPref.getEntry());
//            Log.d("PictureSettingsFragment", "Pref2 text "+key);
//
//        }

    }

    private void displayCurrentName(SharedPreferences sharedPreferences){

        //Log.d("PictureSettingsFragment", "Set ค่า ");

        // Set Picture Quality
        Preference picQPref = findPreference("image_quality");
        String msgPicQuality = sharedPreferences.getString("image_quality","");
        Log.d("PictureSettingsFragment", "----image_quality 2: "+ msgPicQuality);
        picQPref.setSummary(msgPicQuality);

        // Set Picture Resolution
        Preference picResPref = findPreference("image_resolution");
        String msgPicRes = sharedPreferences.getString("image_resolution","");
        Log.d("PictureSettingsFragment", "----image_resolution 2: "+ picResPref);
        picResPref.setSummary(msgPicRes);

    }

    private void listResCamera(){

        mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        Camera.Parameters params = mCamera.getParameters();
        List<Camera.Size> sizes = params.getSupportedPreviewSizes();

        int num = 0;
        int count_res = 0;
        String resCam = "";

        for(Camera.Size size : params.getSupportedPreviewSizes()){
            num++ ;

            //Log.i(TAG, num + ") Show all Width :" + size.width + ", Height :" + size.height);
            //TODO ปรับให้ res ได้ถึง 1280x960
            if (640 <= size.width & size.width <= 960) {
                count_res++;

                double ratio = (double) size.width / size.height;
                if( ratio == 1.3333333333333333){

                    Log.i(TAG, num + ") Show all Width :" + size.width + ", Height :" + size.height + " Ratio : " + ratio);
                    resCam = resCam.concat(size.width + "x" + size.height + ",");
                }
            }
        }

        //Log.i(TAG, "Camera Resolution == "+resCam);

        // ListPreference lp = new ListPreference(getActivity());
        ListPreference lp = (ListPreference)findPreference("image_resolution");

        String[] arr = resCam.split(",");

        int arrsize = arr.length;
        String[] entreisRes = new String[arrsize];
        String[] entryValuesRes = new String[arrsize];

        for(int i=0; i<arr.length; i++)
        {
            entreisRes[i] = arr[i];
            entryValuesRes[i] = arr[i];
            Log.i(TAG, "Value index["+i+"] = " + entreisRes + " Values = "+entryValuesRes);
            lp.setEntries(entreisRes);
            lp.setEntryValues(entryValuesRes);
        }

        mCamera.release();
    }

}
