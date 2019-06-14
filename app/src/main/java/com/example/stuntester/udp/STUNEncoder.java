package com.example.stuntester.udp;

import android.util.Log;

import de.javawi.jstun.header.MessageHeader;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author monkey_liu
 * @date 2019-06-13
 */
public class STUNEncoder extends MessageToByteEncoder<MessageHeader> {
    @Override
    protected void encode(ChannelHandlerContext ctx, MessageHeader msg, ByteBuf out) throws Exception {
        Log.d("lmj", "encode data:" + msg.getBytes().length);
        out.writeBytes(msg.getBytes());
    }
}
