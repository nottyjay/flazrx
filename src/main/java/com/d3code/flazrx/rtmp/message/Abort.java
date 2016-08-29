package com.d3code.flazrx.rtmp.message;

import com.d3code.flazrx.rtmp.RTMPHeader;
import io.netty.buffer.*;

/**
 * Created by Nottyjay on 2016/8/24.
 */
public class Abort extends AbstractMessage {

    private int streamId;

    public Abort(final int streamId){
        this.streamId = streamId;
    }

    public Abort(final RTMPHeader header, final ByteBuf in){
        super(header, in);
    }

    public int getStreamId(){
        return streamId;
    }

    @Override
    public ByteBuf encode() {
        final ByteBuf out = Unpooled.buffer(4);
        out.writeInt(streamId);
        return out;
    }

    @Override
    public void decode(ByteBuf in) {
        streamId = in.readInt();
    }

    @Override
    MessageType getMessageType() {
        return MessageType.ABORT;
    }
}
