package com.fbudassi.neddy.handler.expert;

import com.fbudassi.neddy.config.Config;
import com.fbudassi.neddy.util.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.*;
import org.slf4j.Logger;

/**
 * Abstract class implemented by all the experts that give a response in
 * NeddyServerHandler.
 *
 * @author fbudassi
 */
public abstract class CommunicationExpert {

    /**
     *
     */
    protected static final String SERVERNAME = Config.getValue(Config.KEY_SERVERNAME);

    /**
     * Do whatever is necessary to give a response to the request.
     *
     * @param ctx
     * @param messageEvent
     * @throws Exception
     */
    public abstract void handleRequest(ChannelHandlerContext ctx, MessageEvent messageEvent) throws Exception;

    /**
     * It logs some request debug information.
     *
     * @param logger
     * @param channel
     * @param request
     */
    protected static void logRequestInfo(Logger logger, Channel channel, HttpRequest request) {
        // Log client information and URI requested.
        String remoteAddress = channel.getRemoteAddress().toString();
        String userAgent = request.getHeader(HttpHeaders.Names.USER_AGENT);
        logger.info("{} - {} - requested: {} {}",
                new Object[]{remoteAddress, userAgent, request.getMethod().toString(), request.getUri()});

    }

    /**
     * Builds the basic HTTP headers for the OPTIONS Http Method.
     *
     * @param request
     * @param allowedMethods
     * @return
     */
    protected HttpResponse buildOptionsResponseHeaders(HttpRequest request, Enum[] allowedMethods) {
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);

        // Set some HTTP Headers.
        HttpHeaders.setHeader(response, HttpHeaders.Names.DATE, DateUtil.getCurrent());
        HttpHeaders.setHeader(response, HttpHeaders.Names.SERVER, SERVERNAME);
        HttpHeaders.setHeader(response, HttpHeaders.Names.ALLOW, StringUtils.join(allowedMethods, ", "));
        HttpHeaders.setContentLength(response, 0);

        // Workaround for Apache Benchmark bug.
        // See http://blog.lolyco.com/sean/2009/11/25/ab-apache-bench-hanging-with-k-keep-alive-switch/
        if (HttpHeaders.isKeepAlive(request)) {
            HttpHeaders.setHeader(response, HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        } else {
            HttpHeaders.setHeader(response, HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
        }

        return response;
    }
}
