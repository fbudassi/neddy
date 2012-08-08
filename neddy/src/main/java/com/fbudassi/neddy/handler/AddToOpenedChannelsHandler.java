package com.fbudassi.neddy.handler;

import com.fbudassi.neddy.Neddy;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

/**
 * @author fbudassi
 */
public class AddToOpenedChannelsHandler extends SimpleChannelUpstreamHandler {

    /**
     * Executed when the channel is opened.
     *
     * @param ctx
     * @param e
     */
    @Override
    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) {
        // Add the new opened channel to the neddy ChannelGroup.
        Neddy.getAllChannels().add(e.getChannel());

        // Send the event upstream.
        ctx.sendUpstream(e);
    }
}
