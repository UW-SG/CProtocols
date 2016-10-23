package com.main;

import com.utility.Operation;
import com.client.UDPClient;
import com.uw.adc.rmi.model.Stats;
import com.uw.adc.rmi.util.Constants;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;

import static com.uw.adc.rmi.util.Constants.UDP_STATS_LOGGER;

/**
 * Created by Anurita on 10/20/2016.
 */
public class UDPClientMain {
    public static void main(String args[]) {

        try {

            String csvFile = "kvp-operations.csv";
            //String csvFile = args[2];
            String host = args[0];
            String port = args[1];
            BufferedReader bufferedReader = new BufferedReader(new FileReader(csvFile));
            UDPClient udpClient = new UDPClient();
            String currentData;
            while ((currentData = bufferedReader.readLine()) != null) {
                udpClient.encodePacket(currentData, host, port);
            }
            UDPClientMain.computePerformance(udpClient);
        } catch (Exception e) {
            Constants.LOGGER.error("Client error: " + e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Computation of average and standard deviation
     *
     * @param udpClient
     */
    private static void computePerformance(UDPClient udpClient) {
        Constants.UDP_STATS_LOGGER.info("---------PERFORMANCE ANALYSIS---------");

        int i = 0;
        long getTotalTime = 0, putTotalTime = 0, delTotalTime = 0;
        int getRequestCount = 0, putRequestCount = 0, delRequestCount = 0;
        long getAvgTime, putAvgTime, delAvgTime;
        List<Stats> statsList = udpClient.getStatsList();
        while (i < statsList.size()) {

            Stats statsObj = statsList.get(i);
            Operation op = Operation.fromValue(statsObj.getOperation());
            switch (op) {
                case GET:
                    getTotalTime = getTotalTime + statsObj.getTime();
                    ++getRequestCount;
                    break;
                case PUT:
                    putTotalTime = putTotalTime + statsObj.getTime();
                    ++putRequestCount;
                    break;
                case DELETE:
                    delTotalTime = delTotalTime + statsObj.getTime();
                    ++delRequestCount;
            }
            ++i;
        }
        long avgGetTime = getTotalTime / getRequestCount;
        long avgPutTime = putTotalTime / putRequestCount;
        long avgDelTime = delTotalTime / delRequestCount;
        if (getRequestCount > 0)
            UDP_STATS_LOGGER.info("Average Compute time for UDP GET request:" + avgGetTime + "ms");
        if (putRequestCount > 0)
            UDP_STATS_LOGGER.info("Average Compute time for UDP PUT request:" + avgPutTime + "ms");
        if (delRequestCount > 0)
            UDP_STATS_LOGGER.info("Average Compute time for UDP DELETE request:" + avgDelTime + "ms");

        int sqGetTDiff = 0, sqPutTDiff = 0, sqDelTDiff = 0;
        i = 0;
        while (i < statsList.size()) {
            Stats statsObj = (statsList.get(i));
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

        long getGetVar = sqGetTDiff / getRequestCount;
        long getPutVar = sqPutTDiff / putRequestCount;
        long getDelVar = sqDelTDiff / delRequestCount;
        if (getRequestCount > 0)
            UDP_STATS_LOGGER.info("Standard Deviation for UDP GET request:" + Math.sqrt(getGetVar) + " ms");
        if (putRequestCount > 0)
            UDP_STATS_LOGGER.info("Standard Deviation for UDP PUT request:" + Math.sqrt(getPutVar) + " ms");
        if (delRequestCount > 0)
            UDP_STATS_LOGGER.info("Standard Deviation for UDP DELETE request:" + Math.sqrt(getDelVar) + " ms");
        UDP_STATS_LOGGER.info("---------PERFORMANCE ANALYSIS COMPLETE---------");
    }
}
