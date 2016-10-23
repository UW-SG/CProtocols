package com.uw.adc.rmi;

import java.rmi.RemoteException;

import com.uw.adc.rmi.model.DataTransfer;

public interface RPC extends java.rmi.Remote{
	
	String sayHello() throws RemoteException;
	public DataTransfer getData(DataTransfer obj) throws RemoteException;
	public boolean putData(DataTransfer obj) throws RemoteException;
	public boolean deleteData(DataTransfer obj) throws RemoteException;

}
