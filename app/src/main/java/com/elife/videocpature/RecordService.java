package com.elife.videocpature;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by duanjin on 1/11/15.
 */
public class RecordService extends Service{


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
