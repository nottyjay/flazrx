package com.d3code.flazrx.rtmp;

/**
 * Created by Nottyjay on 2016/8/29.
 */
public interface RTMPWriter {

    void write(RTMPMessage message);

    void close();
}
