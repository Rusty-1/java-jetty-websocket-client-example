package net.rusty1;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

// 
// Java WebSocket Client API (javax.websocket) [JSR-356] Client
//
// References: https://www.eclipse.org/jetty/documentation/current/jetty-websocket-client-api.html


@WebSocket(maxTextMessageSize = 64 * 1024)
public class WebSocketHandler {
    
    private final CountDownLatch closeLatch;

    private static int count = 0;

    private Logger logger;

    public WebSocketHandler() {
        this.logger = Logger.getLogger(WebSocketHandler.class);
        logger.debug( String.format( "Create WebSocket [%d]", ++count ));
        closeLatch = new CountDownLatch(1);
    }

    public boolean awaitClose(int duration, TimeUnit unit) throws InterruptedException
    {
        return this.closeLatch.await(duration, unit);
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        logger.debug( String.format( ("Close: statusCode=" + statusCode + ", reason=" + reason)));
        this.closeLatch.countDown();
    }

    @OnWebSocketError
    public void onError(Throwable t) {
        logger.error( String.format("Error: %s", t.getMessage()), t);
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        logger.debug( String.format( "Connect: " + session.getRemoteAddress().getAddress()));
        try {
            session.getRemote().sendString(String.format("Hello WebSocket client: [%s]", 
                    DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL).format(new Date())
                ));
        } catch (IOException e) {
            logger.error( String.format("Error sending message: %s", e.getMessage()), e);
        }
    }

    @OnWebSocketMessage
    public void onMessage(String message) {
        logger.debug( String.format( "Message: " + message));
    }
}