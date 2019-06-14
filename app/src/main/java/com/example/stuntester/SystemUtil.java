package com.example.stuntester;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class SystemUtil {

    private SystemUtil() {
    }

    public static String getWifiIp(Context context) {
        int ip = 0;
        WifiInfo wifiInfo = getWifiInfo(context);
        if (wifiInfo != null) {
            ip = wifiInfo.getIpAddress();
        }
        return int2IpAddress(ip);
    }

    private static WifiInfo getWifiInfo(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().
                getSystemService(Context.WIFI_SERVICE);
        return wifiManager.getConnectionInfo();
    }

    private static String int2IpAddress(int ip) {
        return (ip & 0xff) + "." + ((ip >> 8) & 0xff) + "." + ((ip >> 16) & 0xff) + "." + ((ip >> 24) & 0xff);

    }
}
