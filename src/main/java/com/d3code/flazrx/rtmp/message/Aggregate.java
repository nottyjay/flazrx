package com.d3code.flazrx.rtmp.message;

import com.d3code.flazrx.rtmp.RTMPHeader;
import io.netty.buffer.ByteBuf;

/**
 * Created by Nottyjay on 2016/8/24.
 */
public class Aggregate extends DataMessage {

    public Aggregate(RTMPHeader header, ByteBuf in){
        super(header, in);
    }

    public Aggregate(int time, ByteBuf in){
        super();
        header.setTime(time);
        data = in;
        header.setSize(data.readableBytes());
    }

    @Override
    MessageType getMessageType() {
        return MessageType.AGGREGATE;
    }

    @Override
    public boolean isConfig() {
        return false;
    }
}
