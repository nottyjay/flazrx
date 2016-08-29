package com.d3code.flazrx.rtmp.message;

import com.d3code.flazrx.rtmp.RTMPHeader;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * Created by Nottyjay on 2016/8/29.
 */
public class WindowAckSize extends AbstractMessage {

    private int value;

    public WindowAckSize(final RTMPHeader header, final ByteBuf in){
        super(header, in);
    }

    public WindowAckSize(int value){
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public ByteBuf encode() {
        ByteBuf out = Unpooled.buffer(4);
        out.writeInt(value);
        return out;
    }

    @Override
    public void decode(ByteBuf in) {
        value = in.readInt();
    }

    @Override
    MessageType getMessageType() {
        return MessageType.WINDOW_ACK_SIZE;
    }
}
