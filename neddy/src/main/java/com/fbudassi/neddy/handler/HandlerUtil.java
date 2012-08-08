package com.fbudassi.neddy.handler;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.handler.codec.http.*;
import org.jboss.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Some utility static methods shared for this package and its children.
 *
 * @author fbudassi
 */
public class HandlerUtil {

    private static final Logger logger = LoggerFactory.getLogger(HandlerUtil.class);

    /**
     * Handy method to inform of an error processing the request and close the
     * connection.
     *
     * @param channel
     * @param status
     */
    public static void sendError(Channel channel, HttpResponseStatus status) {
        // Log some information about the reason.
        logger.info("Error in request: {} - {}", status.getCode(), status.getReasonPhrase());

        // Send error response.
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, status);
        response.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.setContent(ChannelBuffers.copiedBuffer(
                "Failure: " + status.toString() + "\r\n",
                CharsetUtil.UTF_8));

        // Close the connection as soon as the error message is sent.
        channel.write(response).addListener(ChannelFutureListener.CLOSE);
    }
}
