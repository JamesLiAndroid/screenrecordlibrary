package com.elife.videocpature;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.util.DisplayMetrics;

/**
 * Created by duanjin on 1/1/15.
 */
public class SettingFragment extends PreferenceFragment{

    ListPreference videoSize;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);
        videoSize = (ListPreference)findPreference(getResources().getString(R.string.video_dimension_key));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (videoSize != null) {
            DisplayMetrics metrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int width = metrics.widthPixels;
            int height = metrics.heightPixels;
            String[] sizeEntries = new String[3];
            for (int i = 0; i < sizeEntries.length; ++i) {
                String size = String.format("%d x %d", width/(i + 1), height/ (i + 1));
                sizeEntries[i] = size;
            }

            videoSize.setEntries(sizeEntries);
            videoSize.setEntryValues(sizeEntries);
            if (ParameterManager.getInstance(getActivity()).getVideoWidth() == 0) {
                videoSize.setValue(sizeEntries[0]);
            }
            videoSize.setDefaultValue(sizeEntries[0]);
        }
    }
}
