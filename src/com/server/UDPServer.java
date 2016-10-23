package com.server;

import com.handler.RequestHandler;
import com.handler.UDPRequestHandler;
import com.utility.DataPacket;
import com.utility.DataStore;
import com.utility.OperationUtils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import static com.uw.adc.rmi.util.Constants.UDP_SERVER_LOGGER;

/**
 * Created by Anurita on 10/16/2016.
 */
public class UDPServer {

    private final DatagramSocket udpServerSocket;
    private final DataStore dataStore;

    public UDPServer(String port) {
        dataStore = new DataStore();
        try {
            // udpServerSocket = new DatagramSocket(Integer.parseInt(port));
            udpServerSocket = new DatagramSocket(null);
            InetSocketAddress inetSocketAddress = new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(),
                    Integer.parseInt(port));
            udpServerSocket.bind(inetSocketAddress);
        } catch (Exception e) {
            UDP_SERVER_LOGGER.error(e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Start the server
     */
    public void start() {
        try {
            while (true) {
                byte input[] = new byte[1024];
                DatagramPacket inputPacket = new DatagramPacket(input, input.length);
                System.out.println("Waiting for client");
                udpServerSocket.receive(inputPacket);
                new Thread(() -> {
                    //System.out.println("Thread id is : " + Thread.currentThread().getId());
                    processRequest(inputPacket);
                }).start();
            }
        } catch (Exception e) {
            UDP_SERVER_LOGGER.error(e);
            throw new RuntimeException(e);
        } finally {
            udpServerSocket.close();
        }
    }

    /**
     * Deserialize the packet at server end and invoke appropriate PUT/GET/DELETE method
     *
     * @param inputPacket
     */
    private void processRequest(DatagramPacket inputPacket) {
        try {
            byte inBlock[] = inputPacket.getData();
            DataPacket dataPacket = OperationUtils.deserialize(OperationUtils.uncompress(inBlock));
            //String inputRequest = new String(inBlock, 0, inBlock.length);
            RequestHandler handler = new UDPRequestHandler(inputPacket.getAddress(),
                    inputPacket.getPort(),
                    udpServerSocket,
                    dataStore);
            OperationUtils.perform((UDPRequestHandler) handler, dataPacket, inputPacket);
        } catch (Exception e) {
            UDP_SERVER_LOGGER.error(e);
            throw new RuntimeException(e);
        }
    }
}
