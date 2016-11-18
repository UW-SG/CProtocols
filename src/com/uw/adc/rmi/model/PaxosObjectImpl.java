package com.uw.adc.rmi.model;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;


public class PaxosObjectImpl extends UnicastRemoteObject implements PaxosObject{	
	
	public PaxosObjectImpl() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int seqNum;	
	private DataTransfer dataObject;
	private boolean acceptMsg;
	
	//These should be used in response by acceptors 
	private boolean promise;
	private int highestAcceptedSeq;
	
	public int getSeqNum() throws RemoteException{
		return seqNum;
	}
	public void setSeqNum(int seqNum) throws RemoteException{
		this.seqNum = seqNum;
	}
	public DataTransfer getDataObj() throws RemoteException{
		return dataObject;
	}
	
	public void setDataObj(DataTransfer dataObject) throws RemoteException{
		this.dataObject = dataObject;
	}
		
	public boolean isAcceptMsg() throws RemoteException{
		return acceptMsg;
	}
	public void setAcceptMsg(boolean acceptMsg) throws RemoteException{
		this.acceptMsg = acceptMsg;
	}
	
	public boolean hasPromised() throws RemoteException{
		return promise;
	}	
	public void setPromise(boolean promise) throws RemoteException{
		this.promise = promise;
	}

	public int getHighestAcceptedSeq() throws RemoteException{
		return highestAcceptedSeq;
	}
	public void setHighestAcceptedSeq(int highestAcceptedSeq) throws RemoteException{
		this.highestAcceptedSeq = highestAcceptedSeq;
	}
	
}