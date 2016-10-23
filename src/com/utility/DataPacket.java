package com.utility;

import java.io.Serializable;

public class DataPacket implements Serializable {
    private Operation operation;
    private Boolean response;
    private String data;
    private Integer transactionId;
    private static Integer MAX_TRANSACTION_ID = 0;

    public DataPacket(String data) {
        this.data = data;
        this.transactionId = MAX_TRANSACTION_ID++;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public Boolean getResponse() {
        return response;
    }

    public void setResponse(Boolean response) {
        this.response = response;
    }

    public Integer getTransactionId() {
        return transactionId;
    }

    /**
     * Casts the data depending on which type getData() is assigned.
     * Eg:
     * String data = packet.getData(); // cast to String
     * Integer data = packet.getData(); // cast to Integer
     *
     * @throws ClassCastException If type T is different from the type of
     *                            data object.
     * @return
     */
    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return String.format("DataPacket(data=%s, response:%s, op:%s, transactionId: %s)",
                data,
                response,
                operation,
                MAX_TRANSACTION_ID);
    }

}
