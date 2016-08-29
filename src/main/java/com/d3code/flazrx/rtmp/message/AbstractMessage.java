package com.d3code.flazrx.rtmp.message;

import com.d3code.flazrx.amf.Amf0Object;
import com.d3code.flazrx.rtmp.RTMPHeader;
import com.d3code.flazrx.rtmp.RTMPMessage;
import io.netty.buffer.ByteBuf;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Nottyjay on 2016/8/24.
 */
public abstract class AbstractMessage implements RTMPMessage {

    protected final RTMPHeader header;

    public AbstractMessage(){
        header = new RTMPHeader(getMessageType());
    }

    public AbstractMessage(RTMPHeader header, ByteBuf in){
        this.header = header;
        decode(in);
    }

    public RTMPHeader getHeader(){
        return header;
    }

    abstract MessageType getMessageType();

    @Override
    public String toString(){
        return header.toString() + ' ';
    }

    public static Amf0Object object(Amf0Object object, Pair... pairs){
        if(pairs != null){
            for(Pair pair : pairs){
                object.put(pair.name, pair.value);
            }
        }
        return object;
    }

    public static Amf0Object object(Pair... pairs){
        return object(new Amf0Object(), pairs);
    }

    public static Map<String, Object> map(Map<String, Object> map, Pair... pairs){
        if(pairs != null){
            for(Pair pair : pairs){
                map.put(pair.name, pair.value);
            }
        }
        return map;
    }

    public static Map<String, Object> map(Pair... pairs){
        return map(new LinkedHashMap<String, Object>(), pairs);
    }

    public static class Pair{
        String name;
        Object value;
    }

    public static Pair pair(String name, Object value){
        Pair pair = new Pair();
        pair.name = name;
        pair.value = value;
        return pair;
    }
}
