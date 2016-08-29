package com.d3code.flazrx.util;

import org.slf4j.Logger;

/**
 * Created by Nottyjay on 2016/8/29.
 */
public class LoggerUtil {

    public static void debug(Logger logger, String message){
        if(logger.isDebugEnabled()){
            logger.debug(message);
        }
    }

    public static void debug(Logger logger, String format, Object... arg){
        if(logger.isDebugEnabled()){
            logger.debug(format, arg);
        }
    }

    public static void info(Logger logger, String message){
        if(logger.isInfoEnabled()){
            logger.info(message);
        }
    }

    public static void info(Logger logger, String format, Object... args){
        if(logger.isInfoEnabled()){
            logger.info(format, args);
        }
    }
}
