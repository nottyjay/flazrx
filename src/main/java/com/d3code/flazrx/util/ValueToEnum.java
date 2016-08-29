package com.d3code.flazrx.util;

import java.util.Arrays;

/**
 * Created by Nottyjay on 2016/8/23.
 */
public class ValueToEnum<T extends Enum<T> & ValueToEnum.IntValue> {

    public static interface IntValue{
        int intValue();
    }

    private final Enum[] lookupArray;
    private final int maxIndex;

    public ValueToEnum(final T[] enumValues){
        final int[] lookupIndexes = new int[enumValues.length];
        for(int i = 0; i < enumValues.length; i++){
            lookupIndexes[i] = enumValues[i].intValue();
        }
        Arrays.sort(lookupIndexes);
        maxIndex = lookupIndexes[lookupIndexes.length - 1];
        lookupArray = new Enum[maxIndex + 1];
        for(final T t: enumValues){
            lookupArray[t.intValue()] = t;
        }
    }

    public T valueToEnum(final int i){
        final T t;
        try{
            t = (T) lookupArray[i];
        }catch (Exception e){
            throw new RuntimeException(getErrorLogMessage(i) + ", " + e);
        }
        if(t == null){
            throw new RuntimeException(getErrorLogMessage(i) + ", no match found in lookup");
        }
        return t;
    }

    private String getErrorLogMessage(final int i){
        return "bad value / byte: " + i + " (hex: " + Utils.toHex((byte) i) + ")";
    }
}
