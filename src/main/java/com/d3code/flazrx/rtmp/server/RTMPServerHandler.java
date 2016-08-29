package com.d3code.flazrx.rtmp.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * Created by Nottyjay on 2016/8/23.
 */
public class RTMPServerHandler extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast("decoder", null);
        pipeline.addLast("encoder", null);
        pipeline.addLast("handler", null);
    }
}
