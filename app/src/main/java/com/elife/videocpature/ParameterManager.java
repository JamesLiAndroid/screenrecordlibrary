package com.elife.videocpature;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.eversince.screenrecord.R;


/**
 * Created by duanjin on 1/1/15.
 */
public class ParameterManager {
    public static final int QUALITY_HIGH = 3000000;
    public static final int QUALITY_MEDIUM = 1500000;
    public static final int QUALITY_LOW = 600000;
    private static ParameterManager mInstance;
    private Context mContext;

    private ParameterManager(Context context){
        mContext = context;
    }

    public static ParameterManager getInstance(Context context) {
        if (null == mInstance) {
            synchronized (ParameterManager.class) {
                if (null == mInstance) {
                    mInstance = new ParameterManager(context);
                }
            }
        }
        return mInstance;
    }

    public boolean needAudio() {
        if (null != mContext) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
            return sp.getBoolean(mContext.getResources().getString(R.string.record_audio_key), false);
        }
        return false;
    }

    public int getVideoWidth() {
        String demision = "";
        if (null != mContext) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
            demision = sp.getString(mContext.getResources().getString(R.string.video_dimension_key), "");
        }
        if (!TextUtils.isEmpty(demision)) {
            String[] size = demision.split("x");
            if (null != size && size.length > 0) {
                return Integer.valueOf(size[0].trim());
            }
        }
        return 0;
    }

    public int getVideoHeight() {
        String demision = "";
        if (null != mContext) {
            SharedPreferences sp =  PreferenceManager.getDefaultSharedPreferences(mContext);
            demision = sp.getString(mContext.getResources().getString(R.string.video_dimension_key), "");
        }

        if (!TextUtils.isEmpty(demision)) {
            String[] size = demision.split("x");
            if (null != size && size.length > 0) {
                return  Integer.valueOf(size[1].trim());
            }
        }
        return 0;
    }

    public boolean isLandScapeModeOn() {
        if (null != mContext) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
           return sp.getBoolean(mContext.getResources().getString(R.string.record_mode_key), false);
        }
        return false;
    }
    public int getVideoQuality() {
        if (null != mContext) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
            String quality = sp.getString(mContext.getResources().getString(R.string.video_quality_key), "");
            if (!TextUtils.isEmpty(quality)) {
                if (quality.equals(mContext.getResources().getString(R.string.quality_high))){
                    return QUALITY_HIGH;
                }
                if (quality.equals(mContext.getResources().getString(R.string.quality_medium))){
                    return QUALITY_MEDIUM;
                }
                if (quality.equals(mContext.getResources().getString(R.string.quality_low))) {
                    return QUALITY_LOW;
                }
            }
        }
        return QUALITY_MEDIUM;
    }
}
