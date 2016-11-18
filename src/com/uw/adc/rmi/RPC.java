package com.uw.adc.rmi;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import com.uw.adc.rmi.model.DataTransfer;
import com.uw.adc.rmi.model.PaxosObject;

public interface RPC extends java.rmi.Remote{

	public DataTransfer getData(DataTransfer obj) throws RemoteException;
	public boolean putData(DataTransfer obj) throws RemoteException;
	public boolean deleteData(DataTransfer obj) throws RemoteException;
	public List<Integer> publishMessage(DataTransfer obj) throws RemoteException;
	public boolean publishGo(List<Integer> sequenceList) throws RemoteException;
	public int sendMessage(DataTransfer obj) throws RemoteException;
	public boolean sendGo(int sequenceId) throws RemoteException;
	
	public boolean executePaxos(DataTransfer obj) throws RemoteException;
	public ArrayList<PaxosObject> sendProposeMessages(PaxosObject proposeObj) throws RemoteException;
	public PaxosObject proposeTrans(PaxosObject proposeOject) throws RemoteException;
	public boolean sendAcceptMessages(PaxosObject acceptobject) throws RemoteException;
	public boolean accept(PaxosObject acceptoOject) throws RemoteException;
	public DataTransfer getLocalData(DataTransfer obj) throws RemoteException;
	
}
