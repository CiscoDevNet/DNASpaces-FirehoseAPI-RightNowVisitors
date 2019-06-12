package com.cisco.dnaspaces.server.handler;

import com.cisco.dnaspaces.db.Redshift;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class APIHandler {

    private static final Logger log = LogManager.getLogger(APIHandler.class);

    public Router router(Vertx vertx) {
        Router router = Router.router(vertx);
        router.get("/rightnowusers").handler(this::handleGetRightNowUsers);


        return router;
    }

    private void handleGetRightNowUsers(RoutingContext routingContext) {
        String mac = routingContext.request().getParam("mac");
        HttpServerResponse response = routingContext.response();
        response.putHeader("Access-Control-Allow-Origin", "*");
        List<Map<String, String>> records;
        String jsonString = "{\"mac\":\"" + mac + "\"}";
        try {
            long fromTime = System.currentTimeMillis() - 600000;
            String query = "select p.*,pu.first_name, pu.last_name, pu.gender, pu.email, pu.mobile from (select mac_address, max(record_timestamp) as record_timestamp from dev_net.device_location_update where mac_address != '' group by mac_address) t join dev_net.device_location_update as p on p.mac_address = t.mac_address and p.record_timestamp = t.record_timestamp join (select mac_address, max(record_timestamp) as record_timestamp from dev_net.profile_update where mac_address != '' group by mac_address) tpu on p.mac_address = tpu.mac_address join dev_net.profile_update as pu on pu.mac_address = tpu.mac_address and pu.record_timestamp = tpu.record_timestamp where t.record_timestamp > " + fromTime + " order by pu.first_name, pu.last_name";
            log.debug("executing query :: " + query);
            records = Redshift.query(query, new HashMap<>(), 3);
            if (records != null) {
                ObjectMapper mapper = new ObjectMapper();
                jsonString = mapper.writeValueAsString(records);
            }
        } catch (Exception e) {
            response.setStatusCode(400);
            response.putHeader("content-type", "application/json")
                    .end("{\"mac\":\"" + mac + "\"}");
            return;
        }
        response.setStatusCode(200);
        response.putHeader("content-type", "application/json");
        response.end(jsonString);

    }


}
