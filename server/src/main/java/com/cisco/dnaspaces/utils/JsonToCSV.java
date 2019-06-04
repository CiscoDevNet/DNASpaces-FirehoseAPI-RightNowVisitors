package com.cisco.dnaspaces.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JsonToCSV {

    public static String get(JSONObject json, String key) {
        String[] tokens = key.split("_");
        return digForValue(json, tokens);
    }

    private static String digForValue(JSONObject json, String[] tokens) {
        Object data = json;
        try {
            for (String token : tokens) {
                data = getData(data, token);
            }
        }catch (UnsupportedOperationException | JSONException e){
//            System.out.println("UnsupportedOperationException.");
            return "";
        }
        return getValue(data);

    }

    private static Object getData(Object data, String token) {
        if (data instanceof JSONObject) {
            return ((JSONObject) data).get(token);
        } else if (data instanceof JSONArray) {
            return ((JSONArray) data).get(Integer.parseInt(token));
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private static String getValue(Object data) {
        return data.toString();
    }


    public static String getCSVString(JSONObject json, Map<String, String> csvConfig) {
        List<String> values = new ArrayList<>();
        for(String configKey : csvConfig.keySet()){
            String config = csvConfig.get(configKey);
            String value =get(json, config);
            values.add(value);
        }
        return String.join(",", values);
    }

    public static Map<String,String> parseJSONConfig(String jsonString){
        JSONObject configJson = new JSONObject(jsonString);
        Map<String,String> csvConfig = new LinkedHashMap<>();
        for(String key : configJson.keySet()){
            csvConfig.put(key, configJson.getString(key));
        }
        return csvConfig;
    }

    public static void main(String[] args) {
//        test1();
//        test2();
        test3();
    }

    private static void test1(){
        JSONObject json = new JSONObject("{'name':'thanga','location':{'name':'B17-41','members':['me','surendar']}}");
        String value = get(json, "location_members_1");
        System.out.println("Value is :: " + value);

    }

    private static void test2(){
        JSONObject json = new JSONObject("{'name':'thanga','location':{'name':'B17-41','members':['me','surendar']}}");
        Map<String,String> csvConfig = new LinkedHashMap<>();
        csvConfig.put("name","name");
        csvConfig.put("location","location_name");
        csvConfig.put("unknown","location_name_unknown");
        csvConfig.put("member","location_members_1");
        String value = getCSVString(json, csvConfig);
        System.out.println("Value is :: " + value);

    }

    private static void test3(){
        String configJson = "{'name':'name','unknown':'location_name_unknown','member':'location_members_1','location':'location_name'}";
        JSONObject json = new JSONObject("{'name':'thanga','location':{'name':'B17-41','members':['me','surendar']}}");
        Map<String,String> csvConfig = parseJSONConfig(configJson);
        String value = getCSVString(json, csvConfig);
        System.out.println("Value is :: " + value);

    }

}
