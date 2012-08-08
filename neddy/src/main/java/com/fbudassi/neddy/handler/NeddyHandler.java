package com.fbudassi.neddy.handler;

import com.fbudassi.neddy.handler.expert.CommunicationExpert;
import com.fbudassi.neddy.handler.expert.CommunicationExpertFactory;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.frame.TooLongFrameException;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listener and Speaker request handler in the NeddyPipeline.
 *
 * @author fbudassi
 */
public class NeddyHandler extends SimpleChannelUpstreamHandler {

    private static final Logger logger = LoggerFactory.getLogger(NeddyHandler.class);
    //Expert that is going to handle this connection.
    private CommunicationExpert expert;

    /**
     * Executed when a request is received.
     *
     * @param ctx
     * @param e
     * @throws Exception
     */
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        CommunicationExpert currentExpert = CommunicationExpertFactory.getExpert(e.getMessage(), e.getChannel().getLocalAddress());
        if (expert == null || currentExpert.getClass() != expert.getClass()) {
            expert = currentExpert;
        }
        expert.handleRequest(ctx, e);
    }

    /**
     * Executed when an exception is caught.
     *
     * @param ctx
     * @param e
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        Channel ch = e.getChannel();
        Throwable cause = e.getCause();
        if (cause instanceof TooLongFrameException) {
            HandlerUtil.sendError(ctx.getChannel(), HttpResponseStatus.BAD_REQUEST);
            return;
        }

        if (ch.isConnected()) {
            HandlerUtil.sendError(ctx.getChannel(), HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }
        logger.error("Error in Handler", cause);
    }
}
