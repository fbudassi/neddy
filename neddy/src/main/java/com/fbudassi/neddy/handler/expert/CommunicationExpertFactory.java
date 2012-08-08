package com.fbudassi.neddy.handler.expert;

import com.fbudassi.neddy.config.Config;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import org.jboss.netty.handler.codec.http.HttpHeaders.Names;
import org.jboss.netty.handler.codec.http.HttpHeaders.Values;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;

/**
 * It decides which CommunicationExpert must be instantiated, depending on the
 * request.
 *
 * @author fbudassi
 */
public class CommunicationExpertFactory {

    private static final int PORT_SPEAKERS = Config.getIntValue(Config.KEY_PORT_SPEAKERS);

    /**
     * It returns the adequate expert to handle the request based on the class
     * type of msg and the local IP Port.
     *
     * @param msg
     * @param localAddress
     * @return
     */
    public static CommunicationExpert getExpert(Object msg, SocketAddress localAddress) {
        int localPort = ((InetSocketAddress) localAddress).getPort();

        if (localPort == PORT_SPEAKERS) {
            return new RestExpert();
        } else {
            if (msg instanceof HttpRequest) {
                // Handle HTTP static content requests and WebSocket handshakes.
                if (Values.WEBSOCKET.equalsIgnoreCase(((HttpRequest) msg).getHeader(Names.UPGRADE))) {
                    return new WebSocketExpert();
                } else {
                    return new StaticContentExpert();
                }
            } else if (msg instanceof WebSocketFrame) {
                // Handle a Websocket frame.
                return new WebSocketExpert();
            }
        }
        return null;
    }
}
