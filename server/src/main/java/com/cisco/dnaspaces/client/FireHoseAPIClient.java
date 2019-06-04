package com.cisco.dnaspaces.client;

import com.cisco.dnaspaces.consumers.JsonEventConsumer;
import com.cisco.dnaspaces.exceptions.FireHoseAPIException;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.time.Instant;

public class FireHoseAPIClient implements Closeable {

    private static final Logger log = LogManager.getLogger(FireHoseAPIClient.class);
    final String API_URL;
    final String API_KEY;
    long fromTimeStampAdvanceWindow;
    CloseableHttpClient httpclient;
    JsonEventConsumer consumer;

    public FireHoseAPIClient(final String API_URL, final String API_KEY) {
        this.API_KEY = API_KEY;
        this.API_URL = API_URL;
        init();
    }

    public void setConsumer(JsonEventConsumer consumer) {
        this.consumer = consumer;
    }

    public long getFromTimeStampAdvanceWindow() {
        return fromTimeStampAdvanceWindow;
    }

    public void setFromTimeStampAdvanceWindow(long fromTimeStampAdvanceWindow) {
        this.fromTimeStampAdvanceWindow = fromTimeStampAdvanceWindow;
    }

    private void init() {
        log.info("Initializing FireHoseAPIClient");
        RequestConfig requestConfig = RequestConfig
                .custom()
                .setRedirectsEnabled(true)
                .build();
        this.httpclient = HttpClients
                .custom()
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    public void startConsumeEvents() throws FireHoseAPIException {
        if (consumer == null) {
            throw new FireHoseAPIException("Event Data consumer not set");
        }
        HttpGet request = null;
        try {
            request = this.getRequest(this.API_URL);
            log.debug("Executing GET request over http client. URL :: " + request.getURI().toString());
            HttpResponse response = httpclient.execute(request);
            log.debug("GET request executed. Received status code :: " + response.getStatusLine().getStatusCode());
            int statusCode = response.getStatusLine().getStatusCode();
            if(statusCode >= 300 && statusCode == 399){
                String location = redirectHandler(response);
                throw new FireHoseAPIException("Couldn't startConsumeEvents", response.getStatusLine().getStatusCode());
            } else if(statusCode >= 400 && statusCode == 499){
                throw new FireHoseAPIException("Couldn't startConsumeEvents", response.getStatusLine().getStatusCode());
            } else if(statusCode >= 500 && statusCode == 599){
                throw new FireHoseAPIException("Couldn't startConsumeEvents", response.getStatusLine().getStatusCode());
            } else if (response.getStatusLine().getStatusCode() != 200) {
                log.error("Response status code :: " + response.getStatusLine().getStatusCode());
                throw new FireHoseAPIException("Couldn't startConsumeEvents", response.getStatusLine().getStatusCode());
            }
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line;
            while ((line = rd.readLine()) != null) {
                JSONObject eventData = new JSONObject(line);
                consumer.accept(eventData);
                /*if(eventData.getString("eventType").equals("DEVICE_EXIT")){
                    throw new FireHoseAPIException("DEVICE_EXIT event found");
                }*/
            }
        } catch (IOException | URISyntaxException e) {
            throw new FireHoseAPIException(e);
        } finally {
            log.info("request has been ended");
            request.releaseConnection();
        }
    }

    private HttpGet getRequest(String url) throws URISyntaxException{
        URIBuilder uriBuilder = new URIBuilder(url);
        if (this.consumer.getLastSuccessTimeStamp() > 0)
            uriBuilder.setParameter("fromTimestamp", String.valueOf(this.getFromTimeStamp()));
        HttpGet request = new HttpGet(uriBuilder.build());
        request.addHeader("X-API-Key", this.API_KEY);
        return request;
    }

    private String redirectHandler(HttpResponse response) throws FireHoseAPIException, URISyntaxException{
        if(response.getHeaders(HttpHeaders.LOCATION) != null){
            Header[] headers = response.getHeaders(HttpHeaders.LOCATION);
            if(headers.length == 1){
                String location =headers[0].getValue();
                return location;
            }
        }
        throw new FireHoseAPIException("Invalid redirect. Location header not found.");
    }

    public long getFromTimeStamp() {
        if (this.consumer == null)
            return Instant.now().toEpochMilli();
        long lastSuccessTimeStamp = this.consumer.getLastSuccessTimeStamp();
        lastSuccessTimeStamp -= this.getFromTimeStampAdvanceWindow()*1000;
        return lastSuccessTimeStamp;
    }

    public void close() {

    }

}