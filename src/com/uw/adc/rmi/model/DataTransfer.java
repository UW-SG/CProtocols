package com.uw.adc.rmi.model;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface DataTransfer extends Remote{
	
	public String getOperation() throws RemoteException;
	public void setOperation(String operation) throws RemoteException;
	public String getKey() throws RemoteException;
	public void setKey(String key) throws RemoteException;
	public String getValue() throws RemoteException;
	public void setValue(String value) throws RemoteException;
}
