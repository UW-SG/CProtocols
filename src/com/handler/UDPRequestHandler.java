package com.handler;

import com.utility.DataPacket;
import com.utility.DataStore;
import com.utility.Operation;
import com.utility.OperationUtils;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.uw.adc.rmi.util.Constants.UDP_SERVER_LOGGER;

public class UDPRequestHandler implements RequestHandler {
    private InetAddress clientAddr;
    private DatagramSocket serverSocket;
    private Integer clientPort;
    private DataStore dataStore;
    private DataPacket responsePacket;


    public UDPRequestHandler(InetAddress clientAddr,
                             Integer clientPort,
                             DatagramSocket serverSocket,
                             DataStore dataStore) {

        this.clientAddr = clientAddr;
        this.serverSocket = serverSocket;
        this.dataStore = dataStore;
        this.clientPort = clientPort;
    }

    /**
     * Handle GET
     *
     * @param dataPacket
     */
    @Override
    public void handleGet(DataPacket dataPacket) {
        String data = dataPacket.getData();
        String key = data.split(OperationUtils.SEPARATOR)[0].trim();
        responsePacket = dataStore.getValue(key);
        UDP_SERVER_LOGGER.info(String.format("%s : Received request from : %s : %s to do %s ( %s )",
                new SimpleDateFormat("yyyy/MM/dd: HH:mm:ss.SSS").format((new Date()).getTime())
                , this.getClientAddr().toString(), (this.getClientPort()).toString(),
                responsePacket.getOperation().toString(), responsePacket.getData()));
        OperationUtils.sendPacket(responsePacket, this.clientAddr, this.clientPort, this.serverSocket);

    }

    /**
     * Handle PUT
     *
     * @param dataPacket
     */
    @Override
    public void handlePut(DataPacket dataPacket) {
        String data = dataPacket.getData();
        responsePacket = dataStore.putValue(data.split(OperationUtils.SEPARATOR)[0].trim(),
                data.split(OperationUtils.SEPARATOR)[1].trim());

        OperationUtils.sendPacket(responsePacket, this.clientAddr, this.clientPort, this.serverSocket);

    }

    /**
     * Handle DELETE
     *
     * @param dataPacket
     */
    @Override
    public void handleDelete(DataPacket dataPacket) {
        String key = dataPacket.getData();
        responsePacket = dataStore.deleteValue(key);
        OperationUtils.sendPacket(responsePacket, this.clientAddr, this.clientPort, this.serverSocket);

    }

    /**
     * Handle Malformed packet
     *
     * @param dataPacket
     */
    @Override
    public void handleMalformed(DataPacket dataPacket) {
        responsePacket = new DataPacket("Received malformed packet");
        responsePacket.setOperation(Operation.OTHER);
        OperationUtils.sendPacket(responsePacket, this.clientAddr, this.clientPort, this.serverSocket);
    }


    public InetAddress getClientAddr() {
        return clientAddr;
    }

    public void setClientAddr(InetAddress clientAddr) {
        this.clientAddr = clientAddr;
    }

    public DatagramSocket getServerSocket() {
        return serverSocket;
    }

    public void setServerSocket(DatagramSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public Integer getClientPort() {
        return clientPort;
    }

    public void setClientPort(Integer clientPort) {
        this.clientPort = clientPort;
    }

    public DataStore getDataStore() {
        return dataStore;
    }

    public void setDataStore(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    public DataPacket getResponsePacket() {
        return responsePacket;
    }

    public void setResponsePacket(DataPacket responsePacket) {
        this.responsePacket = responsePacket;
    }
}
