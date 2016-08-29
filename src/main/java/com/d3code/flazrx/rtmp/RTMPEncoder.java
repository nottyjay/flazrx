package com.d3code.flazrx.rtmp;


import com.d3code.flazrx.rtmp.message.ChunkSize;
import com.sun.java.swing.plaf.windows.TMSchema;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Nottyjay on 2016/8/23.
 */
public class RTMPEncoder extends MessageToByteEncoder<RTMPMessage> {

    private static final Logger LOG = LoggerFactory.getLogger(RTMPEncoder.class);

    private int chunkSize = 128;
    private RTMPHeader[] channelPrevHeaders = new RTMPHeader[RTMPHeader.MAX_CHANNEL_ID];

    @Override
    protected void encode(ChannelHandlerContext ctx, RTMPMessage msg, ByteBuf out) throws Exception {
        final ByteBuf in = msg.encode();
        final RTMPHeader header = msg.getHeader();
        if(header.isChunkSize()){
            final ChunkSize csMessage = (ChunkSize)msg;
            if(LOG.isDebugEnabled()){
                LOG.debug("encoder new chunk size: {}", csMessage);
            }
            chunkSize = csMessage.getChunkSize();
        } else if(header.isControl()){
            final Control
        }
    }
}
