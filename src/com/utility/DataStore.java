package com.utility;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Anurita on 10/16/2016.
 */
public class DataStore {

    private Map<String, String> dataMap = new ConcurrentHashMap<>();
    private DataPacket responsePacket = null;

    /**
     * Get value from key
     *
     * @param key
     * @return
     */
    public DataPacket getValue(String key) {

        String value = dataMap.get(key);
        responsePacket = new DataPacket(String.format("Packet retrieved Successfully for GET(key=%s) value=%s", key, value));
        responsePacket.setResponse(Boolean.TRUE);
        responsePacket.setOperation(Operation.GET);
        return responsePacket;
    }

    /**
     * Put key value in hashmap
     *
     * @param key
     * @param value
     * @return
     */
    public DataPacket putValue(String key, String value) {
        dataMap.put(key, value);
        responsePacket = new DataPacket("Packet Inserted Successfully for : PUT (" +
                key + "," + value + ")");
        responsePacket.setResponse(Boolean.TRUE);
        responsePacket.setOperation(Operation.PUT);
        return responsePacket;
    }

    /**
     * Delete value from hashmap
     *
     * @param key
     * @return
     */
    public DataPacket deleteValue(String key) {
        dataMap.remove(key);
        responsePacket = new DataPacket("Packet deleted Successfully for : DELETE (" +
                key + ")");
        responsePacket.setResponse(Boolean.TRUE);
        responsePacket.setOperation(Operation.DELETE);
        return responsePacket;
    }
}
