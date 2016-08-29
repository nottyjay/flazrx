package com.d3code.flazrx.rtmp.message;

import com.d3code.flazrx.rtmp.RTMPHeader;
import io.netty.buffer.ByteBuf;

import java.util.Map;

/**
 * Created by Nottyjay on 2016/8/24.
 */
public abstract class Metadata extends AbstractMessage{

    protected String name;
    protected Object[] data;

    public Metadata(String name, Object... data){
        this.name = name;
        this.data = data;
    }

    public Metadata(RTMPHeader header, ByteBuf in){
        super(header, in);
    }

    public Object getData(int index){
        if(data == null || data.length < index + 1){
            return null;
        }
        return data[index];
    }

    public Object getValue(String key){
        final Map<String, Object> map = getMap(0);
        if(map == null){
            return null;
        }
        return map.get(key);
    }

    public Map<String, Object> getMap(int index){
        return (Map<String, Object>) getData(index);
    }

    public String getString(String key){
        return (String)getValue(key);
    }

    public Boolean getBoolean(String key){
        return (Boolean)getValue(key);
    }

    public Double getDouble(String key){
        return (Double) getValue(key);
    }

    public double getDuration(){
        if(data == null || data.length == 0){
            return -1;
        }
        final Map<String, Object> map = getMap(0);
        if(map == null){
            return -1;
        }
        final Object o = map.get("duration");
        if(o == null){
            return -1;
        }
        return ((Double) o).longValue();
    }

    public void setDuration(final double duration){
        if(data == null || data.length == 0){
            data = new Object[]{map(pair("duration", duration))};
        }
        final Object meta = data[0];
        final Map<String, Object> map = (Map) meta;
        if(map == null){
            data[0] = map(pair("duration", duration));
            return;
        }
        map.put("duration", duration);
    }

    public static Metadata onPlayStatus(double duration, double bytes){
        Map<String, Object> map = Command.onStatus(Command.OnStatus.STATUS, "NetStream.Play.Complete", pair("duration", duration), pair("bytes", bytes));
        return new Me
    }


}
