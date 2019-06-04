package com.cisco.dnaspaces;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

/*
@see https://github.com/TooTallNate/Java-WebSocket
 */
public class WSServer extends WebSocketServer {

    private static Logger log = LogManager.getLogger(WSServer.class);
    private static WSServer wsServer = null;


    public WSServer(int port ) throws UnknownHostException {
        super( new InetSocketAddress( port ) );
    }

    public WSServer(InetSocketAddress address ) {
        super( address );
    }

    @Override
    public void onOpen( WebSocket conn, ClientHandshake handshake ) {
        conn.send("Welcome to the server!"); //This method sends a message to the new client
        broadcast( "new connection: " + handshake.getResourceDescriptor() ); //This method sends a message to all clients connected
        log.info( conn.getRemoteSocketAddress().getAddress().getHostAddress() + " entered the room!" );
    }

    @Override
    public void onClose( WebSocket conn, int code, String reason, boolean remote ) {
        broadcast( conn + " has left the room!" );
        log.info( conn + " has left the room!" );
    }

    @Override
    public void onMessage( WebSocket conn, String message ) {
        // not listening to any of incoming messages
    }
    @Override
    public void onMessage( WebSocket conn, ByteBuffer message ) {
        // not listening to any of incoming messages
    }

    @Override
    public void onError( WebSocket conn, Exception ex ) {
        ex.printStackTrace();
        if( conn != null ) {
            // some errors like port binding failed may not be assignable to a specific websocket
        }
    }

    @Override
    public void onStart() {
        setConnectionLostTimeout(0);
        setConnectionLostTimeout(100);
    }

    public static WSServer getWsServer(int port) throws UnknownHostException {
        if(wsServer != null)
            return wsServer;
        log.debug("creating Web Socket server WSServer on port :: " + port);
        wsServer = new WSServer( port );
        wsServer.start();
        log.info( "wsServer started on port: " + wsServer.getPort() );
        return wsServer;
    }


}