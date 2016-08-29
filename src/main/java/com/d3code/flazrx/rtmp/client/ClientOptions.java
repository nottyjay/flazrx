package com.d3code.flazrx.rtmp.client;

import com.d3code.flazrx.rtmp.RTMPReader;
import com.d3code.flazrx.rtmp.RTMPWriter;
import com.d3code.flazrx.rtmp.server.ServerStream;
import com.d3code.flazrx.util.LoggerUtil;
import com.d3code.flazrx.util.Utils;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
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
        options.addOption(new Option("help", "print this message"));
        options.addOption(OptionBuilder.withArgName("host").hasArg()
                .withDescription("host name").create("host"));
        options.addOption(OptionBuilder.withArgName("port").hasArg()
                .withDescription("port number").create("port"));
        options.addOption(OptionBuilder.withArgName("app").hasArg()
                .withDescription("app name").create("app"));
        options.addOption(OptionBuilder
                .withArgName("start").hasArg()
                .withDescription("start position (milliseconds)").create("start"));
        options.addOption(OptionBuilder.withArgName("length").hasArg()
                .withDescription("length (milliseconds)").create("length"));
        options.addOption(OptionBuilder.withArgName("buffer").hasArg()
                .withDescription("buffer duration (milliseconds)").create("buffer"));
        options.addOption(new Option("rtmpe", "use RTMPE (encryption)"));
        options.addOption(new Option("live", "publish local file to server in 'live' mode"));
        options.addOption(new Option("record", "publish local file to server in 'record' mode"));
        options.addOption(new Option("append", "publish local file to server in 'append' mode"));
        options.addOption(OptionBuilder.withArgName("property=value").hasArgs(2)
                .withValueSeparator().withDescription("add / over-ride connection param").create("D"));
        options.addOption(OptionBuilder.withArgName("swf").hasArg()
                .withDescription("path to (decompressed) SWF for verification").create("swf"));
        options.addOption(OptionBuilder.withArgName("version").hasArg()
                .withDescription("client version to use in RTMP handshake (hex)").create("version"));
        options.addOption(OptionBuilder.withArgName("load").hasArg()
                .withDescription("no. of client connections (server load testing)").create("load"));
        options.addOption(OptionBuilder.withArgName("loop").hasArg()
                .withDescription("for publish mode, loop count").create("loop"));
        options.addOption(OptionBuilder.withArgName("threads").hasArg()
                .withDescription("for load testing (load) mode, thread pool size").create("threads"));
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

    public String getTcUrl(){
        return (rtmpe ? "rtmpe://" : "rtmp://") + host + ":" + port + "/" + appName;
    }

    public void initSwfVerification(String pathToLocalSwfFile){
        initSwfVerification(new File(pathToLocalSwfFile));
    }

    public void initSwfVerification(File localSwfFile){
        LoggerUtil.info(LOG, "initializing swf verification data for: " + localSwfFile.getAbsolutePath());
        byte[] bytes = Utils.readAsByteArray(localSwfFile);
//        byte[] hash = Utils.sha256(bytes, RTMPHand)
        swfSize = bytes.length;
        swfHash = hash;
    }
}
