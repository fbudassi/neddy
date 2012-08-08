package com.fbudassi.neddy.benchmark;

import com.fbudassi.neddy.benchmark.config.Config;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NeddyBenchmark implements Shutdownable {

    private static final Logger logger = LoggerFactory.getLogger(NeddyBenchmark.class);
    // Resources to be freed when shutdown happens.
    private ClientBootstrap bootstrap;
    private static ChannelGroup ALL_CHANNELS;
    // General configuration variables.
    private static final boolean KEEPALIVE = Config.getBooleanValue(Config.KEY_KEEPALIVE);
    private static final boolean TCPNODELAY = Config.getBooleanValue(Config.KEY_TCPNODELAY);
    private static final int TIMEOUT = Config.getIntValue(Config.KEY_TIMEOUT);
    private static final String USERAGENT = Config.getValue(Config.KEY_USERAGENT);
    private static final int NUMADDRESSES = Config.getIntValue(Config.KEY_NUMADDRESSES);
    private static final int NUMPORTS = Config.getIntValue(Config.KEY_NUMPORTS);
    private static final int DELAY = Config.getIntValue(Config.KEY_DELAY);
    // Client configuration variables.
    private static final int CLIENT_PORTSTART = Config.getIntValue(Config.KEY_CLIENT_PORTSTART);
    private static final String CLIENT_BASEADDRESS = Config.getValue(Config.KEY_CLIENT_BASEADDRESS);
    // Server configuration variables.
    private static final int SERVER_PORT = Config.getIntValue(Config.KEY_SERVER_PORT);
    private static final String SERVER_PATH = Config.getValue(Config.KEY_SERVER_PATH);
    private static final String SERVER_ADDRESS = Config.getValue(Config.KEY_SERVER_ADDRESS);
    // Statistic variables.
    private static int totalConnections = NUMADDRESSES * NUMPORTS;
    private static int openConnections = 0;

    /**
     * Static constructor.
     */
    static {
        // ChannelGroup of all open channels (server + clients).
        setAllChannels(new DefaultChannelGroup("neddybenchmark"));
    }

    /**
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) {
        try {
            new NeddyBenchmark().start();
        } catch (InterruptedException ex) {
            logger.error("Error in NeddyBenchmark.", ex);
        }
    }

    /**
     *
     * @param args
     */
    public void start() throws InterruptedException {
        logger.info("Starting up {}.", NeddyBenchmark.class.getSimpleName());
        logger.info("Trying to generate {} connections to the server", totalConnections);

        // Registers a shutdown hook to free resources of this class.
        Runtime.getRuntime().addShutdownHook(new ShutdownThread(this, "NettyBenchmark Shutdown Thread"));

        // Configure the client bootstrap.
        setBootstrap(new ClientBootstrap(
                new NioClientSocketChannelFactory(
                Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool())));

        // Set up the event pipeline factory.
        getBootstrap().setPipelineFactory(new NeddyBenchmarkPipelineFactory());

        // Set some necessary or convenient socket options.
        // http://download.oracle.com/javase/6/docs/api/java/net/SocketOptions.html
        getBootstrap().setOption("tcpNoDelay", TCPNODELAY); // disable Nagle's algorithm
        getBootstrap().setOption("keepAlive", KEEPALIVE);  // keep alive connections
        getBootstrap().setOption("connectTimeoutMillis", TIMEOUT); // connection timeout

        // Get the first three octets by one side and the last one by the other side.
        String clientIpBase = CLIENT_BASEADDRESS.substring(0, CLIENT_BASEADDRESS.lastIndexOf(".") + 1);
        byte clientIpLastOctet = Byte.parseByte(CLIENT_BASEADDRESS.substring(
                CLIENT_BASEADDRESS.lastIndexOf(".") + 1, CLIENT_BASEADDRESS.length()));

        //IP addresses loop
        String clientIp;
        for (int i = 0; i < NUMADDRESSES; i++) {
            // Build client ip.
            clientIp = clientIpBase + clientIpLastOctet;

            //Ports loop
            int lastPort = CLIENT_PORTSTART + NUMPORTS;
            for (int port = CLIENT_PORTSTART; port <= lastPort; port++) {
                // Start the connection attempt.
                ChannelFuture future = getBootstrap().connect(
                        new InetSocketAddress(SERVER_ADDRESS, SERVER_PORT),
                        new InetSocketAddress(clientIp, port));

                // Add new channel to the group.
                getAllChannels().add(future.getChannel());

                // Wait until the connection attempt succeeds or fails.
                Channel channel = future.awaitUninterruptibly().getChannel();
                if (!future.isSuccess()) {
                    logger.error("Connection attempt not successful.", future.getCause());
                    return;
                }

                // Prepare the HTTP request.
                HttpRequest request = new DefaultHttpRequest(
                        HttpVersion.HTTP_1_1, HttpMethod.GET, SERVER_PATH);
                request.setHeader(HttpHeaders.Names.HOST, SERVER_ADDRESS);
                request.setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
                request.setHeader(HttpHeaders.Names.USER_AGENT, USERAGENT);

                // Send the HTTP request.
                channel.write(request);

                // Increment open connections variable and print the number once in a while.
                openConnections++;
                if ((((double) openConnections * 100 / totalConnections) % 1) == 0) {
                    logger.info("There are {} open connections so far.", openConnections);
                }

                // Delay between every connection (give the server some breath).
                Thread.sleep(DELAY);
            }

            // Increment last octet.
            clientIpLastOctet++;
        }
    }

    /**
     * Frees all the server resources.
     */
    @Override
    public void shutdown() {
        // Close all connections.
        ChannelGroupFuture groupFuture = NeddyBenchmark.getAllChannels().close();
        groupFuture.awaitUninterruptibly();

        // Shutdown the selector loop (boss and workers).
        getBootstrap().getFactory().releaseExternalResources();
    }

    /**
     * @return the bootstrap
     */
    public ClientBootstrap getBootstrap() {
        return bootstrap;
    }

    /**
     * @param bootstrap the bootstrap to set
     */
    public void setBootstrap(ClientBootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    /**
     * @return the ALL_CHANNELS
     */
    public static ChannelGroup getAllChannels() {
        return ALL_CHANNELS;
    }

    /**
     * @param allChannels the ALL_CHANNELS to set
     */
    public static void setAllChannels(ChannelGroup allChannels) {
        ALL_CHANNELS = allChannels;
    }
}
