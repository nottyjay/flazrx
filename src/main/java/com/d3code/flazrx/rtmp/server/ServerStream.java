package com.d3code.flazrx.rtmp.server;

import com.d3code.flazrx.rtmp.RTMPMessage;
import com.d3code.flazrx.util.Utils;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nottyjay on 2016/8/24.
 */
public class ServerStream {

    public static enum PublishType{
        LIVE, APPEND, RECORD;

        public String asString(){
            return this.name().toLowerCase();
        }

        public static PublishType parse(final String raw){
            return PublishType.valueOf(raw.toUpperCase());
        }
    }

    private final String name;
    private final PublishType publishType;
    private final ChannelGroup subscribers;
    private final List<RTMPMessage> configMessages;

    private Channel publisher;

    public ServerStream(final String rawName, final String typeString){
        this.name = Utils.trimSlashes(rawName).toLowerCase();
        if(typeString != null){
            this.publishType = PublishType.parse(typeString);
            subscribers = new DefaultChannelGroup(name, GlobalEventExecutor.INSTANCE);
            configMessages = new ArrayList<RTMPMessage>();
        }else{
            this.publishType = null;
            subscribers = null;
            configMessages = null;
        }
    }

    public boolean isLive(){
        return publishType != null && publishType == PublishType.LIVE;
    }

    public ChannelGroup getSubscribers(){
        return subscribers;
    }

    public String getName(){
        return name;
    }

    public List<RTMPMessage> getConfigMessages(){
        return configMessages;
    }

    public void addConfigMessage(final RTMPMessage message){
        configMessages.add(message);
    }

    public void setPublisher(Channel publisher){
        this.publisher = publisher;
        configMessages.clear();
    }

    public Channel getPublisher() {
        return publisher;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("[name: '").append(name);
        sb.append("' type: ").append(publishType);
        sb.append(" publisher: ").append(publisher);
        sb.append(" subscribers: ").append(subscribers);
        sb.append(" config: ").append(configMessages);
        sb.append(']');
        return sb.toString();
    }
}
