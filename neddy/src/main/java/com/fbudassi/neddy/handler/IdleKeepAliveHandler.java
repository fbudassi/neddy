package com.fbudassi.neddy.handler;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.timeout.IdleStateAwareChannelHandler;
import org.jboss.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles keep-alive connections whose timer expired due to lack of activity.
 * This class handles the IdleStateEvent triggered by IdleStateHandler.
 *
 * @author fbudassi
 */
public class IdleKeepAliveHandler extends IdleStateAwareChannelHandler {

    private static final Logger logger = LoggerFactory.getLogger(IdleKeepAliveHandler.class);

    @Override
    public void channelIdle(ChannelHandlerContext ctx, IdleStateEvent e) {
        // Logs some information related to the closing channel.
        String remoteAddress = ctx.getChannel().getRemoteAddress().toString();
        logger.debug("Connection with {} has expired.", remoteAddress);

        // Closes the idle channel.
        e.getChannel().close();
    }
}