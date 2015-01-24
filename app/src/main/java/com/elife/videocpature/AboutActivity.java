package com.elife.videocpature;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.eversince.screenrecord.R;


/**
 * Created by duanjin on 1/18/15.
 */
public class AboutActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_ly);

        Button contact = (Button)findViewById(R.id.contact);
        contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailAth();

            }
        });

        final Button recommend = (Button)findViewById(R.id.recommend);
        recommend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recommendTo();

            }
        });
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void emailAth() {
        String[] To = {"chenduanjin@gmail.com"};
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setData(Uri.parse("mailto:"));
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, To);
        startActivity(Intent.createChooser(intent, "发送邮件："));

    }

    private void recommendTo() {
        Utils.getInstance(this).shareApp();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
