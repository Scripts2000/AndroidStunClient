package com.example.stuntester.udp;

import android.util.Log;

import de.javawi.jstun.attribute.MappedAddress;
import de.javawi.jstun.attribute.MessageAttribute;
import de.javawi.jstun.header.MessageHeader;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author monkey_liu
 * @date 2019-06-13
 */
public class STUNMessageHandler extends SimpleChannelInboundHandler<MessageHeader> {

    private UdpClient.MessageReceiver mMessageReceiver;

    public STUNMessageHandler(UdpClient.MessageReceiver messageReceiver) {
        mMessageReceiver = messageReceiver;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageHeader msg) throws Exception {
        MappedAddress ma = (MappedAddress) msg.getMessageAttribute(MessageAttribute.MessageAttributeType.MappedAddress);
        String address = ma.getAddress() + ":" + ma.getPort();
        Log.d("lmj", "mapped address:" + address);
        if (mMessageReceiver != null) {
            mMessageReceiver.onReceiveMessage(address, ctx.channel());
        }
    }
}
