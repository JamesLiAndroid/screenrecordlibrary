package com.elife.videocpature;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.TextView;

import com.elife.webserver.HTTPService;
import com.eversince.screenrecord.R;

/**
 * Created by duanjin on 9/4/15.
 */
public class WebServiceAct extends Activity{
    public static final String STOP_CONNECT = "com.dchen.videocapture.stopconnect";
    protected TextView mIpAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_server);
        setTitle(R.string.web_server_title);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        mIpAddress = (TextView) findViewById(R.id.ip_address);
        registerReceiver(uiUpdate, new IntentFilter("LOCAL_UPDATE"));
        Intent intent = new Intent(this, HTTPService.class);
        startService(intent);
    }

    public void setAddress(String address) {
        if (TextUtils.isEmpty(address)) {
            mIpAddress.setText(address);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (uiUpdate != null) {
            unregisterReceiver(uiUpdate);
        }

        Intent stopInt = new Intent(getBaseContext(), HTTPService.class);
        stopService(stopInt);

    }

    private BroadcastReceiver uiUpdate = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String ip = intent.getStringExtra("ip");
                if (!TextUtils.isEmpty(ip)) {
                    mIpAddress.setText(ip);
                }
            }
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
