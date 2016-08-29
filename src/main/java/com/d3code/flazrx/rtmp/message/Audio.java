package com.d3code.flazrx.rtmp.message;

import com.d3code.flazrx.rtmp.RTMPHeader;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

/**
 * Created by Nottyjay on 2016/8/24.
 */
public class Audio extends DataMessage {

    public Audio(final RTMPHeader header, final ByteBuf in){
        super(header, in);
    }

    public Audio(final byte[] ...bytes){
        data = Unpooled.wrappedBuffer(bytes);
    }

    public Audio(final int time, final byte[] prefix, final byte[] audioData){
        header.setTime(time);
        data = Unpooled.wrappedBuffer(prefix, audioData);
        header.setSize(data.readableBytes());
    }

    public Audio(final int time, final ByteBuf in){
        super(time, in);
    }

    public static Audio empty(){
        Audio empty = new Audio();
        empty.data = Unpooled.EMPTY_BUFFER;
        return empty;
    }

    @Override
    MessageType getMessageType() {
        return MessageType.AUDIO;
    }

    @Override
    public boolean isConfig() {
        return data.readableBytes() > 3 && data.getInt(0) == 0xaf001310;
    }
}
