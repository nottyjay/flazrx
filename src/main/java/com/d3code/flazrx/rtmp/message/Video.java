package com.d3code.flazrx.rtmp.message;

import com.d3code.flazrx.rtmp.RTMPHeader;
import com.d3code.flazrx.util.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * Created by Nottyjay on 2016/8/29.
 */
public class Video extends DataMessage {

    public Video(final RTMPHeader header, final ByteBuf in){
        super(header, in);
    }

    public Video(final byte[]... bytes){
        data = Unpooled.wrappedBuffer(bytes);
    }

    public Video(final int time, final byte[] prefix, final int compositionOffset, final byte[] videoData){
        header.setTime(time);
        data = Unpooled.wrappedBuffer(prefix, Utils.toInt24(compositionOffset), videoData);
        header .setSize(data.readableBytes());
    }

    public Video(final int time, final ByteBuf in){
        super(time, in);
    }

    public static Video empty(){
        Video empty = new Video();
        empty.data = Unpooled.wrappedBuffer(new byte[2]);
        return empty;
    }

    @Override
    MessageType getMessageType() {
        return MessageType.VIDEO;
    }

    @Override
    public boolean isConfig() {
        return data.readableBytes() > 3 && data.getInt(0) == 0x17000000;
    }
}
