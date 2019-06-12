package com.cisco.dnaspaces.server;

import com.cisco.dnaspaces.server.handler.APIHandler;
import com.cisco.dnaspaces.utils.ConfigUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HTTPVerticle extends AbstractVerticle {

    private static final Logger log = LogManager.getLogger(HTTPVerticle.class);
    public void start() {
        Integer httpPort = Integer.parseInt(ConfigUtil.getConfig().getProperty("http.port"));
        HttpServerOptions options = new HttpServerOptions();

        Router router = Router.router(vertx);
        router.mountSubRouter("/api/v1", new APIHandler().router(vertx));


        vertx.createHttpServer(options).requestHandler(router).listen(httpPort);

    }

}