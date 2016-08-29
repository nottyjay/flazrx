package com.d3code.flazrx.rtmp.message;

import com.d3code.flazrx.amf.Amf0Object;
import com.d3code.flazrx.rtmp.RTMPHeader;
import com.d3code.flazrx.rtmp.client.ClientOptions;
import io.netty.buffer.ByteBuf;

/**
 * Created by Nottyjay on 2016/8/24.
 */
public class Command extends AbstractMessage {

    public static enum OnStatus{
        ERROR, STATUS, WARNING;

        public static OnStatus parse(final String raw){
            return OnStatus.valueOf(raw.substring(1).toUpperCase());
        }

        public String asString(){
            return "_" + this.name().toLowerCase();
        }
    }

    protected String name;
    protected int transactionId;
    protected Amf0Object object;
    protected Object[] args;

    public Command(RTMPHeader header, ByteBuf in){
        super(header, in);
    }

    public Command(int transactionId, String name, Amf0Object object, Object... args){
        this.transactionId = transactionId;
        this.name = name;
        this.object = object;
        this.args = args;
    }

    public Command(String name, Amf0Object object, Object... args){
        this(0, name, object, args);
    }

    public Amf0Object getObject(){
        return object;
    }

    public Object getArg(int index){
        return args[index];
    }

    public int getArgCount(){
        if(args == null){
            return 0;
        }
        return args.length;
    }

    private static Amf0Object onStatus(final OnStatus level, final String code, final String description, final String details, final Pair... pairs){
        final Amf0Object object = object(pair("level", level.asString()), pair("code", code));
        if(description != null){
            object.put("description", description);
        }
        if(details != null){
            object.put("details", details);
        }
        return object(object, pairs);
    }

    private static Amf0Object onStatus(final OnStatus level, final String code, final String description, final Pair... pairs){
        return onStatus(level, code, description, null, pairs);
    }

    public static Amf0Object onStatus(final OnStatus level, final String code, final Pair... pairs){
        return onStatus(level, code, null, null, pairs);
    }

    public static Command connect(ClientOptions options){
        Amf0Object object = object(pair("app", options.getAppName()),
                );
    }

}
