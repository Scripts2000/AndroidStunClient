package com.example.stuntester;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.net.BindException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import de.javawi.jstun.test.DiscoveryInfo;
import de.javawi.jstun.test.DiscoveryTest;

/**
 * @author monkey_liu
 * @date 2019-06-13
 */
public class STUNTestActivity extends Activity {
    private static final String TAG = "STUNTestActivity";

    private Handler mHandler = new Handler();

    private TextView mNatTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_stun_test);
        mNatTextView = findViewById(R.id.nat_info_text_view);
    }

    public void obtainNatType(View view) {
        new DiscoveryThread().start();
        mNatTextView.setText("discovering...");
    }

    public void enterChat(View view) {
        startActivity(new Intent(this, STUNChatActivity.class));
    }

    private class DiscoveryThread extends Thread {

        private InetAddress mLocalAddress;

        public DiscoveryThread() {
            mLocalAddress = getLocalAddress();
            Log.d(TAG, "local address:" + mLocalAddress);
        }

        @Override
        public void run() {
            super.run();
            try {
                DiscoveryTest test = new DiscoveryTest(mLocalAddress, "stun.ekiga.net", 3478);
                final DiscoveryInfo discoveryInfo = test.test();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mNatTextView.setText(discoveryInfo.toString());
                    }
                });
            } catch (BindException be) {
                Log.e(TAG, mLocalAddress.toString() + ": " + be.getMessage());
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }

        private InetAddress getLocalAddress() {
            try {
                Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
                while (ifaces.hasMoreElements()) {
                    NetworkInterface iface = ifaces.nextElement();
                    Enumeration<InetAddress> iaddresses = iface.getInetAddresses();
                    while (iaddresses.hasMoreElements()) {
                        InetAddress iaddress = iaddresses.nextElement();
                        if (Class.forName("java.net.Inet4Address").isInstance(iaddress)) {
                            if ((!iaddress.isLoopbackAddress()) && (!iaddress.isLinkLocalAddress())) {
                                return iaddress;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
            return null;
        }
    }
}
