package com.uw.adc.rmi.model;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PaxosObject extends Remote{
	
	public DataTransfer getDataObj() throws RemoteException;
	public void setDataObj(DataTransfer operation) throws RemoteException;
	public int getSeqNum() throws RemoteException;
	public void setSeqNum(int seqNum) throws RemoteException;
	public boolean isAcceptMsg() throws RemoteException;
	public void setAcceptMsg(boolean acceptMsg) throws RemoteException;	
	public boolean hasPromised() throws RemoteException;
	public void setPromise(boolean promise) throws RemoteException;	
	public int getHighestAcceptedSeq() throws RemoteException;
	public void setHighestAcceptedSeq(int highestAcceptedSeq) throws RemoteException;
	
}
