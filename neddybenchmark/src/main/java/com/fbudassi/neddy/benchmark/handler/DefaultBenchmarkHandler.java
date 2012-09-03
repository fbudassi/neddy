package com.fbudassi.neddy.benchmark.handler;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A very simple channel handler that just logs an exception in the channel when
 * it is caught.
 *
 * @author fbudassi
 */
public class DefaultBenchmarkHandler extends SimpleChannelUpstreamHandler {

    private static final Logger logger = LoggerFactory.getLogger(DefaultBenchmarkHandler.class);

    /**
     * Executed when an exception is caught.
     *
     * @param ctx
     * @param e
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        logger.error("Error in handler", e.getCause());
    }
}