package com.example.stuntester.udp;

import android.util.Log;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import de.javawi.jstun.attribute.MappedAddress;
import de.javawi.jstun.attribute.MessageAttribute;
import de.javawi.jstun.header.MessageHeader;
import de.javawi.jstun.util.UtilityException;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.ReferenceCountUtil;

/**
 * Created by codezjx on 2019-05-23.
 */
public class UdpClient implements Runnable {

    protected Bootstrap mBootstrap;
    protected EventLoopGroup mWorkerGroup;
    protected Channel mChannel;

    private String mHostIp;
    private int mHostPort;

    private MessageReceiver mMessageReceiver;

    public UdpClient(String hostIP, int hostPort, final MessageReceiver messageReceiver) {
        mHostIp = hostIP;
        mHostPort = hostPort;
        mMessageReceiver = messageReceiver;
        mWorkerGroup = new NioEventLoopGroup();
        mBootstrap = new Bootstrap();
//        mBootstrap.remoteAddress(hostIP, hostPort);
        mBootstrap.group(mWorkerGroup)
                .channel(NioDatagramChannel.class)
                .remoteAddress(hostIP, hostPort)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_RCVBUF, 1024)
                .option(ChannelOption.SO_SNDBUF, 1024)
                .handler(new ChannelInitializer<DatagramChannel>() {
                    @Override
                    protected void initChannel(DatagramChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new IdleStateHandler(12, 15, 0));
                        pipeline.addLast(new UpdInboundHandler(mMessageReceiver));
                    }
                });
        try {
            mBootstrap.bind(new InetSocketAddress("123", 12));
            ChannelFuture channelFuture = mBootstrap.bind(12).sync();
            mChannel = channelFuture.channel();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Executors.newSingleThreadExecutor().execute(this);
    }

    @Override
    public void run() {
        try {
            ChannelFuture channelFuture = mBootstrap.bind(0).sync();
            mChannel = channelFuture.channel();
            if (mMessageReceiver != null) {
                mMessageReceiver.onChannelSetup();
            }
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            Log.e("lmj", e.getMessage(), e);
        } finally {
            mWorkerGroup.shutdownGracefully();
        }
    }

    public void stop() {
        if (mChannel != null) {
            mChannel.close();
            mChannel = null;
        }

        if (mWorkerGroup != null) {
            mWorkerGroup.shutdownGracefully();
            mWorkerGroup = null;
        }
        mBootstrap = null;
    }

    public ChannelFuture sendMessage(MessageHeader msg) {
        try {
            Log.d("lmj", "sendMessage");
            return mChannel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(msg.getBytes()), new InetSocketAddress(mHostIp, mHostPort)));
        } catch (UtilityException e) {
            Log.e("lmj", e.getMessage(), e);
        }
        return null;
    }


    public class UpdInboundHandler extends SimpleChannelInboundHandler<DatagramPacket> {

        private MessageReceiver mMessageReceiver;

        public UpdInboundHandler(MessageReceiver messageReceiver) {
            mMessageReceiver = messageReceiver;
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
            ByteBuf data = msg.content();
            byte[] messageData = new byte[data.readableBytes()];
            data.readBytes(messageData);

            Log.d("lmj", "read data:" + messageData.length);
            MessageHeader messageHeader = MessageHeader.parseHeader(messageData);
            messageHeader.parseAttributes(messageData);

            MappedAddress ma = (MappedAddress) messageHeader.getMessageAttribute(MessageAttribute.MessageAttributeType.MappedAddress);
            String address = ma.getAddress() + ":" + ma.getPort();
            Log.d("lmj", "mapped address:" + address);
            if (mMessageReceiver != null) {
                mMessageReceiver.onReceiveMessage(address, ctx.channel());
            }
        }
    }

    public class UpdOutboundHandler extends ChannelOutboundHandlerAdapter {
        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            super.write(ctx, msg, promise);
            ReferenceCountUtil.release(msg);
            ctx.write(msg).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (!future.isSuccess()) {
                        future.cause().printStackTrace();
                        future.channel().close();
                    }
                }
            });
            promise.setFailure()
            promise.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    //...
                }
            });
        }
    }

    public class MyInboundHandler extends SimpleChannelInboundHandler<Object> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            super.exceptionCaught(ctx, cause);
        }
    }

    public interface MessageReceiver {
        void onChannelSetup();

        void onReceiveMessage(String message, Channel channel);
    }
}
