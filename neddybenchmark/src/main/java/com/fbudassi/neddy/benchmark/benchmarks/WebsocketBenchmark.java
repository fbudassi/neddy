package com.fbudassi.neddy.benchmark.benchmarks;

import com.fbudassi.neddy.benchmark.NeddyBenchmark;
import com.fbudassi.neddy.benchmark.bean.ListenerActionBean;
import com.fbudassi.neddy.benchmark.bean.ListenerActionBean.ListenerActionEnum;
import com.fbudassi.neddy.benchmark.config.Config;
import com.fbudassi.neddy.benchmark.pipeline.WebsocketPipelineFactory;
import com.google.gson.Gson;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Random;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;
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
    private static Gson gson = new Gson();
    private static Random random = new Random();
    // Configuration constants.
    private static final int SERVER_PORT = Config.getIntValue(Config.KEY_SERVER_PORT);
    private static final String SERVER_ADDRESS = Config.getValue(Config.KEY_SERVER_ADDRESS);
    private static final int NUMLISTENERS = Config.getIntValue(Config.KEY_NUMLISTENERS);
    private static final int NUMCATEGORIES = Config.getIntValue(Config.KEY_LISTENER_NUMCATEGORIES);
    private static final String RESOURCE_LISTENER = Config.getValue(Config.KEY_RESOURCE_LISTENER);
    // URI where to connect the websocket.
    private static URI uri;
    // Websocket handshaker.
    private static WebSocketClientHandshaker handshaker;

    /**
     * Websocket Benchmark constructor.
     */
    public WebsocketBenchmark() throws URISyntaxException {
        // URL of the server, with the resource path
        uri = new URI("ws://" + SERVER_ADDRESS + ":" + SERVER_PORT + "/" + RESOURCE_LISTENER);

        // Connect with V13 (RFC 6455 aka HyBi-17).
        handshaker = new WebSocketClientHandshakerFactory().newHandshaker(
                getUri(), WebSocketVersion.V13, null, false, null);
    }

    /**
     * Executes the benchmark.
     */
    @Override
    public void execute() throws Exception {
        // Open some Websocket channels to Neddy.
        for (int l = 0; l < NUMLISTENERS; l++) {
            // Open a Websocket channel to the server.
            ChannelFuture future = NeddyBenchmark.getBootstrap().connect(
                    new InetSocketAddress(uri.getHost(), uri.getPort()));
            future.syncUninterruptibly();
            Channel ch = future.getChannel();
            handshaker.handshake(ch).syncUninterruptibly();

            // Request the list of categories.
            getCategories(ch);
        }

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
     * Configure the Netty bootstrap for the best behavior in this benchmark.
     *
     * @param bootstrap
     */
    @Override
    public void configureBootstrap(ClientBootstrap bootstrap) {
        // Nothing is necessary to be done for the Websocket benchmark.
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

    /**
     * Request the categories to the server.
     *
     * @param ch
     */
    private void getCategories(Channel ch) {
        ListenerActionBean listenerActionBean = new ListenerActionBean();
        listenerActionBean.setAction(ListenerActionEnum.GET_CATEGORIES.toString());
        ch.write(new TextWebSocketFrame(gson.toJson(listenerActionBean)));
    }

    /**
     * Subscribes the channel to a number of categories in the list, randomly
     * choosing among them.
     *
     * @param ch
     * @param categories
     */
    public static void subscribeToCategories(Channel ch, List<String> categories) {
        for (int n = 0; n < NUMCATEGORIES; n++) {
            ListenerActionBean listenerActionBean = new ListenerActionBean();
            listenerActionBean.setAction(ListenerActionEnum.SUBSCRIBE.toString());
            listenerActionBean.setCategory(categories.get(random.nextInt(categories.size())));
            ch.write(new TextWebSocketFrame(gson.toJson(listenerActionBean)));
        }
    }
}
