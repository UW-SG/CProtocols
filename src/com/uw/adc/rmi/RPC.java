package com.uw.adc.rmi;

import java.rmi.RemoteException;
import java.util.List;

import com.uw.adc.rmi.model.DataTransfer;

public interface RPC extends java.rmi.Remote{
	
	String sayHello() throws RemoteException;
	public DataTransfer getData(DataTransfer obj) throws RemoteException;
	public boolean putData(DataTransfer obj) throws RemoteException;
	public boolean deleteData(DataTransfer obj) throws RemoteException;
	public List<Integer> publishMessage(DataTransfer obj) throws RemoteException;
	public boolean publishGo(List<Integer> sequenceList) throws RemoteException;
	public int sendMessage(DataTransfer obj) throws RemoteException;
	public boolean sendGo(int sequenceId) throws RemoteException;

}
