package com.fbudassi.neddy.benchmark.pipeline;

import com.fbudassi.neddy.benchmark.handler.WebSocketClientHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import static org.jboss.netty.channel.Channels.pipeline;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;

/**
 *
 * @author fbudassi
 */
public class WebsocketPipelineFactory implements ChannelPipelineFactory {

    private final WebSocketClientHandshaker handshaker;

    /**
     * The constructor need a Websocket handshaker.
     *
     * @param handshaker
     */
    public WebsocketPipelineFactory(WebSocketClientHandshaker handshaker) {
        this.handshaker = handshaker;
    }

    /**
     * Generates a Websocket Pipeline Factory. All the necessary parameters are
     * extracted from the config file.
     *
     * @return
     * @throws Exception
     */
    @Override
    public ChannelPipeline getPipeline() throws Exception {
        // Create a default pipeline implementation.
        ChannelPipeline pipeline = pipeline();

        // Add necessary handlers to the pipeline.
        pipeline.addLast("decoder", new HttpResponseDecoder());
        pipeline.addLast("encoder", new HttpRequestEncoder());
        pipeline.addLast("ws-handler", new WebSocketClientHandler(this.handshaker));
        return pipeline;
    }
}