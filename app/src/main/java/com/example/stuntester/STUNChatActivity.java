package com.example.stuntester;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.stuntester.udp.UdpClient;

import de.javawi.jstun.attribute.ChangeRequest;
import de.javawi.jstun.header.MessageHeader;
import de.javawi.jstun.util.UtilityException;
import io.netty.channel.Channel;

/**
 * @author monkey_liu
 * @date 2019-06-13
 */
public class STUNChatActivity extends Activity implements UdpClient.MessageReceiver {

    private TextView mAddressTextView;
    private UdpClient mUdpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_chat);
        mAddressTextView = findViewById(R.id.address_text_view);
    }

    public void obtainAddressInfo(View view) {
        mUdpClient = new UdpClient("stun.ekiga.net", 3478, this);

    }

    @Override
    public void onChannelSetup() {
        Log.e("lmj", "onChannelSetup");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAddressTextView.setText("获取公网地址中...");
            }
        });
        try {
            MessageHeader sendMH = new MessageHeader(MessageHeader.MessageHeaderType.BindingRequest);
            sendMH.generateTransactionID();

            ChangeRequest changeRequest = new ChangeRequest();
            sendMH.addMessageAttribute(changeRequest);

            mUdpClient.sendMessage(sendMH);
        } catch (UtilityException e) {
            Log.e("lmj", "send error.", e);
        }
    }

    @Override
    public void onReceiveMessage(final String message, Channel channel) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAddressTextView.setText(message);
            }
        });
    }
}
