package com.d3code.flazrx.rtmp;

import com.d3code.flazrx.rtmp.message.Metadata;

/**
 * Created by Nottyjay on 2016/8/24.
 */
public interface RTMPReader {

    Metadata getMetadata();

    RTMPMessage[] getStartMessages();

    void setAggregateDuration(int targetDuration);

    long getTimePosition();

    long seek(long timePosition);

    void close();

    boolean hasNext();

    RTMPMessage next();
}
