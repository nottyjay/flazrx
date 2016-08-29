package com.d3code.flazrx.rtmp;

import io.netty.buffer.ByteBuf;

/**
 * Created by Nottyjay on 2016/8/24.
 */
public interface RTMPMessage {

    RTMPHeader getHeader();

    ByteBuf encode();

    void decode(ByteBuf in);
}
