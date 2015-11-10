package com.elife.videocpature;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.baidu.appx.BDBannerAd;
import com.eversince.recordlibrary.RecordConst;
import com.eversince.recordlibrary.service.RecordService;
import com.eversince.screenrecord.R;

import com.qq.e.ads.banner.ADSize;
import com.qq.e.ads.banner.AbstractBannerADListener;
import com.qq.e.ads.banner.BannerView;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;



public class MainActivity extends Activity {
    public final static String RESULT = "com.record.result";
    public final static String DATA = "com.record.data.intent";

    private MediaProjectionManager mProjectionManager;
    private static final int REQUEST_CODE = 0x10001;
    private boolean mIsRecording = false;
    private DisplayMetrics mMetrics = new DisplayMetrics();

    private ListView mList;
    private VideoListAdapter mAdapter;
    private RelativeLayout mBannerContainer;
    IntentFilter mFilter;
    final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase(RecordConst.RECORD_COMPLETE_ACTION))
                MainActivity.this.initVideoList();
            MainActivity.this.mIsRecording = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MobclickAgent.setDebugMode(true);
        setContentView(R.layout.activity_main);
        mProjectionManager = (MediaProjectionManager) (getSystemService(Context.MEDIA_PROJECTION_SERVICE));
        getWindowManager().getDefaultDisplay().getMetrics(mMetrics);


        mList = (ListView) findViewById(R.id.video_list);

        mList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        mList.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                if (position <= 0) {
                    return;
                }
                int selectedCount = mList.getCheckedItemCount();
                mode.setTitle(selectedCount + "");
                if (checked) {
                    mAdapter.addSelectedPos(position - 1);
                } else {
                    mAdapter.removeSelectedPos(position - 1);
                }
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.menu_context, menu);
                getWindow().setStatusBarColor(getResources().getColor(R.color.context_color_dark));
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.share:
                        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                        ArrayList<String> selectedFile = mAdapter.getSelectedFileName();
                        if (null == selectedFile)
                            break;
                        Uri screenshotUri = Uri.fromFile(new File(selectedFile.get(0)));
                        sharingIntent.setType("video/*");
                        sharingIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
                        MainActivity.this.startActivity(Intent.createChooser(sharingIntent, "分享到："));
                        mode.finish();

                        break;
                    case R.id.delete:

                        new AlertDialog.Builder(MainActivity.this)
                                .setMessage("确认删除所选视频？")
                                .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ArrayList<String> fileNames = mAdapter.getSelectedFileName();
                                        for (String fileName : fileNames) {
                                            if (!TextUtils.isEmpty(fileName)) {
                                                File file = new File(fileName);
                                                file.delete();
                                            }
                                        }
                                        mode.finish();
                                    }
                                }).setNegativeButton("取消", null).show();
                        break;
                    default:
                        break;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                mAdapter.deSelectAll();
                initVideoList();
                getWindow().setStatusBarColor(getResources().getColor(R.color.color_primary_dark));
            }
        });
        initVideoList();
        mBannerContainer = (RelativeLayout) findViewById(R.id.banner_container);
        initAdvertise();
        mFilter = new IntentFilter(RecordConst.RECORD_COMPLETE_ACTION);
        registerReceiver(mReceiver, mFilter);
        getDeviceInfo(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startSettingActivity();
            return true;
        } else if (id == R.id.action_donate) {
            //进入推荐按钮
            Utils.getInstance(this).shareApp();

        } else if (id == R.id.action_about) {
            //进入项目介绍页面
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);

        } else if (id == R.id.action_record) {
            btnClicked();
        } else if (id == R.id.action_manage_file) {
            startManageFileAct();

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            mIsRecording = false;
            Intent intent = new Intent(this, RecordService.class);

            int width = ParameterManager.getInstance(this).getVideoWidth();
            int height = ParameterManager.getInstance(this).getVideoHeight();
            boolean needAudio = ParameterManager.getInstance(this).needAudio();
            boolean isLandScapeMode = ParameterManager.getInstance(this).isLandScapeModeOn();
            int quality = ParameterManager.getInstance(this).getVideoQuality();

            intent.putExtra(RecordConst.RECORD_INTENT_RESULT, resultCode);
            intent.putExtra(RecordConst.RECORD_DATA_INTENT, data);
            intent.putExtra(RecordConst.KEY_RECORD_SCREEN_WITH, width);
            intent.putExtra(RecordConst.KEY_RECORD_SCREEN_HEIGHT, height);
            intent.putExtra(RecordConst.KEY_RECORD_NEED_AUDIO, needAudio);
            intent.putExtra(RecordConst.KEY_RECORD_IS_LANDSCAPE, isLandScapeMode);
            intent.putExtra(RecordConst.KEY_VIDEO_QUALITY, quality);
            intent.putExtra(RecordConst.KEY_VIDEO_DIR, getResources().getString(R.string.save_dir));
            intent.putExtra(RecordConst.KEY_NOTIFICATION_ICON, R.drawable.ic_launcher);
            startService(intent);
            moveTaskToBack(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initVideoList();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    private void initTecentAdvertise() {

        BannerView bannerView = new BannerView(this, ADSize.BANNER, "1103948760", "4040605069270554");
        bannerView.setRefresh(30);
        bannerView.setADListener(new AbstractBannerADListener() {
            @Override
            public void onNoAD(int i) {

            }

            @Override
            public void onADReceiv() {

            }
        });
        mBannerContainer.addView(bannerView);
        bannerView.loadAD();
//
//
//        AdView adv = new AdView(this, AdSize.BANNER, "1103948760", "4040605069270554");
//        mBannerContainer.addView(adv);
//        /* 广告请求数据，可以设置广告轮播时间，默认为30s  */
//        AdRequest adr = new AdRequest();
//		/* 这个接口的作用是设置广告的测试模式，该模式下点击不扣费
//		 * 未发布前请设置testad为true，
//		 * 上线的版本请确保设置为false或者去掉这行调用
//		 */
////        adr.setTestAd(true);
//		/* 设置广告刷新时间，为30~120之间的数字，单位为s*/
//        adr.setRefresh(31);
//		/* 设置空广告和首次收到广告数据回调
//		 * 调用fetchAd方法后会发起广告请求，广告轮播时不会产生回调
//		 */
//        adv.setAdListener(new AdListener() {
//            @Override
//            public void onBannerClosed() {
//
//            }
//
//            @Override
//            public void onAdClicked() {
//
//            }
//
//            @Override
//            public void onNoAd() {
//                Log.i("no ad cb:", "no");
//            }
//
//            @Override
//            public void onAdReceiv() {
//                Log.i("ad recv cb:", "revc");
//            }
//
//            @Override
//            public void onAdExposure() {
//
//            }
//        });
//		/* 发起广告请求，收到广告数据后会展示数据	 */
//        adv.fetchAd(adr);
    }


    private void initBaiduAdertise() {
        BDBannerAd bannerAdView = new BDBannerAd(this, "2A42aA2f3OcRTtsGgvPMrxzh", "sbwSIlPeYlsuVvXpLbWhOZuL");
        bannerAdView.setAdListener(new BDBannerAd.BannerAdListener() {
            @Override
            public void onAdvertisementDataDidLoadSuccess() {

            }

            @Override
            public void onAdvertisementDataDidLoadFailure() {

            }

            @Override
            public void onAdvertisementViewDidShow() {

            }

            @Override
            public void onAdvertisementViewDidClick() {

            }

            @Override
            public void onAdvertisementViewWillStartNewIntent() {

            }
        });
        mBannerContainer.addView(bannerAdView);
    }

    private void initAdvertise() {
//        if (BuildConfig.FLAVOR.equals("baidu")) {
//            initBaiduAdertise();
//        } else if (BuildConfig.FLAVOR.equals("qq")) {
//            initTecentAdvertise();
//        }
        initTecentAdvertise();
    }

    private void initVideoList() {
        String dir = Environment.getExternalStorageDirectory().getPath() + "/" +
                getResources().getString(R.string.save_dir);
        File saveDir = new File(dir);
        if (!saveDir.exists()) {
            return;
        }
        File[] allFiles = saveDir.listFiles();
        ArrayList<String> fileNames = new ArrayList<>();
        for (File file : allFiles) {
            if (file.getName().endsWith(".mp4")) {
                fileNames.add(file.getName());
            }
        }
        if (mAdapter == null) {
            mAdapter = new VideoListAdapter(this, fileNames, dir + "/");
            mList.setAdapter(mAdapter);
            View headView = LayoutInflater.from(this).inflate(R.layout.view_header, null);
            View footView = LayoutInflater.from(this).inflate(R.layout.view_header, null);
            mList.addHeaderView(headView);
            mList.addFooterView(footView);
        } else {
            Collections.reverse(fileNames);
            mAdapter.setData(fileNames);
            mAdapter.notifyDataSetChanged();
        }
    }

    private void btnClicked() {
        //stop record
        if (mIsRecording) {
            Toast.makeText(this, R.string.record_already_start, Toast.LENGTH_SHORT).show();
            return;
        } else {
            //start recording
            startRecordingIntent();
        }
    }

    public void startRecordingIntent() {
        if (null != mProjectionManager) {
            Intent projectInt = mProjectionManager.createScreenCaptureIntent();
            startActivityForResult(projectInt, REQUEST_CODE);
        }
    }

    /**
     * 启动设置界面
     */
    private void startSettingActivity() {
        Intent it = new Intent(this, PreferenceActivity.class);
        startActivity(it);
    }

    private void startManageFileAct() {
        Intent it = new Intent(this, WebServiceAct.class);
        startActivity(it);
    }

    @Override
    protected void onDestroy() {
        Log.i("duanjin", "activity get destroyed");
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    public boolean isPasswordRequired() {
        return false;
    }

    public String getPassword() {
        return "";
    }


    public static String getDeviceInfo(Context context) {
        try {
            org.json.JSONObject json = new org.json.JSONObject();
            android.telephony.TelephonyManager tm = (android.telephony.TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);

            String device_id = tm.getDeviceId();

            android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager) context.getSystemService(Context.WIFI_SERVICE);

            String mac = wifi.getConnectionInfo().getMacAddress();
            json.put("mac", mac);

            if (TextUtils.isEmpty(device_id)) {
                device_id = mac;
            }

            if (TextUtils.isEmpty(device_id)) {
                device_id = android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
            }

            json.put("device_id", device_id);
            Log.i("DJDJDJ", json.toString());

            return json.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
