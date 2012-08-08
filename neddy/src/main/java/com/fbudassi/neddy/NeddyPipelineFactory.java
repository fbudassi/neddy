package com.fbudassi.neddy;

import com.fbudassi.neddy.config.Config;
import com.fbudassi.neddy.handler.AddToOpenedChannelsHandler;
import com.fbudassi.neddy.handler.IdleKeepAliveHandler;
import com.fbudassi.neddy.handler.NeddyHandler;
import java.util.concurrent.TimeUnit;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import static org.jboss.netty.channel.Channels.pipeline;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.stream.ChunkedWriteHandler;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.util.Timer;

/**
 * Builds the pipeline of handlers used to analyze the Listeners and Speakers
 * request and create the response.
 *
 * @author fbudassi
 */
public class NeddyPipelineFactory implements ChannelPipelineFactory {

    private static final int KEEPALIVE_TIMEOUT = Config.getIntValue(Config.KEY_KEEPALIVE_TIMEOUT);
    private final ChannelHandler idleStateHandler;
    private final Timer timer;
    //Handler constants
    public static final String HANDLER_DECODER = "decoder";
    public static final String HANDLER_AGGREGATOR = "aggregator";
    public static final String HANDLER_ENCODER = "encoder";
    public static final String HANDLER_CHUNKED_WRITE = "chunkedWriter";
    public static final String HANDLER_ADD_TO_OPENED_CHANNELS = "addToOpenedChannelsHandler";
    public static final String HANDLER__NEDDY = "neddyHandler";
    public static final String HANDLER_IDLE_STATE = "idleStateHandler";
    public static final String HANDLER_IDLE_KEEP_ALIVE = "idleKeepAliveHandler";

    /**
     * Constructor.
     *
     * @param t
     */
    public NeddyPipelineFactory(Timer t) {
        this.timer = t;
        this.idleStateHandler = new IdleStateHandler(this.timer, 0, 0,
                KEEPALIVE_TIMEOUT, TimeUnit.MILLISECONDS); // timer must be shared
    }

    /**
     * Configuration for the Neddy pipeline.
     *
     * @return
     * @throws Exception
     */
    @Override
    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline = pipeline();
        pipeline.addLast(HANDLER_DECODER, new HttpRequestDecoder());
        pipeline.addLast(HANDLER_AGGREGATOR, new HttpChunkAggregator(65536));
        pipeline.addLast(HANDLER_ENCODER, new HttpResponseEncoder());
        pipeline.addLast(HANDLER_CHUNKED_WRITE, new ChunkedWriteHandler());    // Need to be removed when Websocket handshake takes place
        pipeline.addLast(HANDLER_ADD_TO_OPENED_CHANNELS, new AddToOpenedChannelsHandler());
        pipeline.addLast(HANDLER__NEDDY, new NeddyHandler());
        pipeline.addLast(HANDLER_IDLE_STATE, this.idleStateHandler);            // Need to be removed when Websocket handshake takes place
        pipeline.addLast(HANDLER_IDLE_KEEP_ALIVE, new IdleKeepAliveHandler());  // Need to be removed when Websocket handshake takes place
        return pipeline;
    }
}
