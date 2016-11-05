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
import java.util.Properties;
import java.util.Random;

import org.apache.log4j.Logger;

import com.uw.adc.rmi.RPC;
import com.uw.adc.rmi.model.DataTransfer;
import com.uw.adc.rmi.util.Constants;

public class RPCServer implements RPC {

	private static final Logger serverLog = Logger.getLogger("RPC_SERVER_LOG");
		
	private int port = 1099;
	private HashMap<String, String> resource = new HashMap<String, String>();	
	private HashMap<Integer, DataTransfer> pendingTasks = new HashMap<Integer, DataTransfer>();
	
	private String server1Host;
	private String server2Host;
	private String server3Host;
	private String server4Host;
	private int server1Port;
	private int server2Port;
	private int server3Port;
	private int server4Port;
	
	public RPCServer() throws RemoteException {
		
		try{
			Properties prop = new Properties();
			InputStream input = new FileInputStream("common.properties");
			prop.load(input);
			
			serverLog.debug("currentserver.host:"+prop.getProperty("currentserver.host"));
			serverLog.debug("currentserver.port:"+prop.getProperty("currentserver.port"));
			serverLog.debug("server1.host:"+prop.getProperty("server1.host"));
			serverLog.debug("server1.port:"+prop.getProperty("server1.port"));
			serverLog.debug("server2.host:"+prop.getProperty("server2.host"));
			serverLog.debug("server2.port:"+prop.getProperty("server2.port"));
			serverLog.debug("server3.host:"+prop.getProperty("server3.host"));
			serverLog.debug("server3.port:"+prop.getProperty("server3.port"));
			serverLog.debug("server4.host:"+prop.getProperty("server4.host"));
			serverLog.debug("server4.port:"+prop.getProperty("server4.port"));
			
			port = Integer.parseInt(prop.getProperty("currentserver.port"));
			server1Host = prop.getProperty("server1.host");
			server2Host = prop.getProperty("server2.host");
			server3Host = prop.getProperty("server3.host");
			server4Host = prop.getProperty("server4.host");
			server1Port = Integer.parseInt(prop.getProperty("server1.port"));
			server2Port = Integer.parseInt(prop.getProperty("server2.port"));
			server3Port = Integer.parseInt(prop.getProperty("server3.port"));
			server4Port = Integer.parseInt(prop.getProperty("server4.port"));			
		}
		catch(Exception e){
			serverLog.error("Error in reading Prop file: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	@Override
	public String sayHello() throws RemoteException {
		
		return "Hello World";
	}
	
	@Override
	public DataTransfer getData(DataTransfer obj) throws RemoteException {
		
		serverLog.debug("Request received:"+obj.toString());
		if(obj.getKey()!=null){
			String value = (String) resource.get(obj.getKey());		
			obj.setValue(value);
		}else{
			serverLog.debug("Invalid Request: Missing Key");
		}
		serverLog.debug("Response Sent:"+obj.toString());
				
		return obj;
	}
	
	@Override
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
	}
	
	@Override
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
	}
	
	@Override
	public List<Integer> publishMessage(DataTransfer obj) throws RemoteException {
		
		serverLog.debug("Publishing Message:"+obj.toString());
		
		List<Integer> sequenceList = new ArrayList<Integer>();		
		
		try{		
		
			Registry registry1 = LocateRegistry.getRegistry(server1Host,server1Port);
	        RPC stub1 = (RPC) registry1.lookup(Constants.RPC_SERVER);
	        int sequence1 = stub1.sendMessage(obj);
	        if(sequence1 != 0)sequenceList.add(sequence1);
	        
	        Registry registry2 = LocateRegistry.getRegistry(server2Host,server2Port);
	        RPC stub2 = (RPC) registry2.lookup(Constants.RPC_SERVER);
	        int sequence2 = stub2.sendMessage(obj);
	        if(sequence2 != 0)sequenceList.add(sequence2);
	        
	        Registry registry3 = LocateRegistry.getRegistry(server3Host,server3Port);
	        RPC stub3 = (RPC) registry3.lookup(Constants.RPC_SERVER);
	        int sequence3 = stub3.sendMessage(obj);
	        if(sequence3 != 0)sequenceList.add(sequence3);
	        
	        Registry registry4 = LocateRegistry.getRegistry(server4Host,server4Port);
	        RPC stub4 = (RPC) registry4.lookup(Constants.RPC_SERVER);
	        int sequence4 = stub4.sendMessage(obj);
	        if(sequence4 != 0)sequenceList.add(sequence4);
	        
	        serverLog.debug("Message Published");
	        return sequenceList;
		}
		catch(Exception e){
			serverLog.error("Error in Publishing Message: " + e.getMessage()); 
			e.printStackTrace();
		}		
				
		serverLog.debug("Message Publishing failed");
		return null;
	}
	
	@Override
	public boolean publishGo(List<Integer> sequenceList) throws RemoteException {
		
		serverLog.debug("Publising Go:"+sequenceList.toString());	
		
		try{
			
			if(sequenceList != null && sequenceList.size() == 4){
		
				Registry registry1 = LocateRegistry.getRegistry(server1Host,server1Port);
		        RPC stub1 = (RPC) registry1.lookup(Constants.RPC_SERVER);		        
		        
		        Registry registry2 = LocateRegistry.getRegistry(server2Host,server2Port);
		        RPC stub2 = (RPC) registry2.lookup(Constants.RPC_SERVER);
		        
		        Registry registry3 = LocateRegistry.getRegistry(server3Host,server3Port);
		        RPC stub3 = (RPC) registry3.lookup(Constants.RPC_SERVER);
		        
		        Registry registry4 = LocateRegistry.getRegistry(server4Host,server4Port);
		        RPC stub4 = (RPC) registry4.lookup(Constants.RPC_SERVER);
		        
		        boolean success = stub1.sendGo(sequenceList.get(0)) && 
		        					stub2.sendGo(sequenceList.get(1)) &&
		        						stub3.sendGo(sequenceList.get(2)) &&
		        							stub4.sendGo(sequenceList.get(3));
		        
		        serverLog.debug("Go published successfully");
		        return true;			
			}						
		}
		catch(Exception e){
			serverLog.error("Error in Publishing Go: " + e.getMessage());  
			e.printStackTrace();
		}
		
		serverLog.debug("Go publishing failed");				
		return false;
	}
	
	@Override
	public int sendMessage(DataTransfer obj) throws RemoteException {
	
		serverLog.debug("Recieved Message:"+obj.toString());
		
		try{
			
			Random randomGenerator = new Random();
			int sequenceId = randomGenerator.nextInt(10000);
			
			//need a non-zero sequenceId
			while(sequenceId == 0)sequenceId = randomGenerator.nextInt(10000);
			
			pendingTasks.put(sequenceId, obj);
			
			serverLog.debug("Added to Pending Tasks. Sequence Id:"+sequenceId);
			return sequenceId;
		}
		catch(Exception e){
			serverLog.error("Error in receiveing message: " + e.getMessage()); 
			e.printStackTrace();
		}
		
		serverLog.debug("Failed to add to Pending tasks. Sequence Id:"+0);
		return 0;
	}
	
	@Override
	public boolean sendGo(int sequenceId) throws RemoteException {
		
		serverLog.debug("Recieved Go for SequenceId:"+sequenceId);
		boolean response = false;
		
		try{
			
			DataTransfer obj = (DataTransfer)pendingTasks.get(sequenceId);
			
			if(obj != null && obj.getOperation() != null){
				
				switch (obj.getOperation()){					
					case "PUT": if(obj.getKey()!=null && obj.getValue() != null){
									resource.put(obj.getKey(), obj.getValue());
									response = true;
								}
								break;
					case "DELETE":if(obj.getKey()!=null){
									resource.remove(obj.getKey());
									response = true;
								}
								break;
					default: serverLog.debug("Invalid Operation");
				}				
			}
			
			serverLog.debug("Completed Go successfully. Response:"+response);
			return response;
		}
		catch(Exception e){
			serverLog.error("Error in receiveing go: " + e.getMessage()); 
			e.printStackTrace();
		}
		
		serverLog.debug("Failed to complete operation. Sequence Id:"+sequenceId);
		return false;
	}
	
	public static void main(String args[]) 
    { 
        try 
        { 
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
        	
            RPC stub = (RPC) UnicastRemoteObject.exportObject(obj, 0);
            Registry registry = LocateRegistry.createRegistry(obj.port);
        	//Registry registry = LocateRegistry.getRegistry();
        	//System.setProperty("java.rmi.server.hostname","10.0.0.2");
        	registry.bind(Constants.RPC_SERVER, stub);
        	
        	serverLog.debug("RPC Server started");
        } 
        catch (Exception e) 
        { 
        	serverLog.error("RPCServer error: " + e.getMessage()); 
            e.printStackTrace(); 
        } 
    }

}
