package com.elife.videocpature;

import android.content.Context;
import android.content.Intent;

/**
 * Created by duanjin on 1/24/15.
 */
public class Utils {
    private static Utils mInstance = null;
    private Context mContext;

    private Utils(Context context) {
        mContext = context;
    }

    public static synchronized Utils getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new Utils(context);
        }

        return mInstance;
    }

    public void shareApp() {
        String appAddress ="http://sj.qq.com/myapp/detail.htm?apkName=com.eversince.screenrecord";
        String message = " 优秀的android屏幕录像工具";

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, appAddress + message);
        mContext.startActivity(Intent.createChooser(intent, "推荐给朋友："));
    }

}
