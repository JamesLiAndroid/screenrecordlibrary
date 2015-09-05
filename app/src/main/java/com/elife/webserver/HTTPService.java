package com.elife.webserver;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


public class HTTPService extends Service {
	private static final int NOTIFICATION_STARTED_ID = 1;
	
	private NotificationManager notifyManager = null;
	private WebServer server = null;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		notifyManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		server = new WebServer(this, notifyManager);
	}

	@Override
	public void onDestroy() {
		server.stopThread();
		notifyManager.cancel(NOTIFICATION_STARTED_ID);
		
		notifyManager = null;
		
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		server.startThread();
		
		showNotification();
		
		return START_STICKY;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	private void showNotification(){

	}
}
