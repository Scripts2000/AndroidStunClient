package com.example.stuntester.udp;

import android.util.Log;

import java.util.List;

import de.javawi.jstun.header.MessageHeader;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * @author monkey_liu
 * @date 2019-06-13
 */
public class STUNDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        Log.d("lmj", "decode data");
        byte[] messageData = new byte[in.readableBytes()];
        in.readBytes(messageData);
        MessageHeader messageHeader = MessageHeader.parseHeader(messageData);
        messageHeader.parseAttributes(messageData);
        out.add(messageHeader);
    }
}
