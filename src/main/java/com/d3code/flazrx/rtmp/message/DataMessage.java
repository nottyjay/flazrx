package com.d3code.flazrx.rtmp.message;

import com.d3code.flazrx.rtmp.RTMPHeader;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;

/**
 * Created by Nottyjay on 2016/8/24.
 */
public abstract class DataMessage extends AbstractMessage{

    private boolean encoded;
    protected ByteBuf data;

    public DataMessage(){
        super();
    }

    public DataMessage(final RTMPHeader header, final ByteBuf in){
        super(header, in);
    }

    public DataMessage(final int time, final ByteBuf in){
        header.setTime(time);
        header.setSize(in.readableBytes());
        data = in;
    }

    @Override
    public ByteBuf encode() {
        if(encoded){
            data.resetReaderIndex();
        }else{
            encoded = true;
        }
        return data;
    }

    @Override
    public void decode(ByteBuf in) {
        data = in;
    }

    @Override
    public String toString() {
        return super.toString() + ByteBufUtil.hexDump(data);
    }

    public abstract boolean isConfig();
}
