package com.d3code.flazrx.rtmp.server;

import com.d3code.flazrx.rtmp.RTMPDecoder;
import com.d3code.flazrx.rtmp.RTMPEncoder;
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
        pipeline.addLast("decoder", new RTMPDecoder());
        pipeline.addLast("encoder", new RTMPEncoder());
        pipeline.addLast("handler", null);
    }
}
