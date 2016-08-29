package com.d3code.flazrx.rtmp.message;

import com.d3code.flazrx.amf.Amf0Value;
import com.d3code.flazrx.rtmp.RTMPHeader;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nottyjay on 2016/8/24.
 */
public class MetadataAmf0 extends Metadata{

    public MetadataAmf0(String name, Object... data){
        super(name, data);
    }

    public MetadataAmf0(RTMPHeader header, ByteBuf in){
        super(header, in);
    }

    @Override
    MessageType getMessageType(){
        return MessageType.METADATA_AMF0;
    }

    @Override
    public ByteBuf encode(){
        ByteBuf out = Unpooled.directBuffer();
        Amf0Value.encode(out, name);
        Amf0Value.encode(out, data);
        return out;
    }

    @Override
    public void decode(ByteBuf in) {
        name = (String) Amf0Value.decode(in);
        List<Object> list = new ArrayList<Object>();
        while(in.isReadable()){
            list.add(Amf0Value.decode(in));
        }
        data = list.toArray();
    }
}
