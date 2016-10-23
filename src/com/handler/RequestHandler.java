package com.handler;

import com.utility.DataPacket;

/**
 * Provides method
 */
public interface RequestHandler {
    void handleGet(DataPacket dataPacket);

    void handlePut(DataPacket dataPacket);

    void handleDelete(DataPacket dataPacket);

    void handleMalformed(DataPacket dataPacket);
}
