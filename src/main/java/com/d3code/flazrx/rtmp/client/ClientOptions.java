package com.d3code.flazrx.rtmp.client;

import com.d3code.flazrx.rtmp.RTMPHandshake;
import com.d3code.flazrx.rtmp.RTMPReader;
import com.d3code.flazrx.rtmp.RTMPWriter;
import com.d3code.flazrx.rtmp.server.ServerStream;
import com.d3code.flazrx.util.LoggerUtil;
import com.d3code.flazrx.util.Utils;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Nottyjay on 2016/8/24.
 */
public class ClientOptions {

    private static final Logger LOG = LoggerFactory.getLogger(ClientOptions.class);

    private ServerStream.PublishType publishType;
    private String host = "localhost";
    private int port = 1935;
    private String appName = "vod";
    private String streamName;
    private String fileToPublish;
    private RTMPReader readerToPublish;
    private RTMPWriter writerToSave;
    private String saveAs;
    private boolean rtmpe;
    private Map<String, Object> params;
    private Object[] args;
    private byte[] clientVersionToUse;
    private int start = -2;
    private int length = -1;
    private int buffer = 100;
    private byte[] swfHash;
    private int swfSize;
    private int load = 1;
    private int loop = 1;
    private int threads = 10;

    private static final Pattern URL_PATTERN = Pattern.compile("(rtmp.?)://([^/:]+)(:[0-9]+)?/([^/]+)(.*)");

    public ClientOptions(){}

    public ClientOptions(String host, String appName, String streamName, String saveAs){
        this(host, 1935, appName, streamName, saveAs, false, null);
    }

    public ClientOptions(String host, int port, String appName, String streamName, String saveAs, boolean rtmpe, String swfFile){
        this.host = host;
        this.port = port;
        this.appName = appName;
        this.streamName = streamName;
        this.saveAs = saveAs;
        this.rtmpe = rtmpe;
        if(swfFile != null){
            initSwfVerification(swfFile);
        }
    }

    public ClientOptions(String url, String saveAs){
        parseUrl(url);
        this.saveAs = saveAs;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getFileToPublish() {
        return fileToPublish;
    }

    public void setFileToPublish(String fileToPublish) {
        this.fileToPublish = fileToPublish;
    }

    public RTMPReader getReaderToPublish() {
        return readerToPublish;
    }

    public void setReaderToPublish(RTMPReader readerToPublish) {
        this.readerToPublish = readerToPublish;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object... args) {
        this.args = args;
    }

    public byte[] getClientVersionToUse() {
        return clientVersionToUse;
    }

    public void setClientVersionToUse(byte[] clientVersionToUse) {
        this.clientVersionToUse = clientVersionToUse;
    }

    public int getLoad() {
        return load;
    }

    public void setLoad(int load) {
        this.load = load;
    }

    public int getLoop() {
        return loop;
    }

    public void setLoop(int loop) {
        this.loop = loop;
    }

    public void setPublishType(ServerStream.PublishType publishType) {
        this.publishType = publishType;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getStreamName() {
        return streamName;
    }

    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }

    public RTMPWriter getWriterToSave() {
        return writerToSave;
    }

    public void setWriterToSave(RTMPWriter writerToSave) {
        this.writerToSave = writerToSave;
    }

    public String getSaveAs() {
        return saveAs;
    }

    public void setSaveAs(String saveAs) {
        this.saveAs = saveAs;
    }

    public boolean isRtmpe() {
        return rtmpe;
    }

    public void setRtmpe(boolean rtmpe) {
        this.rtmpe = rtmpe;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getBuffer() {
        return buffer;
    }

    public void setBuffer(int buffer) {
        this.buffer = buffer;
    }

    public byte[] getSwfHash() {
        return swfHash;
    }

    public void setSwfHash(byte[] swfHash) {
        this.swfHash = swfHash;
    }

    public int getSwfSize() {
        return swfSize;
    }

    public void setSwfSize(int swfSize) {
        this.swfSize = swfSize;
    }

    public int getThreads() {
        return threads;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    public Map<String, Object> getParams(){
        return params;
    }

    public void setParams(Map<String, Object> params){
        this.params = params;
    }

    public ServerStream.PublishType getPublishType() {
        return publishType;
    }

    public void parseUrl(String url){
        Matcher matcher = URL_PATTERN.matcher(url);
        if(!matcher.matches()){
            throw new RuntimeException("invalid url: " + url);
        }
        LoggerUtil.debug(LOG, "parsing url: {}", url);
        String protocol = matcher.group(1);
        LoggerUtil.debug(LOG, "protocol = '{}'", protocol);
        host = matcher.group(2);
        LoggerUtil.debug(LOG, "host = '{}'", host);
        String portString = matcher.group(3);
        if(portString == null){
            LoggerUtil.debug(LOG, "port is null in url, will use default 1935");
        }else{
            portString = portString.substring(1);// skip the ':'
            LoggerUtil.debug(LOG, "port = '{}'", portString);
        }
        port = portString == null ? 1935 : Integer.parseInt(portString);
        appName = matcher.group(4);
        LoggerUtil.debug(LOG, "app = '{}'", appName);
        streamName = matcher.group(5);
        LoggerUtil.debug(LOG, "playName = '{}'", streamName);
        rtmpe = protocol.equalsIgnoreCase("rtmpe");
        if(rtmpe){
            LoggerUtil.debug(LOG, "rtmpe requested, will use encryption");
        }
    }

    public void publishLive(){
        publishType = ServerStream.PublishType.LIVE;
    }

    public void publishRecord(){
        publishType = ServerStream.PublishType.RECORD;
    }

    public void publishAppend(){
        publishType = ServerStream.PublishType.APPEND;
    }

    protected static Options getCliOptions(){
        final Options options = new Options();
        options.addOption("help", "print this message");
        options.addOption(Option.builder("host").argName("host").hasArg().desc("host name").build());
        options.addOption(Option.builder("port").argName("port").hasArg().desc("port number").build());
        options.addOption(Option.builder("app").argName("app").hasArg().desc("app name").build());
        options.addOption(Option.builder("start").argName("start").hasArg().desc("start position (milliseconds)").build());
        options.addOption(Option.builder("length").argName("length").hasArg().desc("length (milliseconds)").build());
        options.addOption(Option.builder("buffer").argName("buffer").hasArg().desc("buffer duration (milliseconds)").build());
        options.addOption("rtmpe", "use RTMPE (encryption)");
        options.addOption("live", "publish local file to server in 'live' mode");
        options.addOption("record", "publish local file to server in 'record' mode");
        options.addOption("append", "publish local file to server in 'append' mode");
        options.addOption(Option.builder("D").argName("property=value").hasArgs().numberOfArgs(2).valueSeparator().desc("add / over-ride connection param").build());
        options.addOption(Option.builder("swf").argName("swf").hasArg().desc("path to (decompressed) SWF for verification").build());
        options.addOption(Option.builder("version").argName("version").hasArg().desc("client version to use in RTMP handshake (hex)").build());
        options.addOption(Option.builder("load").argName("load").hasArg().desc("no. of client connections (server load testing)").build());
        options.addOption(Option.builder("loop").argName("loop").hasArg().desc("for publish mode, loop count").build());
        options.addOption(Option.builder("threads").argName("threads").hasArg().desc("for load testing (load) mode, thread pool size").build());
        return options;
    }

    public boolean parseCli(final String[] args){
        CommandLineParser parser = new GnuParser();
        CommandLine line = null;
        final Options options = getCliOptions();
        try{
            line = parser.parse(options, args);
            if(line.hasOption("help") || line.getArgs().length == 0){
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("client [options] streaNameOrUrl [saveAs | fileToPublish]", options);
                return false;
            }
            if(line.hasOption("host")){
                host = line.getOptionValue("host");
            }
            if(line.hasOption("post")){
                port = Integer.valueOf(line.getOptionValue("port"));
            }
            if(line.hasOption("app")){
                appName = line.getOptionValue("app");
            }
            if(line.hasOption("start")) {
                start = Integer.valueOf(line.getOptionValue("start"));
            }
            if(line.hasOption("length")) {
                length = Integer.valueOf(line.getOptionValue("length"));
            }
            if(line.hasOption("buffer")) {
                buffer = Integer.valueOf(line.getOptionValue("buffer"));
            }
            if(line.hasOption("rtmpe")) {
                rtmpe = true;
            }
            if(line.hasOption("live")) {
                publishLive();
            }
            if(line.hasOption("record")) {
                publishRecord();
            }
            if(line.hasOption("append")) {
                publishAppend();
            }
            if(line.hasOption("version")) {
                clientVersionToUse = Utils.fromHex(line.getOptionValue("version"));
                if(clientVersionToUse.length != 4) {
                    throw new RuntimeException("client version to use has to be 4 bytes long");
                }
            }
            if(line.hasOption("D")) { // TODO integers, TODO extra args for 'play' command
                params = new HashMap(line.getOptionProperties("D"));
            }
            if(line.hasOption("load")) {
                load = Integer.valueOf(line.getOptionValue("load"));
                if(publishType != null && load > 1) {
                    throw new RuntimeException("cannot publish in load testing mode");
                }
            }
            if(line.hasOption("threads")) {
                threads = Integer.valueOf(line.getOptionValue("threads"));
            }
            if(line.hasOption("loop")) {
                loop = Integer.valueOf(line.getOptionValue("loop"));
                if(publishType == null && loop > 1) {
                    throw new RuntimeException("cannot loop when not in publish mode");
                }
            }
        }catch (Exception e){
            System.err.println("parsing failed: " + e.getMessage());
            return false;
        }
        String[] actualArgs = line.getArgs();
        Matcher matcher = URL_PATTERN.matcher(actualArgs[0]);
        if (matcher.matches()) {
            parseUrl(actualArgs[0]);
        } else {
            streamName = actualArgs[0];
        }
        if(publishType != null) {
            if(actualArgs.length < 2) {
                System.err.println("fileToPublish is required for publish mode");
                return false;
            }
            fileToPublish = actualArgs[1];
        } else if(actualArgs.length > 1) {
            saveAs = actualArgs[1];
        }
        LoggerUtil.info(LOG, "options: {}", this);
        return true;
    }

    public String getTcUrl(){
        return (rtmpe ? "rtmpe://" : "rtmp://") + host + ":" + port + "/" + appName;
    }

    public void initSwfVerification(String pathToLocalSwfFile){
        initSwfVerification(new File(pathToLocalSwfFile));
    }

    public void initSwfVerification(File localSwfFile){
        LoggerUtil.info(LOG, "initializing swf verification data for: " + localSwfFile.getAbsolutePath());
        byte[] bytes = Utils.readAsByteArray(localSwfFile);
        byte[] hash = Utils.sha256(bytes, RTMPHandshake.CLIENT_CONST);
        swfSize = bytes.length;
        swfHash = hash;
        LoggerUtil.info(LOG, "swf verification initialized - size: {}, hash: {}", swfSize, Utils.toHex(swfHash));
    }

    public void putParam(String key, Object value){
        if(params == null){
            params = new LinkedHashMap<String, Object>();
        }
        params.put(key, value);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[host: '").append(host);
        sb.append("' port: ").append(port);
        sb.append(" appName: '").append(appName);
        sb.append("' streamName: '").append(streamName);
        sb.append("' saveAs: '").append(saveAs);
        sb.append("' rtmpe: ").append(rtmpe);
        sb.append(" publish: ").append(publishType);
        if(clientVersionToUse != null) {
            sb.append(" clientVersionToUse: '").append(Utils.toHex(clientVersionToUse)).append('\'');
        }
        sb.append(" start: ").append(start);
        sb.append(" length: ").append(length);
        sb.append(" buffer: ").append(buffer);
        sb.append(" params: ").append(params);
        sb.append(" args: ").append(Arrays.toString(args));
        if(swfHash != null) {
            sb.append(" swfHash: '").append(Utils.toHex(swfHash));
            sb.append("' swfSize: ").append(swfSize).append('\'');
        }
        sb.append(" load: ").append(load);
        sb.append(" loop: ").append(loop);
        sb.append(" threads: ").append(threads);
        sb.append(']');
        return sb.toString();
    }
}
