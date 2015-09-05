package com.elife.videocpature;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.Toast;

import com.eversince.screenrecord.R;


/**
 * Created by duanjin on 1/11/15.
 */
public class RecordService extends Service{

    public static final String STOP_ACTION = "STOP_ACTION";
    private RecordThread mRecordThread;
    private DisplayMetrics mMetrics  = new DisplayMetrics();
    private boolean mIsRecording = false;
    private Toast mToast;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (null != intent) {
            String action = intent.getAction();
            if (null != action && action.equals(STOP_ACTION)) {
                mRecordThread.quit();
                stopForeground(true);
                Intent intent1 = new Intent();
                intent1.setAction(MainActivity.ACTION);
                sendBroadcast(intent1);
                return Service.START_NOT_STICKY;
            }
        }
        if (null != intent) {
            int result = intent.getIntExtra(MainActivity.RESULT, -1);
            Intent data = (Intent) intent.getParcelableExtra(MainActivity.DATA);
            if (result == Activity.RESULT_OK) {
                //create mediaprojection
                MediaProjectionManager mpm = (MediaProjectionManager)getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                MediaProjection mp = mpm.getMediaProjection(result, data);

                //get metrics info
                WindowManager windowManager = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
                windowManager.getDefaultDisplay().getRealMetrics(mMetrics);

                int width = ParameterManager.getInstance(this).getVideoWidth();
                int height = ParameterManager.getInstance(this).getVideoHeight();
                boolean needAudio = ParameterManager.getInstance(this).needAudio();
                boolean isLandScapeMode = ParameterManager.getInstance(this).isLandScapeModeOn();
                int quality = ParameterManager.getInstance(this).getVideoQuality();
                mRecordThread = new RecordThread(width, height, mMetrics, mp,
                        getResources().getString(R.string.save_dir), needAudio, isLandScapeMode, quality);
                mIsRecording = true;

                mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
                new CountDownTimer(10000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        mToast.setText(String.format(getResources().getString(R.string.start_tips),
                                millisUntilFinished / 1000));
                        mToast.show();
                    }
                    @Override
                    public void onFinish() {
                        mRecordThread.start();
                        Intent clickIntent = new Intent(getBaseContext(), RecordService.class);
                        clickIntent.setAction(STOP_ACTION);
                        PendingIntent pStop = PendingIntent.getService(getBaseContext(), 0, clickIntent, 0);

                        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
                        Notification.Builder builder = new Notification.Builder(RecordService.this);

                        builder.setContentIntent(pStop)
                                .setSmallIcon(R.drawable.ic_launcher)
                                .setLargeIcon(largeIcon)
                                .setTicker(getString(R.string.action_about))
                                .setWhen(System.currentTimeMillis())
                                .setAutoCancel(true)
                                .setContentTitle(getString(R.string.n_title))
                                .setContentText(getString(R.string.n_btn));
                        Notification n = builder.build();
                        startForeground(10001, n);
                    }
                }.start();

            }
        }
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
