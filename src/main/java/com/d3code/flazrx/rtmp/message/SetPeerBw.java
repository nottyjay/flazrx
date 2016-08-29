package com.d3code.flazrx.rtmp.message;

import com.d3code.flazrx.rtmp.RTMPHeader;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * Created by Nottyjay on 2016/8/29.
 */
public class SetPeerBw extends AbstractMessage {

    public static enum LimitType{
        HARD, SOFT, DYNAMIC
    }

    private int value;
    private LimitType limitType;

    public SetPeerBw(final RTMPHeader header, final ByteBuf in){
        super(header, in);
    }

    public SetPeerBw(int value, LimitType limitType){
        this.value = value;
        this.limitType = limitType;
    }

    public static SetPeerBw dynamic(int value){
        return new SetPeerBw(value, LimitType.DYNAMIC);
    }

    public static SetPeerBw hard(int value){
        return new SetPeerBw(value, LimitType.HARD);
    }

    public int getValue(){
        return value;
    }

    @Override
    public ByteBuf encode() {
        ByteBuf out = Unpooled.buffer(5);
        out.writeInt(value);
        out.writeByte((byte)limitType.ordinal());
        return out;
    }

    @Override
    public void decode(ByteBuf in) {
        value = in.readInt();
        limitType = LimitType.values()[in.readByte()];
    }

    @Override
    MessageType getMessageType() {
        return MessageType.SET_PEER_BW;
    }
}
