package com.d3code.flazrx.rtmp.message;

import com.d3code.flazrx.amf.Amf0Object;
import com.d3code.flazrx.amf.Amf0Value;
import com.d3code.flazrx.rtmp.RTMPHeader;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nottyjay on 2016/8/30.
 */
public class CommandAmf0 extends Command{

    public CommandAmf0(final RTMPHeader header, final ByteBuf in){
        super(header, in);
    }

    public CommandAmf0(int transactionId, String name, Amf0Object object, Object... args){
        super(transactionId, name, object, args);
    }

    public CommandAmf0(String name, Amf0Object object, Object... args){
        super(name, object, args);
    }

    @Override
    public ByteBuf encode() {
        ByteBuf out = Unpooled.directBuffer();
        Amf0Value.encode(out, name, transactionId, object);
        if(args != null){
            for(Object o : args){
                Amf0Value.encode(out, o);
            }
        }
        return out;
    }

    @Override
    public void decode(ByteBuf in) {
        name = (String) Amf0Value.decode(in);
        transactionId = ((Double)Amf0Value.decode(in)).intValue();
        object = (Amf0Object) Amf0Value.decode(in);
        List<Object> list = new ArrayList<Object>();
        while(in.isReadable()){
            list.add(Amf0Value.decode(in));
        }
        args = list.toArray();
    }

    @Override
    MessageType getMessageType() {
        return null;
    }
}
