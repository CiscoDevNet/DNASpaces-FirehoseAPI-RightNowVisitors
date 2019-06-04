package com.cisco.dnaspaces.consumers;

import com.cisco.dnaspaces.utils.ConfigUtil;
import com.cisco.dnaspaces.utils.JsonToCSV;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class CSVWriter {

    private static final Logger log = LogManager.getLogger(CSVWriter.class);

    private Map<String, Map<String, String>> configs = new HashMap<>();
    private Map<String, BufferedWriter> writers = new HashMap<>();
    private Map<String, String> eventFiles = new HashMap<>();
    private String dataPath = "";
    private String dataFilePattern = "{{timestamp}}.csv";
    private String dataDelimiter = ",";


    public CSVWriter() {
        super();
        init();
    }

    private void init(){
        Properties props = ConfigUtil.getConfig();
        this.dataPath = props.getProperty("data.directory") != null ?props.getProperty("data.directory"):this.dataPath;
        this.dataDelimiter = props.getProperty("data.delimiter") != null ?props.getProperty("data.delimiter"):this.dataDelimiter;
        this.dataFilePattern = props.getProperty("data.filePattern") != null ?props.getProperty("data.filePattern"):this.dataFilePattern;
    }
    private void configureEventType(String eventType){
            configs.put(eventType, ConfigUtil.getJsonConfig(eventType));
            if(configs.get(eventType) != null) {
                log.info("Config" + configs.get(eventType));
                // create csv file writer for this event and write header
                BufferedWriter writer = this.createFileWriter(eventType);
                writers.put(eventType, writer);
            }
    }

    private void setCSVHeader(String eventType) throws IOException{
        BufferedWriter writer = writers.get(eventType);
        if(configs.get(eventType) != null && writer != null){
            String csvHeader = String.join(",",configs.get(eventType).keySet());
            writer.write(csvHeader);
        }
    }

    public void write(JSONObject eventData) throws IOException{
        String eventType = eventData.getString("eventType");
        if (!configs.containsKey(eventType)) {
            this.configureEventType(eventType);
            this.setCSVHeader(eventType);
        }
        if (configs.get(eventType) != null) {
            String csv = JsonToCSV.getCSVString(eventData, configs.get(eventType));
            log.debug("csv :: " + csv);
            this.writeCSVToFile(eventType, csv);
        }
    }

    public BufferedWriter createFileWriter(String eventType) {
        if(configs.get(eventType) == null)
            return null;
        try {
            String filePath = this.getDirectory(eventType) + this.getFileName(eventType);
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
            eventFiles.put(eventType,filePath);
            //String csvHeader = String.join(",",configs.get(eventType).keySet());
            //writer.write(csvHeader);
            return writer;
        } catch (IOException e) {
            log.error("Couldn't create file writer for event :: " + eventType);
        }
        return null;
    }

    public void writeCSVToFile(String evenType, String csv) {
        BufferedWriter writer = writers.get(evenType);
        if (writer != null) {
            try {
                writer.write("\n");
                writer.write(csv);
            } catch (IOException e) {
                log.error("Couldn't write to csv file writer for eventType : " + evenType);
            }
        }
    }

    private String getDirectory(String eventType){
        return this.dataPath.replace("{{eventtype}}",eventType);
    }


    private String getFileName(String eventType){
        return this.dataFilePattern
                .replace("{{eventtype}}",eventType)
                .replace("{{timestamp}}",String.valueOf(System.currentTimeMillis()));
    }

    public Map<String, String> rollOutFiles(){
        Map<String, String> rolledOutFiles = new HashMap<>();
        for (String eventType : writers.keySet()) {
            String filePath = eventFiles.get(eventType);
            BufferedWriter writer = writers.get(eventType);
            // create new file writer to new file and set csv headers
            this.configureEventType(eventType);
            try {
                this.setCSVHeader(eventType);
            } catch (Exception e){
                log.error("couldn't write csv headers",e);
            }
            try {
                writer.flush();
                writer.close();
                rolledOutFiles.put(eventType,filePath);
            } catch (Exception e){
                log.error("Error while closing writer",e);
            }
        }
        return rolledOutFiles;
    }

}
