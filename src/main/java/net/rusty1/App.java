package net.rusty1;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.jetty.util.ssl.SslContextFactory.Client;



/**
 * WebSocket Client example using Jetty WebSocketClient
 *
 * See: https://www.eclipse.org/jetty/documentation/current/jetty-websocket-client-api.html
 *      https://docs.oracle.com/javase/10/security/java-secure-socket-extension-jsse-reference-guide.htm#JSSEC-GUID-93DEEE16-0B70-40E5-BBE7-55C3FD432345
 * 
 */
public class App 
{
    //
    // Public web socket envrionment: https://www.websocket.org/echo.html
    final private static String destUri = "wss://echo.websocket.org";

    public static void main( String[] args ) throws Exception
    {
        BasicConfigurator.configure();
        Logger logger = Logger.getLogger(App.class);

        logger.info("Starting WebSocket Client");

        //
        // SSL Context factory required to enable a secure WebSocket connection
        SslContextFactory sslClient = new Client(true);
        sslClient.setEndpointIdentificationAlgorithm("HTTPS");

        //
        // This is a security concern. The site only support weak ciphers and so
        // we are required to clear the exclusion list to permit the use of all ciphers
        sslClient.setExcludeCipherSuites();

        //
        // Client protocol to be used
        HttpClient httpClient = new HttpClient(sslClient);

        //
        // Create a Jetty WebSocket client
        WebSocketClient webSocketClient = new WebSocketClient(httpClient);

        //
        // Our application handler to respond to socket events
        WebSocketHandler webSocketHandler = new WebSocketHandler();

        try{
            webSocketClient.start();

            URI echoUri = new URI(destUri);

            ClientUpgradeRequest request = new ClientUpgradeRequest();

            //
            // The seeion can be used to gracefully close the connection from the client side.
            // The example WebSocket server closes the current WebSocket after replying so we dont
            // need it in this example.
            Session session = webSocketClient.connect(webSocketHandler, echoUri, request).get();

            //
            // Connection information
            logger.info(String.format("Connecting to   : %s", echoUri));
            logger.info(String.format("SSL Cipher used : %s", httpClient.getSslContextFactory().getSelectedCipherSuites()[0]));

            webSocketHandler.awaitClose(5, TimeUnit.SECONDS);
        }
        catch(Throwable t) {
            logger.error("WebSocket failed", t);
        }
        finally {
            webSocketClient.stop();
        }

        logger.info("WebSocket Client Done !");
    }
}
