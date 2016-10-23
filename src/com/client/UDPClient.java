package com.client;

import com.utility.Operation;
import com.utility.DataPacket;
import com.utility.OperationUtils;
import com.uw.adc.rmi.model.Stats;
import com.uw.adc.rmi.util.Constants;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.uw.adc.rmi.util.Constants.LOGGER;

/**
 * Created by Anurita on 10/16/2016.
 */
public class UDPClient {

    public static final int MAX_RETRIES = 3;

    private List<Stats> statsList = new ArrayList<Stats>();
    Date beforeDate = null;
    Date afterDate = null;

    public void encodePacket(String packetString, String host, String port) {
        beforeDate = new Date();
        String operation = packetString.split(OperationUtils.SEPARATOR)[0];
        String data = extractData(packetString);
        InetAddress destAddr = null;
        int destPort;
        DataPacket dataPacket = null;
        DatagramSocket clientSocket = null;
        Operation op = Operation.fromValue(operation.toUpperCase());
        try {
            switch (op) {
                case PUT:
                    dataPacket = new DataPacket(data);
                    dataPacket.setOperation(Operation.PUT);
                    dataPacket.setResponse(Boolean.TRUE);
                    break;
                case GET:
                    dataPacket = new DataPacket(data);
                    dataPacket.setOperation(Operation.GET);
                    dataPacket.setResponse(Boolean.TRUE);
                    break;
                case DELETE:
                    dataPacket = new DataPacket(data);
                    dataPacket.setOperation(Operation.DELETE);
                    dataPacket.setResponse(Boolean.TRUE);
                    break;
                default:
                    dataPacket = new DataPacket("");
                    dataPacket.setOperation(Operation.OTHER);
                    dataPacket.setResponse(Boolean.FALSE);
                    break;
            }
            destAddr = InetAddress.getByName(host);
            destPort = Integer.parseInt(port);
            clientSocket = new DatagramSocket();
            clientSocket.setSoTimeout(5000);
            int currentAttempt = 0;
            boolean completed = false;
            while (currentAttempt <= MAX_RETRIES && !completed) {
                if (currentAttempt > 0) {
                    String msg = String.format("Retry attempt #%d for request: %s", currentAttempt, dataPacket);
                    LOGGER.info(msg);
                    System.out.println(msg);
                }
                OperationUtils.sendPacket(dataPacket, destAddr, destPort, clientSocket);
                try {
                    waitForAcknowledgement(clientSocket);
                    completed = true;
                } catch (Exception ex) {
                    String msg = "Server timed-out for request: " + dataPacket;
                    LOGGER.error(msg);
                    System.out.println(msg);
                    currentAttempt++;
                }
            }
            if (!completed) {
                String msg = "Server timed-out even after 3 retries. Could not complete request: " + dataPacket;
                LOGGER.error(msg);
                System.out.println(msg);
            }
        } catch (Exception e) {
            Constants.LOGGER.error(e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Extract the key value paper from a given row
     * @param packetString
     * @return
     */
    private String extractData(String packetString) {

        String data = Arrays.stream(packetString.split(","))
                .skip(1)
                .map(s -> s.trim())
                .collect(Collectors.joining(","));
        // String data = packetString.substring(packetString.indexOf(",") + 1);
        return data;
    }

    /**
     * Client will wait for acknowledgement
     * @param clientSocket
     * @throws Exception
     */
    private void waitForAcknowledgement(DatagramSocket clientSocket) throws Exception {
        byte[] receiveData = new byte[1024];
        DatagramPacket datagramPacket = new DatagramPacket(receiveData, receiveData.length);
        clientSocket.receive(datagramPacket);
        receiveData = datagramPacket.getData();
        DataPacket dataPacket = OperationUtils.deserialize(OperationUtils.uncompress(receiveData));
        System.out.println(dataPacket.getData());
        afterDate = new Date();
        long time = afterDate.getTime() - beforeDate.getTime();
        Stats currentStats = new Stats(dataPacket.getOperation().toString(), time);
        statsList.add(currentStats);
    }

    /**
     * Get statistics for computing average and standard deviation
     * @return
     */
    public List<Stats> getStatsList() {
        return statsList;
    }
}
