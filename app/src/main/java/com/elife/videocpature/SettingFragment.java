package com.elife.videocpature;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.util.DisplayMetrics;

import com.eversince.screenrecord.R;


/**
 * Created by duanjin on 1/1/15.
 */
public class SettingFragment extends PreferenceFragment{

    ListPreference videoSize;
    ListPreference videoQuality;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);
        videoSize = (ListPreference)findPreference(getResources().getString(R.string.video_dimension_key));
        videoQuality = (ListPreference)findPreference(getResources().getString(R.string.video_quality_key));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (videoSize != null) {
            DisplayMetrics metrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
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
        if (videoQuality != null) {
            String[] qualityEntries = {getResources().getString(R.string.quality_high),
                    getResources().getString(R.string.quality_medium),
                    getResources().getString(R.string.quality_low)};
            videoQuality.setEntries(qualityEntries);
            videoQuality.setEntryValues(qualityEntries);
            if (ParameterManager.getInstance(getActivity()).getVideoQuality() == ParameterManager.QUALITY_MEDIUM) {
                videoQuality.setValue(qualityEntries[1]);
            }
            videoQuality.setDefaultValue(qualityEntries[1]);
        }
    }
}
