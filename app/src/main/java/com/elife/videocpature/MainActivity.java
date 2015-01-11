package com.elife.videocpature;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.qq.e.ads.AdListener;
import com.qq.e.ads.AdRequest;
import com.qq.e.ads.AdSize;
import com.qq.e.ads.AdView;

import java.io.File;
import java.util.ArrayList;


public class MainActivity extends Activity {
    private ImageButton startBtn;
    private MediaProjectionManager mProjectionManager;
    private static final int REQUEST_CODE = 0x10001;
    private boolean mIsRecording = false;
    private DisplayMetrics mMetrics  = new DisplayMetrics();
    private RecordThread mRecordThread;

    private ListView mList;
    private VideoListAdapter mAdapter;
    private RelativeLayout mBannerContainer;
    private ActionMode mActionMode;
    private View mSelectedView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startBtn = (ImageButton)findViewById(R.id.start);
        mProjectionManager = (MediaProjectionManager)(getSystemService(Context.MEDIA_PROJECTION_SERVICE));
        getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnClicked();
            }
        });
        mList = (ListView) findViewById(R.id.video_list);

        mList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (mActionMode != null) {
                    return false;
                }
                mActionMode = startActionMode(mActionCallback);
                view.setSelected(true);
                mSelectedView = view;
                mAdapter.setSelectedPos(position, view);
                return true;
            }
        });
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (view.isSelected()) {
                    view.setSelected(false);
                    mAdapter.setSelectedPos(-1, view);
                }
                if (null != mActionMode) {
                    mActionMode.finish();
                    mActionMode = null;
                }
            }
        });
        initVideoList();
        android.os.Process.setThreadPriority(-19);

        mBannerContainer = (RelativeLayout) findViewById(R.id.banner_container);
        initAdvertise();
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
        } else if(id == R.id.action_donate) {
            //进入捐助页面
        } else if (id == R.id.action_about) {
            //进入项目介绍页面
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            MediaProjection projection = mProjectionManager.getMediaProjection(resultCode, data);
            startRecording(projection);
        }
    }

    private void initAdvertise() {
        AdView adv = new AdView(this, AdSize.BANNER, "1103948760","4040605069270554");
        mBannerContainer.addView(adv);
		/* 广告请求数据，可以设置广告轮播时间，默认为30s  */
        AdRequest adr = new AdRequest();
		/* 这个接口的作用是设置广告的测试模式，该模式下点击不扣费
		 * 未发布前请设置testad为true，
		 * 上线的版本请确保设置为false或者去掉这行调用
		 */
        adr.setTestAd(true);
		/* 设置广告刷新时间，为30~120之间的数字，单位为s*/
        adr.setRefresh(31);
		/* 设置空广告和首次收到广告数据回调
		 * 调用fetchAd方法后会发起广告请求，广告轮播时不会产生回调
		 */
        adv.setAdListener(new AdListener() {
            @Override
            public void onBannerClosed() {

            }

            @Override
            public void onAdClicked() {

            }

            @Override
            public void onNoAd() {
                Log.i("no ad cb:","no");
            }
            @Override
            public void onAdReceiv() {
                Log.i("ad recv cb:","revc");
            }

            @Override
            public void onAdExposure() {

            }
        });
		/* 发起广告请求，收到广告数据后会展示数据	 */
        adv.fetchAd(adr);
    }
    private void initVideoList() {
        String dir = Environment.getExternalStorageDirectory().getPath() + "/" +
                getResources().getString(R.string.save_dir);
        File saveDir = new File(dir);
        if (!saveDir.exists()) {
            return;
        }
        File[] allFiles  = saveDir.listFiles();
        ArrayList<String> fileNames = new ArrayList<>();
        for (File file : allFiles) {
            if (file.getName().endsWith(".mp4")) {
                fileNames.add(file.getName());
            }
        }
        if (mAdapter == null) {
            mAdapter = new VideoListAdapter(this, fileNames, dir + "/");
            mList.setAdapter(mAdapter);
        } else {
            mAdapter.setData(fileNames);
            mAdapter.notifyDataSetChanged();
        }


    }

    private void btnClicked() {
        //stop record
        if (mIsRecording) {
            stopRecording();
        } else {
            //start recording
            startRecordingIntent();
        }
    }


    /**
     * 停止录像
     */
    public void stopRecording() {
        if (null != mRecordThread) {
            mRecordThread.quit();
            mRecordThread = null;
            mIsRecording = false;
        }
        initVideoList();
    }

    public void startRecordingIntent() {
        if (null != mProjectionManager) {
            Intent projectInt = mProjectionManager.createScreenCaptureIntent();
            startActivityForResult(projectInt, REQUEST_CODE);
        }
    }

    /**
     * Actually start recording
     */
    public void startRecording(MediaProjection projection) {
        //读取用户设置信息
        int width = ParameterManager.getInstance(this).getVideoWidth();
        int height = ParameterManager.getInstance(this).getVideoHeight();
        boolean needAudio = ParameterManager.getInstance(this).needAudio();
        mRecordThread = new RecordThread(width, height, mMetrics, projection,
                getResources().getString(R.string.save_dir), needAudio);
        mIsRecording = true;
        mRecordThread.start();
    }

    /**
     * 启动设置界面
     */
    private void startSettingActivity() {
        Intent it = new Intent(this, PreferenceActivity.class);
        startActivity(it);
    }

    @Override
    protected void onDestroy() {
        Log.i("duanjin", "activity get destroyed");
        super.onDestroy();
    }

    private ActionMode.Callback mActionCallback = new ActionMode.Callback() {
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
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.share:
                    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                    String selectedFile = mAdapter.getSelectedFileName();
                    if (TextUtils.isEmpty(selectedFile))
                        break;
                    Uri screenshotUri = Uri.fromFile(new File(selectedFile));
                    sharingIntent.setType("video/*");
                    sharingIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
                    MainActivity.this.startActivity(Intent.createChooser(sharingIntent, "分享到："));
                    break;
                case R.id.delete:
                    String fileName = mAdapter.getSelectedFileName();
                    if (!TextUtils.isEmpty(fileName)) {
                        File file = new File(fileName);
                        file.delete();
                        mActionMode.finish();
                        initVideoList();
                    }
                    break;
                default:
                    break;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            if (null != mSelectedView) {
                mSelectedView.setSelected(false);
                mSelectedView = null;
                mAdapter.setSelectedPos(-1, null);
            }
            mActionMode = null;
            getWindow().setStatusBarColor(getResources().getColor(R.color.color_primary_dark));
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
