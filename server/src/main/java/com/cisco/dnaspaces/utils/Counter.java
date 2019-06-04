package com.cisco.dnaspaces.utils;

import com.cisco.dnaspaces.WSServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Counter {

    private static final Logger log = LogManager.getLogger(Counter.class);
    private static Map<String,Integer> counts = new HashMap<>();
    private static long lastPublishedMillis = System.currentTimeMillis();
    private static WSServer wsServer;
    private static final long publishInterval = 0;

    public static void setWsServer(WSServer wsServer) {
        Counter.wsServer = wsServer;
    }

    public static void addCount(String eventName){
        if(counts.containsKey(eventName)){
            counts.put(eventName,counts.get(eventName)+1);
        }else{
            counts.put(eventName,1);
        }
        log.trace("Count of "+eventName+" :: "+counts.get(eventName));
        publishCounts();
    }

    private static void publishCounts(){
        if((lastPublishedMillis+publishInterval) < System.currentTimeMillis()){
            lastPublishedMillis = System.currentTimeMillis();
            log.debug("Count Results :: "+counts);

            String fileContent = new JSONObject(counts).toString();
            try {
                if(Counter.wsServer != null)
                    Counter.wsServer.broadcast(fileContent);
            }catch (Exception e){

            }
        }

    }

}
