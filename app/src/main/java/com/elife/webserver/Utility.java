package com.elife.webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;

import com.eversince.screenrecord.R;

public class Utility {
	public static int serverPort;
	public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }
	
	public static String convertStreamToString(InputStream is) {
		/*
		 * To convert the InputStream to String we use the Reader.read(char[]
		 * buffer) method. We iterate until the Reader return -1 which means
		 * there's no more data to read. We use the StringWriter class to
		 * produce the string.
		 */
		if (is != null) {
			Writer writer = new StringWriter();

			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			return writer.toString();
		} else {
			return "";
		}
	}

	public static String openHTMLString(Context context, int id){
		InputStream is = context.getResources().openRawResource(id);
		
		return Utility.convertStreamToString(is);
	}

	public static String getLocalIpStr(Context ctx) {
		WifiManager wm = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wi = wm.getConnectionInfo();
		int ip = wi.getIpAddress();
		final String ipstr;
		if(ip != 0x00000000)
			ipstr ="http://" + (ip & 0xff) + "." + ((ip >> 8) & 0xff) + "." + ((ip >> 16) & 0xff)
					+ "." + ((ip >> 24) & 0xff);
		else
			ipstr = "Relaunch with WiFi";

		return ipstr;
	}

	public static String getSaveFolder(Context ctx) {
		String dir = Environment.getExternalStorageDirectory().getPath() + "/" +
				ctx.getResources().getString(R.string.save_dir);

		return dir;
	}

	public static int getServerPort() {
		return serverPort;
	}
}
