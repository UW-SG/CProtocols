package com.main;

import com.server.UDPServer;
import com.utility.UDPConstants;

class UDPServerMain {
    public static void main(String args[]) {
        try {
            UDPServer udpServer = new UDPServer(args[0]);
            udpServer.start();

        } catch (Exception e) {
            UDPConstants.UDP_SERVER_LOGGER.error(e);
            throw new RuntimeException(e);
        }
    }
}
