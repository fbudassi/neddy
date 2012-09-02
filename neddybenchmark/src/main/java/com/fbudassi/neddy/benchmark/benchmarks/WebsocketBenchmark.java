package com.fbudassi.neddy.benchmark.benchmarks;

import com.fbudassi.neddy.benchmark.config.Config;
import com.fbudassi.neddy.benchmark.pipeline.WebsocketPipelineFactory;
import java.net.URI;
import java.net.URISyntaxException;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author fbudassi
 */
public class WebsocketBenchmark implements Benchmark {

    private static final Logger logger = LoggerFactory.getLogger(WebsocketBenchmark.class);
    // Configuration constants.
    private static final int SERVER_PORT = Config.getIntValue(Config.KEY_SERVER_PORT);
    private static final String SERVER_ADDRESS = Config.getValue(Config.KEY_SERVER_ADDRESS);
    private static final String RESOURCE_LISTENER = Config.getValue(Config.KEY_RESOURCE_LISTENER);
    // URI where to connect the websocket.
    private static URI uri;
    // Websocket handshaker.
    private static WebSocketClientHandshaker handshaker;

    /**
     * Websocket Benchmark constructor.
     */
    public WebsocketBenchmark() throws URISyntaxException {
        uri = new URI("ws://" + SERVER_ADDRESS + ":" + SERVER_PORT + "/" + RESOURCE_LISTENER);

        // Connect with V13 (RFC 6455 aka HyBi-17).
        handshaker = new WebSocketClientHandshakerFactory().newHandshaker(
                getUri(), WebSocketVersion.V13, null, false, null);
    }

    /**
     * Executes the benchmark.
     */
    @Override
    public void execute() {
    }

    /**
     * Returns the pipeline for Websocket benchmark.
     *
     * @return
     */
    @Override
    public ChannelPipelineFactory getPipeline() {
        return new WebsocketPipelineFactory(getHandshaker());
    }

    /**
     * @return the uri
     */
    public static URI getUri() {
        return uri;
    }

    /**
     * @return the handshaker
     */
    public static WebSocketClientHandshaker getHandshaker() {
        return handshaker;
    }
}
