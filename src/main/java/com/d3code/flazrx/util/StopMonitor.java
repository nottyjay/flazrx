package com.d3code.flazrx.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Nottyjay on 2016/8/23.
 */
public class StopMonitor extends Thread {

    private static final Logger LOG = LoggerFactory.getLogger(StopMonitor.class);
    private ServerSocket socket;

    public StopMonitor(int port) {
        setDaemon(true);
        setName("StopMonitor");
        try {
            socket = new ServerSocket(port, 1, InetAddress.getByName("127.0.0.1"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        LOG.info("Stop monitor thread listening on: {}", socket);
        Socket accept;
        try {
            accept = socket.accept();
            BufferedReader reader = new BufferedReader(new InputStreamReader(accept.getInputStream()));
            reader.readLine();
            LOG.info("Stop signal received, stopping server");
            accept.close();
            socket.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
