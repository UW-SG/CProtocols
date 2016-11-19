package com.uw.adc.rmi.client;

import com.uw.adc.rmi.RPC;
import com.uw.adc.rmi.model.DataTransfer;
import com.uw.adc.rmi.model.DataTransferImpl;
import com.uw.adc.rmi.model.Stats;
import com.uw.adc.rmi.util.Constants;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RPCClient {

    private static final Logger logger = Logger.getLogger("RPC_CLIENT_LOG");
    private static final Logger statsLogger = Logger.getLogger("RPC_CLIENT_STATISTICS_LOG");

    private List<Stats> statsList = new ArrayList<Stats>();

    public static void main(String args[]) {
        logger.debug("--------RPC PROCESS STARTED---------");

        String host = null;
        int port = 1099;
        if (args.length > 0) host = args[0];
        try {
            if (args.length > 1) port = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            logger.error("RPCClient exception: " + e.getMessage());
            System.out.println("Invalid Port");
            throw e;
        }

        logger.debug("Host:" + host);
        logger.debug("Port:" + port);

        try {
            /*Registry registry = LocateRegistry.getRegistry(host);
            RPC stub = (RPC) registry.lookup("RPCServer");
            String response = stub.sayHello();
            System.out.println("response: " + response);*/

            RPCClient client = new RPCClient();
            client.process(host, port);
        } catch (Exception e) {
            logger.error("RPCClient exception: " + e.getMessage());
            e.printStackTrace();
        }

        logger.debug("--------RPC PROCESS ENDED---------");
    }

    public void process(String host, int port) throws RemoteException {

        logger.debug("Inside process()");

        BufferedReader br = null;
        try {
            String currentLine;

            br = new BufferedReader(new FileReader(Constants.INPUT_FILE));

            Registry registry = LocateRegistry.getRegistry(host, port);
            RPC stub = (RPC) registry.lookup(Constants.RPC_SERVER);
			/*DataTransfer obj = new DataTransferImpl();
			obj.setOperation("PUT");
			obj.setKey("ABC");
			obj.setValue("100");
*/
            //RPCServer server = new RPCServer();

            while ((currentLine = br.readLine()) != null) {
                logger.debug(currentLine);

                String strArray[] = currentLine.split(",");
                if (strArray != null && strArray.length > 0 && strArray.length <= 3) {

                    DataTransfer requestObj = new DataTransferImpl();
                    if (strArray.length > 1 && strArray[1] != null) {
                        requestObj.setKey(strArray[1]);
                    }
                    if (strArray.length > 2 && strArray[2] != null) {
                        requestObj.setValue(strArray[2]);
                    }

                    switch (strArray[0]) {
                        case "GET":
                            requestObj.setOperation("GET");
                            //invokeRemoteGetMethod(stub, requestObj);
                            break;
                        case "PUT":
                            requestObj.setOperation("PUT");
                            //invokeRemotePutMethod(stub, requestObj);
                            System.out.println(stub.putData(requestObj));
                            break;
                        case "DELETE":
                            requestObj.setOperation("DELETE");
                            //invokeRemoteDeleteMethod(stub, requestObj);
                            System.out.println(stub.deleteData(requestObj));
                            break;
                        default:
                            logger.debug("Invalid Command");
                    }

                } else {
                    logger.debug("Malformed input");
                }
            }

            //Compute Performance
            //computePerformance();


        } catch (FileNotFoundException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            logger.error(e.getStackTrace());
            e.printStackTrace();
        } finally {
            try {
                if (br != null) br.close();
            } catch (IOException ex) {
                logger.error(ex.getStackTrace());
                ex.printStackTrace();
            }
        }

    }

    private void invokeRemoteGetMethod(RPC stub, DataTransfer request) throws RemoteException {

        try {

            if (request.getKey() != null) {
                Date beforeDate = new Date();
                DataTransfer response = stub.getData(request);
                Date afterDate = new Date();
                logger.debug("Response:" + response.toString());

                long time = afterDate.getTime() - beforeDate.getTime();
                Stats curentStats = new Stats("GET", time);
                statsList.add(curentStats);
                statsLogger.info(curentStats.toString());

                if (response.getKey() == null || response.getValue() == null) {
                    logger.debug("Received invalid response for GET for KEY:" + request.getKey());
                }
            } else logger.debug("Malformed input in GET: Missing Key");
        } catch (Exception e) {
            logger.error("Error in GET operation for " + request.toString());
            logger.error("Error Message:" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void invokeRemotePutMethod(RPC stub, DataTransfer request) throws RemoteException {

        try {
            if (request.getKey() != null && request.getValue() != null) {
                Date beforeDate = new Date();
                boolean response = stub.putData(request);
                Date afterDate = new Date();
                logger.debug("Response:" + response);

                long time = afterDate.getTime() - beforeDate.getTime();
                Stats curentStats = new Stats("PUT", time);
                statsList.add(curentStats);
                statsLogger.info(curentStats.toString());

                if (!response) {
                    logger.debug("Received invalid response for PUT for KEY:" + request.getKey() + " & VALUE:" + request.getValue());
                }
            } else logger.debug("Malformed input in PUT: Missing Key-value");
        } catch (Exception e) {
            logger.error("Error in PUT operation for " + request.toString());
            logger.error("Error Message:" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void invokeRemoteDeleteMethod(RPC stub, DataTransfer request) throws RemoteException {

        try {
            if (request.getKey() != null) {
                Date beforeDate = new Date();
                boolean response = stub.deleteData(request);
                Date afterDate = new Date();
                logger.debug("Response:" + response);

                long time = afterDate.getTime() - beforeDate.getTime();
                Stats curentStats = new Stats("DELETE", time);
                statsList.add(curentStats);
                statsLogger.info(curentStats.toString());

                if (!response) {
                    logger.debug("Received invalid response for DELETE for KEY:" + request.getKey());
                }
            } else logger.debug("Malformed input: Missing key in Delete");
        } catch (Exception e) {
            logger.error("Error in DELETE operation for " + request.toString());
            logger.error("Error Message:" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void computePerformance() {

        statsLogger.info("---------PERFORMANCE ANALYSIS---------");

        //Computing Average Time
        int i = 0;
        long getTotalTime = 0, putTotalTime = 0, delTotalTime = 0;
        int getRequestCount = 0, putRequestCount = 0, delRequestCount = 0;

        while (i < statsList.size()) {

            Stats statsObj = (Stats) statsList.get(i);
            switch (statsObj.getOperation()) {
                case "GET":
                    getTotalTime = getTotalTime + statsObj.getTime();
                    ++getRequestCount;
                    break;
                case "PUT":
                    putTotalTime = putTotalTime + statsObj.getTime();
                    ++putRequestCount;
                    break;
                case "DELETE":
                    delTotalTime = delTotalTime + statsObj.getTime();
                    ++delRequestCount;
            }

            ++i;
        }

        long avgGetTime = 0, avgPutTime = 0, avgDelTime = 0;

        //System.out.println("count = ===="+getRequestCount+"----"+putRequestCount+"-----"+delRequestCount);

        if (getRequestCount > 0) {
            avgGetTime = getTotalTime / getRequestCount;
            statsLogger.info("Average Compute time for RPC GET request:" + avgGetTime + " ms");
        }
        if (putRequestCount > 0) {
            avgPutTime = putTotalTime / putRequestCount;
            statsLogger.info("Average Compute time for RPC PUT request:" + avgPutTime + " ms");
        }
        if (delRequestCount > 0) {
            avgDelTime = delTotalTime / delRequestCount;
            statsLogger.info("Average Compute time for RPC DELETE request:" + avgDelTime + " ms");
        }


        //Computing Standard Variation
        int sqGetTDiff = 0, sqPutTDiff = 0, sqDelTDiff = 0;

        i = 0;

        while (i < statsList.size()) {

            Stats statsObj = (Stats) statsList.get(i);
            switch (statsObj.getOperation()) {
                case "GET":
                    sqGetTDiff += Math.pow(statsObj.getTime() - avgGetTime, 2);
                    break;
                case "PUT":
                    sqPutTDiff += Math.pow(statsObj.getTime() - avgPutTime, 2);
                    break;
                case "DELETE":
                    sqDelTDiff += Math.pow(statsObj.getTime() - avgDelTime, 2);
            }

            ++i;
        }
		
		/*long getGetVar = sqGetTDiff/getRequestCount;
		long getPutVar = sqPutTDiff/putRequestCount;
		long getDelVar = sqDelTDiff/delRequestCount;		*/

        if (getRequestCount > 0)
            statsLogger.info("Standard Deviation for RPC GET request:" + Math.sqrt(sqGetTDiff / getRequestCount) + " ms");
        if (putRequestCount > 0)
            statsLogger.info("Standard Deviation for RPC PUT request:" + Math.sqrt(sqPutTDiff / putRequestCount) + " ms");
        if (delRequestCount > 0)
            statsLogger.info("Standard Deviation for RPC DELETE request:" + Math.sqrt(sqDelTDiff / delRequestCount) + " ms");

        statsLogger.info("---------PERFORMANCE ANALYSIS COMPLETE---------");

    }
}
