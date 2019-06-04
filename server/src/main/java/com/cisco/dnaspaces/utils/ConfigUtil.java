package com.cisco.dnaspaces.utils;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Properties;

/**
 * Utility to provide configuration needed by the application.
 */
public class ConfigUtil {

    /**
     * Reads configuratoins from a file which will be used wherever needed by the application
     *
     * @return Properties object containing list of configured application properties
     */
    public static Properties getConfig() {
        Properties prop = new Properties();
        try (InputStream input = ConfigUtil.class.getClassLoader().getResourceAsStream("app.properties")) {
            // load a properties file
            prop.load(input);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return prop;

    }

    public static Map<String,String> getJsonConfig(String eventName) {
        try {
            InputStream input = ConfigUtil.class.getClassLoader().getResourceAsStream("jsonconfig/"+eventName.toLowerCase()+".json");
            BufferedReader rd = new BufferedReader(new InputStreamReader(input));
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                json.append(line);
            }
            return JsonToCSV.parseJSONConfig(json.toString());

        } catch (Exception e) {

        }
        return null;
    }
}
