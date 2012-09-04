package com.fbudassi.neddy.benchmark.benchmarks;

import com.fbudassi.neddy.benchmark.NeddyBenchmark;
import com.fbudassi.neddy.benchmark.config.Config;
import com.fbudassi.neddy.benchmark.pipeline.DefaultHttpPipelineFactory;
import java.net.InetSocketAddress;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Static content benchmark. Please go to the properties file for configurable
 * parameters.
 *
 * @author fbudassi
 */
public class StaticContentBenchmark implements Benchmark {

    private static final Logger logger = LoggerFactory.getLogger(StaticContentBenchmark.class);
    // Bootstrap socket options variables.
    private static final boolean KEEPALIVE = Config.getBooleanValue(Config.KEY_KEEPALIVE);
    private static final boolean TCPNODELAY = Config.getBooleanValue(Config.KEY_TCPNODELAY);
    private static final int TIMEOUT = Config.getIntValue(Config.KEY_TIMEOUT);
    // General configuration variables.
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
     * Executes the static content benchmark with the configured parameters in
     * the properties file.
     *
     * @throws InterruptedException
     */
    @Override
    public void execute() throws InterruptedException {
        logger.info("Trying to generate {} connections to the server", totalConnections);

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
                ChannelFuture future = NeddyBenchmark.getBootstrap().connect(
                        new InetSocketAddress(SERVER_ADDRESS, SERVER_PORT),
                        new InetSocketAddress(clientIp, port));

                // Add new channel to the group.
                NeddyBenchmark.getAllChannels().add(future.getChannel());

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
     * Returns the Static Content benchmark pipeline.
     */
    @Override
    public ChannelPipelineFactory getPipeline() {
        return new DefaultHttpPipelineFactory();
    }

    /**
     * Configure the Netty bootstrap for the best behavior in this benchmark.
     *
     * @param bootstrap
     */
    @Override
    public void configureBootstrap(ClientBootstrap bootstrap) {
        // Set some necessary or convenient socket options.
        // http://download.oracle.com/javase/6/docs/api/java/net/SocketOptions.html
        bootstrap.setOption("tcpNoDelay", TCPNODELAY); // disable Nagle's algorithm
        bootstrap.setOption("keepAlive", KEEPALIVE);  // keep alive connections
        bootstrap.setOption("connectTimeoutMillis", TIMEOUT); // connection timeout
    }
}
