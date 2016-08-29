package com.d3code.flazrx.rtmp;

import com.d3code.flazrx.rtmp.message.ChunkSize;
import com.d3code.flazrx.rtmp.message.MessageType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by Nottyjay on 2016/8/23.
 */
public class RTMPDecoder extends ReplayingDecoder<RTMPDecoder.DecoderState> {

    private static final Logger LOG = LoggerFactory.getLogger(RTMPDecoder.class);

    public static enum DecoderState{
        GET_HEADER,
        GET_PAYLOAD
    }

    public RTMPDecoder(){
        super(DecoderState.GET_HEADER);
    }

    private RTMPHeader header;
    private int channelId;
    private ByteBuf payload;
    private int chunkSize = 128;

    private final RTMPHeader[] incompleteHeaders = new RTMPHeader[RTMPHeader.MAX_CHANNEL_ID];
    private final ByteBuf[] incompletePayloads = new ByteBuf[RTMPHeader.MAX_CHANNEL_ID];
    private final RTMPHeader[] completeHeaders = new RTMPHeader[RTMPHeader.MAX_CHANNEL_ID];

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        switch (state()){
            case GET_HEADER:
                header = new RTMPHeader(byteBuf, incompleteHeaders);
                channelId = header.getChannelId();
                if(incompleteHeaders[channelId] == null){// new chunk stream
                    incompleteHeaders[channelId] = header;
                    incompletePayloads[channelId] = Unpooled.buffer(header.getSize());
                }
                payload = incompletePayloads[channelId];
                checkpoint(DecoderState.GET_PAYLOAD);
            case GET_PAYLOAD:
                final byte[] bytes = new byte[Math.min(payload.writableBytes(), chunkSize)];
                byteBuf.readBytes(bytes);
                payload.writeBytes(bytes);
                checkpoint(DecoderState.GET_HEADER);
                if(payload.isWritable()){// more chunks remain
                    return;
                }
                incompletePayloads[channelId] = null;
                final RTMPHeader prevHeader = completeHeaders[channelId];
                if(!header.isLarge()){
                    header.setTime(prevHeader.getTime() + header.getDeltaTime());
                }
                final RTMPMessage message = MessageType.decode(header, payload);
                if(LOG.isDebugEnabled()){
                    LOG.debug("<< {}", message);
                }
                payload = null;
                if(header.isChunkSize()){
                    final ChunkSize csMessage = (ChunkSize) message;
                    LOG.debug("decoder new chunk size: {}", csMessage);
                    chunkSize = csMessage.getChunkSize();
                }
                completeHeaders[channelId] = header;
                list.add(message);
            default:
                throw new RuntimeException("unexpected decoder state: " + state());
        }
    }
}
