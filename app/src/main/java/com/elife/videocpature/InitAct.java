package com.elife.videocpature;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.FrameLayout;

import com.eversince.screenrecord.R;
import com.qq.e.ads.splash.SplashAD;
import com.qq.e.ads.splash.SplashADListener;

/**
 * Created by duanjin on 10/26/15.
 */
public class InitAct extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_init_ly);
        getActionBar().hide();
        FrameLayout adContainer = (FrameLayout)findViewById(R.id.ad_container);
        new SplashAD(this, adContainer, "1103948760", "3080704604029364", new SplashADListener() {
            @Override
            public void onADDismissed() {
                Intent intent = new Intent(InitAct.this, MainActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onNoAD(int i) {
                Intent intent = new Intent(InitAct.this, MainActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onADPresent() {

            }
        });

    }


}
