package com.fbudassi.neddy.benchmark.handler;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpResponseHandler extends SimpleChannelUpstreamHandler {

    private static final Logger logger = LoggerFactory.getLogger(HttpResponseHandler.class);

    /**
     * Executed when an exception is caught.
     *
     * @param ctx
     * @param e
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        logger.error("Error in HttpResponseHandler", e.getCause());
    }
}