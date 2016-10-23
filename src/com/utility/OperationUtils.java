package com.utility;

import com.handler.UDPRequestHandler;
import com.uw.adc.rmi.util.Constants;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * Created by Anurita on 10/18/2016.
 */
public class OperationUtils {

    public static final String SEPARATOR = ",";
    public static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd: HH:mm:ss.SSS");
    public static void perform(UDPRequestHandler handler, DataPacket dataPacket, DatagramPacket inputPacket) {
        Operation op = dataPacket.getOperation();
        switch (op) {

            case PUT:
                Constants.UDP_SERVER_LOGGER.info(String.format("%s : Received request from : %s : %s to do %s ( %s )",
                        simpleDateFormat.format((new Date()).getTime())
                        , handler.getClientAddr().toString(), (handler.getClientPort()).toString(),
                        op.toString(), dataPacket.getData()));
                handler.handlePut(dataPacket);
                break;
            case GET:
                Constants.UDP_SERVER_LOGGER.info(String.format("%s : Received request from : %s : %s to do %s ( %s )",
                        simpleDateFormat.format((new Date()).getTime())
                        , handler.getClientAddr().toString(),
                        (handler.getClientPort()).toString(),
                        op.toString(),
                        dataPacket.getData()));
                handler.handleGet(dataPacket);
                break;
            case DELETE:
                Constants.UDP_SERVER_LOGGER.info(String.format("%s : Received request from : %s : %s to do %s ( %s )",
                        simpleDateFormat.format((new Date()).getTime())
                        , handler.getClientAddr().toString(), (handler.getClientPort()).toString(),
                        op.toString(), dataPacket.getData()));
                handler.handleDelete(dataPacket);
                break;
            case OTHER:
                Constants.UDP_SERVER_LOGGER.info(String.format("%s : Received unsolicited request from : %s : %s "+
                                "of length %d",
                        simpleDateFormat.format((new Date()).getTime())
                        , handler.getClientAddr().toString(), (handler.getClientPort()).toString(),
                         inputPacket.getLength()));
                handler.handleMalformed(dataPacket);
                break;
        }
    }

    /**
     * Serialization of data packet
     *
     * @param packet
     * @return
     * @throws IOException
     */
    public static byte[] serialize(DataPacket packet) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = null;
        ObjectOutput objectOutput = null;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            objectOutput = new ObjectOutputStream(byteArrayOutputStream);
            objectOutput.writeObject(packet);
            objectOutput.flush();
            return byteArrayOutputStream.toByteArray();
        } finally {
            objectOutput.close();
            byteArrayOutputStream.close();

        }
    }

    /**
     * To compress the data packet after serializing
     *
     * @param bytes
     * @return
     */
    public static byte[] compress(byte[] bytes) {
        try {
            Deflater deflate = new Deflater();
            deflate.setLevel(Deflater.BEST_SPEED);
            deflate.setInput(bytes);
            deflate.finish();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] tempBuffer = new byte[8 * 1024];
            while (!deflate.finished()) {
                int size = deflate.deflate(tempBuffer);
                byteArrayOutputStream.write(tempBuffer, 0, size);
            }
            byteArrayOutputStream.close();
            return byteArrayOutputStream.toByteArray();
        } catch (Exception ex) {
            Constants.UDP_SERVER_LOGGER.error(ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Uncompress the data packet before deserializing
     *
     * @param bytes
     * @return
     */
    public static byte[] uncompress(byte[] bytes) {
        try {
            Inflater inflate = new Inflater();
            inflate.setInput(bytes);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] tempBuffer = new byte[8 * 1024];
            while (!inflate.finished()) {
                int size = inflate.inflate(tempBuffer);
                byteArrayOutputStream.write(tempBuffer, 0, size);
            }
            byteArrayOutputStream.close();
            return byteArrayOutputStream.toByteArray();
        } catch (Exception ex) {
            Constants.UDP_SERVER_LOGGER.error(ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Deserialization of data packet at the destination
     *
     * @param data
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static DataPacket deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
        ObjectInput objectInputStream = new ObjectInputStream(byteArrayInputStream);
        DataPacket dataPacket = (DataPacket) objectInputStream.readObject();
        return dataPacket;
    }

    /**
     * Send packet across network
     *
     * @param outputPacket
     * @param destAddr
     * @param destPort
     * @param sourceSocket
     */
    public static void sendPacket(DataPacket outputPacket, InetAddress destAddr,
                                  Integer destPort, DatagramSocket sourceSocket) {
        try {
            byte output[] = OperationUtils.compress(OperationUtils.serialize(outputPacket));
            DatagramPacket outputDatagramPacket = new DatagramPacket(output, output.length, destAddr, destPort);
            sourceSocket.send(outputDatagramPacket);
        } catch (Exception e) {
            Constants.UDP_SERVER_LOGGER.error(e);
            throw new RuntimeException(e);
        }
    }
}
