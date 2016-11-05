package com.uw.adc.rmi.model;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class DataTransferImpl extends UnicastRemoteObject implements DataTransfer{	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String operation;
	private String key;
	private String value;
	
	public DataTransferImpl() throws RemoteException {
		super();
	}
	
	@Override
	public String getOperation() throws RemoteException {
		return operation;
	}
	@Override
	public void setOperation(String operation) throws RemoteException {
		this.operation = operation;
	}
	@Override
	public String getKey() throws RemoteException {
		return key;
	}
	@Override
	public void setKey(String key) throws RemoteException {
		this.key = key;
	}
	@Override
	public String getValue() throws RemoteException {
		return value;
	}
	@Override
	public void setValue(String value) throws RemoteException {
		this.value = value;
	}
	
	@Override
	public String toString(){
		return "Data ["
				+ (operation != null ? "operation=" + key + ", " : "")
				+ (key != null ? "key=" + key + ", " : "") 
				+ (value != null ? "value=" + value : "") + "]";
	}
}
