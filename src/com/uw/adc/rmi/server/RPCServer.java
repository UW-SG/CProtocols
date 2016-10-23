package com.uw.adc.rmi.server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.uw.adc.rmi.RPC;
import com.uw.adc.rmi.model.DataTransfer;
import com.uw.adc.rmi.util.Constants;

public class RPCServer implements RPC {

	private static final Logger serverLog = Logger.getLogger("RPC_SERVER_LOG");
		
	private HashMap<String, String> resource = new HashMap<String, String>();

	public RPCServer() throws RemoteException {}
	
	@Override
	public String sayHello() throws RemoteException {
		// TODO Auto-generated method stub
		return "Hello World";
	}
	
	@Override
	public DataTransfer getData(DataTransfer obj) throws RemoteException {
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		
		boolean response=false;
		
		serverLog.debug("Request received:"+obj.toString());
		if(obj.getKey()!=null && obj.getValue() != null){
			resource.put(obj.getKey(), obj.getValue());
			response = true;
		}else{
			serverLog.debug("Invalid Request: Missing Key/Value");
		}
		serverLog.debug("Response Sent:"+response);
		
		return response;
	}
	
	@Override
	public boolean deleteData(DataTransfer obj) throws RemoteException {
		// TODO Auto-generated method stub
		
		boolean response=false;
		
		serverLog.debug("Request received:"+obj.toString());
		if(obj.getKey()!=null){
			resource.remove(obj.getKey());
			response = true;
		}else{
			serverLog.debug("Invalid Request: Missing Key");
		}
		serverLog.debug("Response Sent:"+response);
		
		return response;
	}
	
	public static void main(String args[]) 
    { 
        try 
        { 
        	serverLog.debug("Starting RPC Server");
        	
        	int port = 1099;
        	try{
        		if(args.length > 0)port = Integer.parseInt(args[0]);
        	}catch(NumberFormatException e)
            {        	
        		serverLog.error("RPCServer exception: " + e.getMessage()); 
            	System.out.println("Invalid Port");
            	throw e;
            }
        	
        	RPCServer obj = new RPCServer();        	
            RPC stub = (RPC) UnicastRemoteObject.exportObject(obj, 0);
            // Bind this object instance to the name "HelloServer" 
            Registry registry = LocateRegistry.createRegistry(port);
        	//Registry registry = LocateRegistry.getRegistry();
        	//System.setProperty("java.rmi.server.hostname","10.0.0.2");
        	registry.bind(Constants.RPC_SERVER, stub);
            //Naming.rebind("RPCServer", obj); 
        	
        	serverLog.debug("RPC Server started");
        } 
        catch (Exception e) 
        { 
        	serverLog.error("RPCServer error: " + e.getMessage()); 
            e.printStackTrace(); 
        } 
    }

}
