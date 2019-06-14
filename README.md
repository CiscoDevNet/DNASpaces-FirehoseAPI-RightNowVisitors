# Cisco DNA Spaces Firehose API Sample Application - Right Now Visitors

Firehose API provides multiple events such as device entry, exit, current location, associated profile and more. Cisco DNA Spaces partner would be able to integrate the Firehose API to consume these events to realise many use cases, one such use case is to view the current visitors of a location for a customer. 

This sample application uses Cisco DNA Spaces Firehose API events such as entry, exit, current location and associated profile, builds data pipeline using AWS S3 and AWS Redshift. 

The server application(APIConsumer), invokes the Cisco DNA Spaces Firehose API and consumes the events. The events data is extracted and written into CSV file which is uploaded to AWS S3. Then the events data in the CSV files are read from AWS S3 and inserted into corressponding AWS Redshift tables for each event.

Steps to Run the server application
1) Clone the code
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
4) Build the project and execute com.cisco.dnaspaces.APIConsumer class to run the application.


With the data available in Redshift we have build a RightNow Visitors dashboard, which will list out available users in the locations configured in FireHose API. User with no updates for 10 minutes are considered in-active and will be removed from the list until application gets his location update.

Steps to Run the client application
1) Clone the code
2) In terminal move to /client directory
3) run ```npm install``` to install angular and its dependencies
4) In client/src/app/services/location/location.service.ts file, update serverUrl to your server url
5) Run the client by using ```ng serve```
6) You can access the client by browsing url http://localhost:4200


