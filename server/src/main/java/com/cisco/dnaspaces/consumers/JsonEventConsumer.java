package com.cisco.dnaspaces.consumers;

import com.cisco.dnaspaces.utils.Counter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

public class JsonEventConsumer {

    private static final Logger log = LogManager.getLogger(JsonEventConsumer.class);
    private long lastSuccessTimeStamp = -1;

    private CSVWriter csvWriter;

    public void setCsvWriter(CSVWriter csvWriter) {
        this.csvWriter = csvWriter;
    }

    public long getLastSuccessTimeStamp() {
        return lastSuccessTimeStamp;
    }

    public void setLastSuccessTimeStamp(long lastSuccessTimeStamp) {
        this.lastSuccessTimeStamp = lastSuccessTimeStamp;
    }

    public void accept(JSONObject eventData) {
        String eventType = eventData.getString("eventType");
        log.debug("eventType : " + eventType);
        log.trace(eventData.toString());
        try{
            csvWriter.write(eventData);
        }catch (Exception e){
            log.error("Error while writing to CSV",e);
        }
        this.setLastSuccessTimeStamp(System.currentTimeMillis());

        Counter.addCount(eventType);
    }

}
