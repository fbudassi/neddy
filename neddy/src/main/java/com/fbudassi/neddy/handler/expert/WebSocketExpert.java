package com.fbudassi.neddy.handler.expert;

import com.fbudassi.neddy.NeddyPipelineFactory;
import com.fbudassi.neddy.action.ListenerActionHandler;
import com.fbudassi.neddy.config.Config;
import com.fbudassi.neddy.handler.HandlerUtil;
import com.google.gson.Gson;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.websocketx.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * It concentrates all the methods related to the WebSocket functionality.
 *
 * @author fbudassi
 */
public class WebSocketExpert extends CommunicationExpert {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketExpert.class);
    private WebSocketServerHandshaker handshaker;
    // WebSocket path and scheme.
    private static final String WEBSOCKET_SCHEME = "ws";
    private static final String WEBSOCKET_PATH = Config.getValue(Config.KEY_RESOURCE_LISTENER);

    /**
     * It handles Websocket requests.
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handleRequest(ChannelHandlerContext ctx, MessageEvent messageEvent) throws Exception {
        if (handshaker == null) {
            handleWebSocketHandshake(ctx, (HttpRequest) messageEvent.getMessage());
        } else {
            handleWebSocketFrame(ctx, (WebSocketFrame) messageEvent.getMessage());
        }
    }

    /**
     * Gets a WebSocket Text Frame with a string message embedded.
     *
     * @param message
     * @return
     */
    public static TextWebSocketFrame getTextWebSocketFrame(String message) {
        return new TextWebSocketFrame(message);
    }

    /**
     * It specifically handles WebSocket handshakes.
     *
     * @param ctx
     * @param request
     */
    private void handleWebSocketHandshake(ChannelHandlerContext ctx, HttpRequest request) {
        // Log debug info.
        logRequestInfo(logger, ctx.getChannel(), request);

        // Only GET method is supported here.
        if (request.getMethod() != HttpMethod.GET) {
            HandlerUtil.sendError(ctx.getChannel(), HttpResponseStatus.METHOD_NOT_ALLOWED);
            return;
        }

        // Upgrade the Http connection to a WebSocket connection.
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                getWebSocketURL(request), null, false);
        handshaker = wsFactory.newHandshaker(request);
        if (handshaker == null) {
            wsFactory.sendUnsupportedWebSocketVersionResponse(ctx.getChannel());
        } else {
            ChannelPipeline pipeline = ctx.getPipeline();
            pipeline.remove(NeddyPipelineFactory.HANDLER_CHUNKED_WRITE);
            pipeline.remove(NeddyPipelineFactory.HANDLER_IDLE_STATE);
            pipeline.remove(NeddyPipelineFactory.HANDLER_IDLE_KEEP_ALIVE);
            handshaker.handshake(ctx.getChannel(), request).addListener(WebSocketServerHandshaker.HANDSHAKE_LISTENER);
        }
    }

    /**
     * It handles all the incoming messages that are WebSocket Frames.
     *
     * @param ctx
     * @param frame
     */
    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        // Check for closing frame
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.getChannel(), (CloseWebSocketFrame) frame);
            return;
        } else if (frame instanceof PingWebSocketFrame) {
            ctx.getChannel().write(new PongWebSocketFrame(frame.getBinaryData()));
            return;
        } else if (!(frame instanceof TextWebSocketFrame)) {
            throw new UnsupportedOperationException(String.format("%s frame type not supported.", frame.getClass().getName()));
        }

        // Process frame
        Gson gson = new Gson();
        String request = ((TextWebSocketFrame) frame).getText();
        String response = gson.toJson(ListenerActionHandler.handleRequest(request, ctx.getChannel()));
        if (response != null) {
            ctx.getChannel().write(new TextWebSocketFrame(response));
        }
    }

    /**
     * Returns the WebSocket server absolute path.
     *
     * @param req
     * @return
     */
    private static String getWebSocketURL(HttpRequest req) {
        return WEBSOCKET_SCHEME + "://" + req.getHeader(HttpHeaders.Names.HOST) + "/" + WEBSOCKET_PATH;
    }
}
