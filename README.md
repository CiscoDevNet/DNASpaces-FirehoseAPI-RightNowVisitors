# Cisco Spaces Firehose API Sample Application - Right Now Visitors

Firehose API provides multiple events such as device entry, exit, current location, associated profile and more. Cisco Spaces partner would be able to integrate the Firehose API to consume these events to realise many use cases, one such use case is to view the current visitors of a location for a customer. 

This sample application uses Cisco Spaces Firehose API events such as entry, exit, current location and associated profile, builds data pipeline using AWS S3 and AWS Redshift. 

Sample Application consists of 2 components namely

1) API Server
2) Client

#### Clone the Repository and follow below instructions to run the application.

## 1) API Server
The server application(APIConsumer), invokes the Cisco Spaces Firehose API and consumes the events. The events data is extracted and written into CSV file which is uploaded to AWS S3. Then the events data in the CSV files are read from AWS S3 and inserted into corressponding AWS Redshift tables for each event.


### Steps to Run the server application
1) Navigate to /server/ folder in the cloned repository.
2) Update app.properties file (/server/src/main/resources/app.properties) with appropriate values. All the below mentioned properties are mandatory.
```properties
    api.key={{Firehose API key}}
    api.url={{Firehose API URL}}}

    websocket.port={{websocket port}}

    data.directory={{directory path to store files locally}}
    
    s3.bucketname={{S3 bucketname}}
    s3.accesskey={{S3 access key}}
    s3.secretkey={{S3 Secret key}}


    redshift.db.lookupSchemas={{Redshift Schema name}}
    redshift.db.jdbcURL={{Redshift connection String}}
    redshift.db.user={{Redshift user name}}
    redshift.db.password={{Redshift user password}}
```
3) Create local data directory as mentioned in the properties file.
4) In the configured AWS RedShift DB, create tables using the script from "data/queries.txt" file
5) Build the project and execute com.cisco.dnaspaces.APIConsumer class to run the application.

## 2) Client
With the data available in Redshift we have build a RightNow Visitors dashboard, which will list out available users in the locations configured in FireHose API. User with no updates for 10 minutes are considered in-active and will be removed from the list until application gets his location update.

### Steps to Run the client application
1) Navigate to /client/ folder in the cloned repository.
2) run ```npm install``` to install angular and its dependencies
3) In client/src/app/services/location/location.service.ts file, update serverUrl to your server url
4) Run the client by using ```ng serve```
5) You can access the client by browsing url http://localhost:4200

[![published](https://static.production.devnetcloud.com/codeexchange/assets/images/devnet-published.svg)](https://developer.cisco.com/codeexchange/github/repo/CiscoDevNet/DNASpaces-FirehoseAPI-DetectAndLocate)
