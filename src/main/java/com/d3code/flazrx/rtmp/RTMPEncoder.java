package com.d3code.flazrx.rtmp;


import com.d3code.flazrx.rtmp.message.ChunkSize;
import com.d3code.flazrx.rtmp.message.Control;
import com.d3code.flazrx.util.LoggerUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
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
            final Control control = (Control) msg;
            if(control.getType() == Control.Type.STREAM_BEGIN){
                clearPrevHeaders();
            }
        }
        final int channelId = header.getChannelId();
        header.setSize(in.readableBytes());
        final RTMPHeader prevHeader = channelPrevHeaders[channelId];
        if(prevHeader != null && header.getStreamId() > 0 && header.getTime() > 0){
            if(header.getSize() == prevHeader.getSize()){
                header.setHeaderType(RTMPHeader.Type.SMALL);
            }else{
                header.setHeaderType(RTMPHeader.Type.MEDIUM);
            }
            final int deltaTime = header.getTime() - prevHeader.getTime();
            if(deltaTime < 0){
                LoggerUtil.warn(LOG, "negative time: {}", header);
                header.setDeltaTime(0);
            }else{
                header.setDeltaTime(deltaTime);
            }
        }
        channelPrevHeaders[channelId] = header;
        LoggerUtil.debug(LOG, ">> {}", msg);
        out = Unpooled.buffer(RTMPHeader.MAX_ENCODE_SIZE + header.getSize() + header.getSize() / chunkSize);
        boolean first = true;
        while (in.isReadable()){
            final int size = Math.min(chunkSize, in.readableBytes());
            if(first){
                header.encode(out);
                first = false;
            }else{
                out.writeBytes(header.getTinyHeader());
            }
            in.readBytes(out, size);
        }
    }

    private void clearPrevHeaders(){
        LoggerUtil.debug(LOG, "clear prev stream headers");
        channelPrevHeaders = new RTMPHeader[RTMPHeader.MAX_CHANNEL_ID];
    }
}
