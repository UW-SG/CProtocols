package com.uw.adc.rmi.server;

import java.io.FileInputStream;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import org.apache.log4j.Logger;

import com.uw.adc.rmi.RPC;
import com.uw.adc.rmi.model.DataTransfer;
import com.uw.adc.rmi.model.PaxosObject;
import com.uw.adc.rmi.model.PaxosObjectImpl;
import com.uw.adc.rmi.util.Constants;

public class RPCServer implements RPC {

    private static final Logger serverLog = Logger.getLogger("RPC_SERVER_LOG");

    private int port = 1099;
    private HashMap<String, String> resource = new HashMap<String, String>();
    private HashMap<Integer, DataTransfer> pendingTasks = new HashMap<Integer, DataTransfer>();

    private static int seq = 0;

    private String server1Host;
    private String server2Host;
    private String server3Host;
    private String server4Host;
    private String server5Host;
    private int server1Port;
    private int server2Port;
    private int server3Port;
    private int server4Port;
    private int server5Port;

    public RPCServer() throws RemoteException {

        try {
            Properties prop = new Properties();
            InputStream input = new FileInputStream("common.properties");
            prop.load(input);

            serverLog.debug("currentserver.host:" + prop.getProperty("currentserver.host"));
            serverLog.debug("currentserver.port:" + prop.getProperty("currentserver.port"));
            serverLog.debug("server1.host:" + prop.getProperty("server1.host"));
            serverLog.debug("server1.port:" + prop.getProperty("server1.port"));
            serverLog.debug("server2.host:" + prop.getProperty("server2.host"));
            serverLog.debug("server2.port:" + prop.getProperty("server2.port"));
            serverLog.debug("server3.host:" + prop.getProperty("server3.host"));
            serverLog.debug("server3.port:" + prop.getProperty("server3.port"));
            serverLog.debug("server4.host:" + prop.getProperty("server4.host"));
            serverLog.debug("server4.port:" + prop.getProperty("server4.port"));
            serverLog.debug("server5.host:" + prop.getProperty("server5.host"));
            serverLog.debug("server5.port:" + prop.getProperty("server5.port"));

            port = Integer.parseInt(prop.getProperty("currentserver.port"));
            server1Host = prop.getProperty("server1.host");
            server2Host = prop.getProperty("server2.host");
            server3Host = prop.getProperty("server3.host");
            server4Host = prop.getProperty("server4.host");
            server5Host = prop.getProperty("server5.host");
            server1Port = Integer.parseInt(prop.getProperty("server1.port"));
            server2Port = Integer.parseInt(prop.getProperty("server2.port"));
            server3Port = Integer.parseInt(prop.getProperty("server3.port"));
            server4Port = Integer.parseInt(prop.getProperty("server4.port"));
            server5Port = Integer.parseInt(prop.getProperty("server5.port"));

            seq = Integer.parseInt(prop.getProperty("current.sequence"));
        } catch (Exception e) {
            serverLog.error("Error in reading Prop file: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @Override
    public DataTransfer getLocalData(DataTransfer obj) throws RemoteException {

        serverLog.debug("Request received:" + obj.toString());
        if (obj.getKey() != null) {
            String value = (String) resource.get(obj.getKey());
            obj.setValue(value);
        } else {
            serverLog.debug("Invalid Request: Missing Key");
        }
        serverLog.debug("Response Sent:" + obj.toString());

        return obj;
    }

	
	/*@Override
    public boolean putData(DataTransfer obj) throws RemoteException {
		
		boolean response=false;
		
		serverLog.debug("Request received:"+obj.toString());
		if(obj.getKey()!=null && obj.getValue() != null){
			
			obj.setOperation("PUT");
			
			List<Integer> sequenceList = publishMessage(obj);
			boolean status = publishGo(sequenceList);
			
			if(status){
				resource.put(obj.getKey(), obj.getValue());
				response = true;
			}
			
		}
		else{
			serverLog.debug("Invalid Request: Missing Key/Value");
		}
		
		serverLog.debug("Response Sent:"+response);		
		return response;
	}*/

	/* --------------------------  PAXOS STARTS ----------------------------------------------------------------*/

    @Override
    public DataTransfer getData(DataTransfer obj) throws RemoteException {

        serverLog.debug("Request received:" + obj.toString());
        if (obj.getKey() != null) {
            obj = remoteGET(obj);
        } else {
            serverLog.debug("Invalid Request: Missing Key");
        }
        serverLog.debug("Response Sent:" + obj.toString());

        return obj;
    }


    @Override
    public boolean putData(DataTransfer obj) throws RemoteException {

        boolean response = false;

        serverLog.debug("Request received:" + obj.toString());

        if (obj.getKey() != null && obj.getValue() != null) {

            obj.setOperation("PUT");

            response = executePaxos(obj);
        } else {
            serverLog.debug("Invalid Request: Missing Key/Value");
        }

        serverLog.debug("Response Sent:" + response);

        return response;
    }

	
	
	/*@Override
    public boolean deleteData(DataTransfer obj) throws RemoteException {
		
		boolean response=false;		
		
		serverLog.debug("Request received:"+obj.toString());
		if(obj.getKey()!=null){
			
			obj.setOperation("DELETE");
			List<Integer> sequenceList = publishMessage(obj);
			boolean status = publishGo(sequenceList);
			
			if(status){
				resource.remove(obj.getKey());
				response = true;
			}			
		}
		else{
			serverLog.debug("Invalid Request: Missing Key");
		}
		serverLog.debug("Response Sent:"+response);
		
		return response;
	}*/

    @Override
    public boolean deleteData(DataTransfer obj) throws RemoteException {

        boolean response = false;

        serverLog.debug("Request received:" + obj.toString());
        if (obj.getKey() != null) {

            obj.setOperation("DELETE");

            response = executePaxos(obj);
        } else {
            serverLog.debug("Invalid Request: Missing Key");
        }
        serverLog.debug("Response Sent:" + response);

        return response;
    }

    @Override
    public boolean executePaxos(DataTransfer obj) throws RemoteException {

        PaxosObject proposeRequest = new PaxosObjectImpl();
        boolean result = false;

        seq = seq + 5;
        proposeRequest.setSeqNum(seq);
        proposeRequest.setDataObj(obj);
        proposeRequest.setAcceptMsg(false);
        System.out.println(obj.toString());
        System.out.println(proposeRequest.getDataObj().toString());

        ArrayList<PaxosObject> proposalResultList = sendProposeMessages(proposeRequest);
		
		/*Testing-Start*/
		/*DataTransfer obj1 = new DataTransferImpl();
		obj1.setOperation("PUT");
		obj1.setKey("XYZ");
		obj1.setValue("101");
		
		
		ArrayList<PaxosObject> proposalResultList  = new ArrayList<PaxosObject>();
		PaxosObject resObj1 = new PaxosObjectImpl();
			resObj1.setSeqNum(seq);
			resObj1.setDataObj(obj);
			resObj1.setAcceptMsg(false);
			resObj1.setPromise(true);
		proposalResultList.add(resObj1);
		PaxosObject resObj2 = new PaxosObjectImpl();
			resObj2.setSeqNum(seq);
			resObj2.setDataObj(obj);
			resObj2.setAcceptMsg(false);
			resObj2.setPromise(true);			
		proposalResultList.add(resObj2);
		PaxosObject resObj3 = new PaxosObjectImpl();
			resObj3.setSeqNum(seq);
			resObj3.setDataObj(null);
			resObj3.setAcceptMsg(false);
			resObj3.setPromise(true);			
		proposalResultList.add(resObj3);
		PaxosObject resObj4 = new PaxosObjectImpl();
			resObj4.setSeqNum(seq);
			resObj4.setDataObj(null);
			resObj4.setAcceptMsg(false);
			resObj4.setPromise(true);
		proposalResultList.add(resObj4);
		PaxosObject resObj5 = new PaxosObjectImpl();
			resObj5.setSeqNum(seq);
			resObj5.setDataObj(null);
			resObj5.setAcceptMsg(false);
			resObj5.setPromise(true);
		proposalResultList.add(resObj5);
		System.out.println(((PaxosObject)proposalResultList.get(0)).getDataObj().toString());*/
		/*Testing-End*/

        PaxosObject paxObj = new PaxosObjectImpl();

        boolean sendAccept = false;

        if (proposalResultList != null && proposalResultList.size() >= Constants.SERVER_MAJORITY) {

            Map<DataTransfer, Integer> dataTransferMap = new HashMap<DataTransfer, Integer>();
            int countAccept = 0;
            int maxCount = 0;
            int maxUnacceptedSeq = 0;
            DataTransfer maxDataTransfer = null;

            for (int i = 0; i <= proposalResultList.size() - 1; i++) {
                paxObj = (PaxosObject) proposalResultList.get(i);
                System.out.println("paxObj hasPromised:" + paxObj.hasPromised());
                System.out.println("paxObj.getDataObj():"+paxObj.getDataObj().getOperation() + " "+ paxObj.getDataObj().getKey());                
                
                if (paxObj.hasPromised()) {

                    countAccept += 1;

                    if (paxObj.getDataObj() != null) {
                        DataTransfer dataTransfer = paxObj.getDataObj();
                        int count;
                        if (dataTransferMap.containsKey(dataTransfer)) {
                            count = dataTransferMap.get(dataTransfer);
                            dataTransferMap.put(dataTransfer, ++count);
                        } else {
                            dataTransferMap.put(dataTransfer, 1);
                            count = 1;
                        }
                        if (count > maxCount) {
                            maxCount = count;
                            maxDataTransfer = dataTransfer;
                        }
                    }
                } else {

                    if (paxObj.getSeqNum() > maxUnacceptedSeq) maxUnacceptedSeq = paxObj.getSeqNum();
                }

            }

            System.out.println("countAccept:" + countAccept);
            System.out.println("maxCount:" + maxCount);
            System.out.println("maxDataTransfer:" + maxDataTransfer.toString());

            if (countAccept >= Constants.SERVER_MAJORITY && maxCount >= Constants.SERVER_MAJORITY) {

                sendAccept = true;
                //proposeRequest.setSeqNum(seq); //Not needed
                proposeRequest.setAcceptMsg(true);
                proposeRequest.setDataObj(maxDataTransfer);
                System.out.println("maxDataTransfer:" + maxDataTransfer.toString());
            } else {
                if (maxUnacceptedSeq > seq) seq = maxUnacceptedSeq;
                System.out.println("maxUnacceptedSeq:" + maxUnacceptedSeq);
            }

        }

        if (sendAccept) {
            System.out.println("Inside sendAccept: " + sendAccept);
            result = sendAcceptMessages(proposeRequest);
		
			/*Testing-Start*/
            //result = true;
        }	/*Testing-End*/
        else {
            result = false;
        }
        return result;
    }


    @Override
    public ArrayList<PaxosObject> sendProposeMessages(PaxosObject proposeObj) throws RemoteException {

        ArrayList<PaxosObject> proposalResp = new ArrayList<PaxosObject>();

        serverLog.debug("proposeMessage Message:" + proposeObj.toString());

        try {


            Registry registry1 = LocateRegistry.getRegistry(server1Host, server1Port);
            RPC stub1 = (RPC) registry1.lookup(Constants.RPC_SERVER);
            PaxosObject resObj1 = stub1.proposeTrans(proposeObj);
            if (resObj1 != null) proposalResp.add(resObj1);
        } catch (Exception e) {
            serverLog.error("Error in Proposal phase : Server-" + server1Host + " Port-" + server1Port + "Error-" + e.getMessage());
            e.printStackTrace();
        }

        try {
            Registry registry2 = LocateRegistry.getRegistry(server2Host, server2Port);
            RPC stub2 = (RPC) registry2.lookup(Constants.RPC_SERVER);
            PaxosObject resObj2 = stub2.proposeTrans(proposeObj);
            if (resObj2 != null) proposalResp.add(resObj2);
        } catch (Exception e) {
            serverLog.error("Error in Proposal phase : Server-" + server2Host + " Port-" + server2Port + "Error-" + e.getMessage());
            e.printStackTrace();
        }

        try {
            Registry registry3 = LocateRegistry.getRegistry(server3Host, server3Port);
            RPC stub3 = (RPC) registry3.lookup(Constants.RPC_SERVER);
            PaxosObject resObj3 = stub3.proposeTrans(proposeObj);
            if (resObj3 != null) proposalResp.add(resObj3);
        } catch (Exception e) {
            serverLog.error("Error in Proposal phase : Server-" + server3Host + " Port-" + server3Port + "Error-" + e.getMessage());
            e.printStackTrace();
        }

        try {
            Registry registry4 = LocateRegistry.getRegistry(server4Host, server4Port);
            RPC stub4 = (RPC) registry4.lookup(Constants.RPC_SERVER);
            PaxosObject resObj4 = stub4.proposeTrans(proposeObj);
            if (resObj4 != null) proposalResp.add(resObj4);
        } catch (Exception e) {
            serverLog.error("Error in Proposal phase : Server-" + server4Host + " Port-" + server4Port + "Error-" + e.getMessage());
            e.printStackTrace();
        }

        try {
            Registry registry5 = LocateRegistry.getRegistry(server5Host, server5Port);
            RPC stub5 = (RPC) registry5.lookup(Constants.RPC_SERVER);
            PaxosObject resObj5 = stub5.proposeTrans(proposeObj);
            if (resObj5 != null) proposalResp.add(resObj5);
        } catch (Exception e) {
            serverLog.error("Error in Proposal phase : Server-" + server5Host + " Port-" + server5Port + "Error-" + e.getMessage());
            e.printStackTrace();
        }

        serverLog.debug("Proposal phase over");
        return proposalResp;
    }


    public PaxosObject proposeTrans(PaxosObject reqObj) throws RemoteException {

        serverLog.debug("Propose msg:" + reqObj.toString());
        System.out.println("Propose msg:" + reqObj.toString());
        PaxosObject obj = new PaxosObjectImpl();
        System.out.println("In acceptor phase prepare");
        try {

            if (!reqObj.isAcceptMsg()) {

                if (pendingTasks.isEmpty()) {
                    pendingTasks.put(reqObj.getSeqNum(), reqObj.getDataObj());
                    obj.setPromise(true);
                    obj.setSeqNum(reqObj.getSeqNum());
                    obj.setDataObj(reqObj.getDataObj());
                    

                } else {
                    Map.Entry<Integer, DataTransfer> entry = pendingTasks.entrySet().iterator().next();
                    Integer prevSequenceNumber = entry.getKey();
                    if (prevSequenceNumber > reqObj.getSeqNum()) {
                        obj.setPromise(false);
                        obj.setSeqNum(prevSequenceNumber);

                    } else {
                        DataTransfer prevDataTransferObj = pendingTasks.get(prevSequenceNumber);
                        pendingTasks.clear();
                        pendingTasks.put(reqObj.getSeqNum(), prevDataTransferObj);
                        obj.setPromise(true);
                        obj.setSeqNum(reqObj.getSeqNum());
                        obj.setDataObj(prevDataTransferObj);

                    }
                }
            }
            System.out.println("Accepted seq number: " + pendingTasks.toString());            
            System.out.println("obj.getDataObj():"+obj.getDataObj().getOperation() + " "+ obj.getDataObj().getKey());
            
            return obj;
			/*  Accpetopr implements*/

        } catch (Exception e) {
            serverLog.error("Error in Proposal phase : " + e.getMessage());
            e.printStackTrace();
        }

        return obj;
    }


    @Override
    public boolean sendAcceptMessages(PaxosObject acceptObject) throws RemoteException {

        serverLog.debug("Accept msg: " + acceptObject.toString());

        boolean acceptance = false;
        List<Boolean> acceptanceList = new ArrayList<Boolean>();

        try {
            Registry registry1 = LocateRegistry.getRegistry(server1Host, server1Port);
            RPC stub1 = (RPC) registry1.lookup(Constants.RPC_SERVER);
            acceptanceList.add(stub1.accept(acceptObject));

        } catch (Exception e) {
            serverLog.error("Error in Acceptance phase : Server-" + server1Host + " Port-" + server1Port + "Error-" + e.getMessage());
            e.printStackTrace();
        }

        try {
            Registry registry2 = LocateRegistry.getRegistry(server2Host, server2Port);
            RPC stub2 = (RPC) registry2.lookup(Constants.RPC_SERVER);
            acceptanceList.add(stub2.accept(acceptObject));

        } catch (Exception e) {
            serverLog.error("Error in Acceptance phase : Server-" + server2Host + " Port-" + server2Port + "Error-" + e.getMessage());
            e.printStackTrace();
        }

        try {
            Registry registry3 = LocateRegistry.getRegistry(server3Host, server3Port);
            RPC stub3 = (RPC) registry3.lookup(Constants.RPC_SERVER);
            acceptanceList.add(stub3.accept(acceptObject));

        } catch (Exception e) {
            serverLog.error("Error in Acceptance phase : Server-" + server3Host + " Port-" + server3Port + "Error-" + e.getMessage());
            e.printStackTrace();
        }

        try {
            Registry registry4 = LocateRegistry.getRegistry(server4Host, server4Port);
            RPC stub4 = (RPC) registry4.lookup(Constants.RPC_SERVER);
            acceptanceList.add(stub4.accept(acceptObject));

        } catch (Exception e) {
            serverLog.error("Error in Acceptance phase : Server-" + server4Host + " Port-" + server4Port + "Error-" + e.getMessage());
            e.printStackTrace();
        }

        try {
            Registry registry5 = LocateRegistry.getRegistry(server5Host, server5Port);
            RPC stub5 = (RPC) registry5.lookup(Constants.RPC_SERVER);
            acceptanceList.add(stub5.accept(acceptObject));

        } catch (Exception e) {
            serverLog.error("Error in Acceptance phase : Server-" + server5Host + " Port-" + server5Port + "Error-" + e.getMessage());
            e.printStackTrace();
        }

        for (int i = 0; i < acceptanceList.size(); i++) {

            acceptance = acceptance || (boolean)acceptanceList.get(i);

        }

        return acceptance;
    }

    public boolean accept(PaxosObject acceptObj) throws RemoteException {

        serverLog.debug("Acceptance msg:" + acceptObj.toString());
        System.out.println("In acceptor second phase");
        boolean result = false;
        try {

            if (acceptObj.isAcceptMsg()) {
                DataTransfer dataTransfer = acceptObj.getDataObj();
                switch (dataTransfer.getOperation()) {
                    case "PUT":
                        if (dataTransfer.getKey() != null && dataTransfer.getValue() != null) {
                            resource.put(dataTransfer.getKey(), dataTransfer.getValue());
                            result = true;
                            pendingTasks.clear();
                        }
                        break;
                    case "DELETE":
                        if (dataTransfer.getKey() != null) {
                            resource.remove(dataTransfer.getKey());
                            result = true;
                            pendingTasks.clear();
                        }
                        break;
                    default:
                        serverLog.debug("Invalid Operation");
                }

            }
           	/*  Acceptor implements*/
        } catch (Exception e) {
            serverLog.error("Error in Acceptor phase : " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }


    public DataTransfer remoteGET(DataTransfer getRequest){

        serverLog.debug("Call Get Method Remotely: " + getRequest.toString());

        DataTransfer response = null;


        try {
            Registry registry1 = LocateRegistry.getRegistry(server1Host, server1Port);
            RPC stub1 = (RPC) registry1.lookup(Constants.RPC_SERVER);
            response = stub1.getLocalData(getRequest);

            if (response != null && response.getValue() != null)
                return response;

        } catch (Exception e) {
            serverLog.error("Error in GET Method : Server-" + server1Host + " Port-" + server1Port + "Error-" + e.getMessage());
            e.printStackTrace();
        }

        try {
            Registry registry2 = LocateRegistry.getRegistry(server2Host, server2Port);
            RPC stub2 = (RPC) registry2.lookup(Constants.RPC_SERVER);
            response = stub2.getLocalData(getRequest);

            if (response != null && response.getValue() != null)
                return response;

        } catch (Exception e) {
            serverLog.error("Error in GET Method : Server-" + server2Host + " Port-" + server2Port + "Error-" + e.getMessage());
            e.printStackTrace();
        }

        try {
            Registry registry3 = LocateRegistry.getRegistry(server3Host, server3Port);
            RPC stub3 = (RPC) registry3.lookup(Constants.RPC_SERVER);
            response = stub3.getLocalData(getRequest);

            if (response != null && response.getValue() != null)
                return response;

        } catch (Exception e) {
            serverLog.error("Error in GET Method : Server-" + server3Host + " Port-" + server3Port + "Error-" + e.getMessage());
            e.printStackTrace();
        }

        try {
            Registry registry4 = LocateRegistry.getRegistry(server4Host, server4Port);
            RPC stub4 = (RPC) registry4.lookup(Constants.RPC_SERVER);
            response = stub4.getLocalData(getRequest);
            if (response != null && response.getValue() != null)
                return response;

        } catch (Exception e) {
            serverLog.error("Error in GET Method : Server-" + server4Host + " Port-" + server4Port + "Error-" + e.getMessage());
            e.printStackTrace();
        }

        try {
            Registry registry5 = LocateRegistry.getRegistry(server5Host, server5Port);
            RPC stub5 = (RPC) registry5.lookup(Constants.RPC_SERVER);
            response = stub5.getLocalData(getRequest);

            if (response != null && response.getValue() != null)
                return response;
        } catch (Exception e) {
            serverLog.error("Error in in GET Method : Server-" + server5Host + " Port-" + server5Port + "Error-" + e.getMessage());
            e.printStackTrace();
        }

        return getRequest;

    }
	
	/* -----------------------------------------   PAXOS ENDS ----------------------------------------------------------*/
	
	
	/* -----------------------------------------TWO PHASE COMMIT STARTS -----------------------------------------------*/

    @Override
    public List<Integer> publishMessage(DataTransfer obj) throws RemoteException {

        serverLog.debug("Publishing Message:" + obj.toString());

        List<Integer> sequenceList = new ArrayList<Integer>();

        try {

            Registry registry1 = LocateRegistry.getRegistry(server1Host, server1Port);
            RPC stub1 = (RPC) registry1.lookup(Constants.RPC_SERVER);
            int sequence1 = stub1.sendMessage(obj);
            if (sequence1 != 0) sequenceList.add(sequence1);

            Registry registry2 = LocateRegistry.getRegistry(server2Host, server2Port);
            RPC stub2 = (RPC) registry2.lookup(Constants.RPC_SERVER);
            int sequence2 = stub2.sendMessage(obj);
            if (sequence2 != 0) sequenceList.add(sequence2);

            Registry registry3 = LocateRegistry.getRegistry(server3Host, server3Port);
            RPC stub3 = (RPC) registry3.lookup(Constants.RPC_SERVER);
            int sequence3 = stub3.sendMessage(obj);
            if (sequence3 != 0) sequenceList.add(sequence3);

            Registry registry4 = LocateRegistry.getRegistry(server4Host, server4Port);
            RPC stub4 = (RPC) registry4.lookup(Constants.RPC_SERVER);
            int sequence4 = stub4.sendMessage(obj);
            if (sequence4 != 0) sequenceList.add(sequence4);

            serverLog.debug("Message Published");
            return sequenceList;
        } catch (Exception e) {
            serverLog.error("Error in Publishing Message: " + e.getMessage());
            e.printStackTrace();
        }

        serverLog.debug("Message Publishing failed");
        return null;
    }

    @Override
    public boolean publishGo(List<Integer> sequenceList) throws RemoteException {

        serverLog.debug("Publising Go:" + sequenceList.toString());

        try {

            if (sequenceList != null && sequenceList.size() == 4) {

                Registry registry1 = LocateRegistry.getRegistry(server1Host, server1Port);
                RPC stub1 = (RPC) registry1.lookup(Constants.RPC_SERVER);

                Registry registry2 = LocateRegistry.getRegistry(server2Host, server2Port);
                RPC stub2 = (RPC) registry2.lookup(Constants.RPC_SERVER);

                Registry registry3 = LocateRegistry.getRegistry(server3Host, server3Port);
                RPC stub3 = (RPC) registry3.lookup(Constants.RPC_SERVER);

                Registry registry4 = LocateRegistry.getRegistry(server4Host, server4Port);
                RPC stub4 = (RPC) registry4.lookup(Constants.RPC_SERVER);

                boolean success = stub1.sendGo(sequenceList.get(0)) &&
                        stub2.sendGo(sequenceList.get(1)) &&
                        stub3.sendGo(sequenceList.get(2)) &&
                        stub4.sendGo(sequenceList.get(3));

                serverLog.debug("Go published successfully");
                return true;
            }
        } catch (Exception e) {
            serverLog.error("Error in Publishing Go: " + e.getMessage());
            e.printStackTrace();
        }

        serverLog.debug("Go publishing failed");
        return false;
    }


    @Override
    public int sendMessage(DataTransfer obj) throws RemoteException {

        serverLog.debug("Recieved Message:" + obj.toString());

        try {

            Random randomGenerator = new Random();
            int sequenceId = randomGenerator.nextInt(10000);

            //need a non-zero sequenceId
            while (sequenceId == 0) sequenceId = randomGenerator.nextInt(10000);

            pendingTasks.put(sequenceId, obj);

            serverLog.debug("Added to Pending Tasks. Sequence Id:" + sequenceId);
            return sequenceId;
        } catch (Exception e) {
            serverLog.error("Error in receiveing message: " + e.getMessage());
            e.printStackTrace();
        }

        serverLog.debug("Failed to add to Pending tasks. Sequence Id:" + 0);
        return 0;
    }

    @Override
    public boolean sendGo(int sequenceId) throws RemoteException {

        serverLog.debug("Recieved Go for SequenceId:" + sequenceId);
        boolean response = false;

        try {

            DataTransfer obj = (DataTransfer) pendingTasks.get(sequenceId);

            if (obj != null && obj.getOperation() != null) {

                switch (obj.getOperation()) {
                    case "PUT":
                        if (obj.getKey() != null && obj.getValue() != null) {
                            resource.put(obj.getKey(), obj.getValue());
                            response = true;
                        }
                        break;
                    case "DELETE":
                        if (obj.getKey() != null) {
                            resource.remove(obj.getKey());
                            response = true;
                        }
                        break;
                    default:
                        serverLog.debug("Invalid Operation");
                }
            }

            serverLog.debug("Completed Go successfully. Response:" + response);
            return response;
        } catch (Exception e) {
            serverLog.error("Error in receiveing go: " + e.getMessage());
            e.printStackTrace();
        }

        serverLog.debug("Failed to complete operation. Sequence Id:" + sequenceId);
        return false;
    }

    public static void main(String args[]) {
        try {
            serverLog.debug("Starting RPC Server");
            RPCServer obj = new RPCServer();
        	
        /*	try{
        		if(args.length > 0)obj.port = Integer.parseInt(args[0]);
        	}catch(NumberFormatException e)
            {        	
        		serverLog.error("RPCServer exception: " + e.getMessage()); 
            	System.out.println("Invalid Port");
            	throw e;
            }
*/
            obj.port = Integer.parseInt(args[0]);
            RPC stub = (RPC) UnicastRemoteObject.exportObject(obj, 0);
            Registry registry = LocateRegistry.createRegistry(obj.port);
            //Registry registry = LocateRegistry.getRegistry();
            //System.setProperty("java.rmi.server.hostname","10.0.0.2");
            registry.bind(Constants.RPC_SERVER, stub);

            serverLog.debug("RPC Server started");


           // Random randomGenerator = new Random();
            //int sequenceId = randomGenerator.nextInt(10000);    //random sequence number for the server
        } catch (Exception e) {
            serverLog.error("RPCServer error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //For Testing purpose
    /*public static void main(String args[]) {

        try {
            DataTransfer obj = new DataTransferImpl();
            obj.setOperation("PUT");
            obj.setKey("ABC");
            obj.setValue("100");

            RPCServer server = new RPCServer();
            System.out.println(server.putData(obj));
        } catch (Exception e) {
            serverLog.error("RPCServer error: " + e.getMessage());
            e.printStackTrace();
        }

    }
	
	/* TWO PHASE COMMIT ENDS */


}
