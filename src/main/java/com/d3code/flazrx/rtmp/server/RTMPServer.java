package com.d3code.flazrx.rtmp.server;

import com.d3code.flazrx.rtmp.RTMPConfig;
import com.d3code.flazrx.util.StopMonitor;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RTMP Server Start
 * Created by Nottyjay on 2016/8/23.
 */
public class RTMPServer {

    private static final Logger LOG = LoggerFactory.getLogger(RTMPServer.class);

    public static void main(String[] args){
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();

        try{
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workGroup);
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.option(ChannelOption.TCP_NODELAY, true);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.childHandler(new RTMPServerHandler());

            LOG.info("Listening port: {}", RTMPConfig.SERVER_PORT);
            Channel channel = bootstrap.bind(RTMPConfig.SERVER_PORT).sync().channel();
            LOG.info("RTMP server has been started! Waiting connect....");

            final Thread stopListener = new StopMonitor(RTMPConfig.SERVER_STOP_PORT);
            stopListener.start();
            stopListener.join();

            final ChannelFuture future = channel.close();
            LOG.info("Closing channels");
            future.sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            LOG.info("releasing resources");
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
            LOG.info("server stopped");
        }
    }
}
