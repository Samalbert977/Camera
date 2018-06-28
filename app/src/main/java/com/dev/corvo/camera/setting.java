package com.dev.corvo.camera;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;


/*
Copyright 2018 samalbert977

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/


public class setting extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        getFragmentManager().beginTransaction().replace(R.id.settingsReplace, new MypreferenceFragment()).commit();
    }



    public static class MypreferenceFragment extends PreferenceFragment
    {
        private static final String Model ="LG-H540";
        public static String[] CameraResolutions;
        public static int CamNUm;
        public static int RearCam,FrontCam;
        public static String[] EntryValues;
        static ListPreference DefaultResolution;
        static final String[] CustomRes = new String[]{"16 MP (4:3)","18 MP (4:3)","21 MP (4:3)"};
        ListPreference CustomResolution;
        PreferenceScreen screen;
        final Keys keys = new Keys();

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            //addPreferencesFromResource(R.xml.preview_preferences);
            screen = getPreferenceManager().createPreferenceScreen(getActivity());
            DefaultResolution = new ListPreference(getActivity());
            if(CamNUm==RearCam) {
                DefaultResolution.setKey(keys.REAR_CAMERA_RESOLUTION_KEY);
            }
            else if(CamNUm==FrontCam) {
                DefaultResolution.setKey(keys.FRONT_CAMERA_RESOLUTION_KEY);
            }
            DefaultResolution.setTitle(R.string.Pref_Preview_Size);
            DefaultResolution.setSummary(R.string.Pref_Summary);
            DefaultResolution.setEntries(EntryValues);
            DefaultResolution.setEntryValues(CameraResolutions);
            //Adding CheckBox preferences
            //LocationClass Setting
            CheckBoxPreference LocationPref = new CheckBoxPreference(getActivity());
            LocationPref.setKey(keys.LOCATION_KEY);
            LocationPref.setTitle(R.string.Location_title);
            LocationPref.setSummary(R.string.Location_summary);

            CheckBoxPreference CustomResolutions = new CheckBoxPreference(getActivity());
            CustomResolutions.setKey(keys.CUSTOM_RESOLUTION_KEY);
            CustomResolutions.setDefaultValue(false);
            CustomResolutions.setTitle(R.string.checkboxpreference2);
            CustomResolutions.setSummary(R.string.checkboxpreference2_summary);

            CheckBoxPreference SoundPref = new CheckBoxPreference(getActivity());
            SoundPref.setKey(keys.SOUND_PREFERENCE_KEY);
            SoundPref.setTitle(R.string.title_sound);
            SoundPref.setDefaultValue(true);

            CheckBoxPreference BeepPref = new CheckBoxPreference(getActivity());
            BeepPref.setKey(keys.TIMER_SOUND_KEY);
            BeepPref.setTitle(R.string.title_beep);
            BeepPref.setSummary(R.string.title_beep_summary);
            BeepPref.setDefaultValue(true);

            ListPreference VolumeKey = new ListPreference(getActivity());
            VolumeKey.setKey(keys.VOLUME_BUTTON_KEY);
            VolumeKey.setTitle(R.string.title_Volume_keys);
            VolumeKey.setSummary(R.string.title_Volume_keys_summary);
            String[] VolumeEntries = new String[]{
                    "Take photo",
                    "Zoom in/out",
                    "Change volume"
            };
            String[] volumevalues = new String[]{"T","Z","C"};
            VolumeKey.setEntries(VolumeEntries);
            VolumeKey.setEntryValues(volumevalues);

            ListPreference EnhancedImages = new ListPreference(getActivity());
            EnhancedImages.setKey(keys.ENHANCED_IMAGES_KEY);
            EnhancedImages.setTitle(R.string.title_enhance_images);
            EnhancedImages.setSummary(R.string.title_enhanced_summary);
            String[] Entries = new String[]{
                    "No enhancement",
                    "Saturate Images",
                    "Contrast Images",
                    "Saturate and contrast Images"
            };
            String[] EntryValues = new String[]{
                    "N",
                    "S",
                    "C",
                    "SC"
            };
            EnhancedImages.setEntries(Entries);
            EnhancedImages.setEntryValues(EntryValues);
            EnhancedImages.setDefaultValue("N");

            //Adding preferences to root of preference Screen
            //The preferences are aligned according to their order of codeLine below
            PreferenceCategory CameraCategory = new PreferenceCategory(getActivity());
            CameraCategory.setTitle(R.string.Category_camera);
            CameraCategory.setIcon(R.drawable.ic_camera_alt_black_24dp);
            CameraCategory.setEnabled(true);
            screen.addPreference(CameraCategory);
            try{
            CameraCategory.addPreference(SoundPref);
            CameraCategory.addPreference(BeepPref);
            CameraCategory.addPreference(VolumeKey);
            }catch (NullPointerException e){e.printStackTrace();}
            //screen.addPreference(CameraCategory);
            PreferenceCategory ImageCategory = new PreferenceCategory(getActivity());
            ImageCategory.setTitle(R.string.Category_Image);
            ImageCategory.setIcon(R.drawable.ic_image_black_24dp);
            ImageCategory.setEnabled(true);
            screen.addPreference(ImageCategory);
            try{
                ImageCategory.addPreference(EnhancedImages);
                ImageCategory.addPreference(DefaultResolution);

                if(Build.MODEL.equals(Model)){
                ImageCategory.addPreference(CustomResolutions);
                ImageCategory.addPreference(Custom_Resolution());}

            }catch (NullPointerException e){e.printStackTrace();}

            PreferenceCategory LocationCategory = new PreferenceCategory(getActivity());
            LocationCategory.setTitle(R.string.Category_location);
            LocationCategory.setIcon(R.drawable.ic_place_black_24dp);
            LocationCategory.setEnabled(true);

            Preference preference = new Preference(getActivity());
            preference.setTitle(R.string.Info);
            preference.setKey("Info");
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    ShowDialog();
                    return false;
                }
            });
            screen.addPreference(LocationCategory);
            try{
                LocationCategory.addPreference(LocationPref);
            }catch (NullPointerException e){e.printStackTrace();}
            screen.addPreference(preference);
            setPreferenceScreen(screen);
            if(Build.MODEL.equals(Model)){
            CustomResolution.setDependency(keys.CUSTOM_RESOLUTION_KEY);
            if(CamNUm==FrontCam){
                findPreference(keys.CUSTOM_RESOLUTION_KEY).setEnabled(false);}
            else{
                findPreference(keys.CUSTOM_RESOLUTION_KEY).setEnabled(true);
                if(PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).getBoolean("Enable_custom_res",false))
                {
                    findPreference(keys.REAR_CAMERA_RESOLUTION_KEY).setEnabled(false);
                }
                else
                    findPreference(keys.REAR_CAMERA_RESOLUTION_KEY).setEnabled(true);
            }

            SharedPreferences.OnSharedPreferenceChangeListener changeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
                    if(s.equals(keys.CUSTOM_RESOLUTION_KEY))
                    {
                        if(sharedPreferences.getBoolean(s,false))
                        findPreference(keys.REAR_CAMERA_RESOLUTION_KEY).setEnabled(false);
                        else
                            findPreference(keys.REAR_CAMERA_RESOLUTION_KEY).setEnabled(true);
                    }

                }
            };
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(changeListener);}

        }

        private ListPreference Custom_Resolution()
        {
            CustomResolution = new ListPreference(getActivity());
            CustomResolution.setKey(keys.CUSTOM_IMAGE_SIZE_KEY);
            CustomResolution.setEntries(CustomRes);
            CustomResolution.setEntryValues(CustomRes);
            CustomResolution.setTitle(R.string.CustomResTitle);
            CustomResolution.setSummary(R.string.CustomResSummary);

            return CustomResolution;
        }

        @SuppressLint("SetTextI18n")
        private void ShowDialog()
        {
            final Dialog infoDialog = new Dialog(getActivity());
            infoDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            infoDialog.setCancelable(false);
            infoDialog.setContentView(R.layout.about);
            String Manufacture  = Build.MANUFACTURER;
            String Model = Build.MODEL;
            String Version = BuildConfig.VERSION_NAME;
            TextView textView = infoDialog.findViewById(R.id.Info);
            textView.setText("Camera ".concat(Version).concat("\n"+"Manufacturer :"+Manufacture)
            .concat("\n"+"Model :"+Model));
            Button Close = infoDialog.findViewById(R.id.Ok);
            Close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    infoDialog.dismiss();
                }
            });
            infoDialog.show();
         }

    }

}
