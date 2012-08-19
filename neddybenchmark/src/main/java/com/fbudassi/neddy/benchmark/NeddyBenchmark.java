package com.fbudassi.neddy.benchmark;

import com.fbudassi.neddy.benchmark.config.Config;
import com.fbudassi.neddy.benchmark.rest.RestBenchmark;
import com.fbudassi.neddy.benchmark.staticcontent.StaticContentBenchmark;
import com.fbudassi.neddy.benchmark.websocket.WebsocketBenchmark;
import java.util.concurrent.Executors;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NeddyBenchmark implements Shutdownable {

    private static final Logger logger = LoggerFactory.getLogger(NeddyBenchmark.class);

    //Allowed command line parameters
    private enum ParameterEnum {

        STATIC, WS, REST
    }
    // Resources to be freed when shutdown happens.
    private static ClientBootstrap bootstrap;
    private static ChannelGroup allChannels;
    // General configuration variables.
    private static final boolean KEEPALIVE = Config.getBooleanValue(Config.KEY_KEEPALIVE);
    private static final boolean TCPNODELAY = Config.getBooleanValue(Config.KEY_TCPNODELAY);
    private static final int TIMEOUT = Config.getIntValue(Config.KEY_TIMEOUT);

    /**
     * Static constructor.
     */
    static {
        // Configure the client bootstrap.
        setBootstrap(new ClientBootstrap(
                new NioClientSocketChannelFactory(
                Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool())));

        // ChannelGroup of all open channels (server + clients).
        setAllChannels(new DefaultChannelGroup("neddybenchmark"));
    }

    /**
     * Benchmark application starting point.
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) {
        try {
            new NeddyBenchmark().start(args);
        } catch (InterruptedException ex) {
            logger.error("Error in NeddyBenchmark.", ex);
        }
    }

    /**
     * Start the benchmark and go to the chosen one.
     *
     * @param args
     */
    public void start(String[] args) throws InterruptedException {
        logger.info("Starting up {}.", NeddyBenchmark.class.getSimpleName());

        // Process command line parameters
        if (args.length != 1) {
            System.out.print(getHelp(args));
            logger.error("Program executed with incorrect number of parameters. Shutting down.");
            System.exit(-1);
        }

        ParameterEnum parameter = null;
        try {
            parameter = ParameterEnum.valueOf(args[0].replace("-", "").toUpperCase());
        } catch (IllegalArgumentException iae) {
            System.out.print(getHelp(args));
            logger.error("Program called with invalid parameter. Shutting down.");
            System.exit(-1);
        }

        // Registers a shutdown hook to free resources of this class.
        Runtime.getRuntime().addShutdownHook(new ShutdownThread(this, "NettyBenchmark Shutdown Thread"));

        // Set up the event pipeline factory.
        getBootstrap().setPipelineFactory(new NeddyBenchmarkPipelineFactory());

        // Set some necessary or convenient socket options.
        // http://download.oracle.com/javase/6/docs/api/java/net/SocketOptions.html
        getBootstrap().setOption("tcpNoDelay", TCPNODELAY); // disable Nagle's algorithm
        getBootstrap().setOption("keepAlive", KEEPALIVE);  // keep alive connections
        getBootstrap().setOption("connectTimeoutMillis", TIMEOUT); // connection timeout

        // Switch to the requested benchmark.
        switch (parameter) {
            case STATIC:
                StaticContentBenchmark.execute();
                break;
            case WS:
                WebsocketBenchmark.execute();
                break;
            case REST:
                RestBenchmark.execute();
                break;
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
    public static ClientBootstrap getBootstrap() {
        return bootstrap;
    }

    /**
     * @param bootstrap the bootstrap to set
     */
    public static void setBootstrap(ClientBootstrap bootstrap) {
        NeddyBenchmark.bootstrap = bootstrap;
    }

    /**
     * @return the allChannels
     */
    public static ChannelGroup getAllChannels() {
        return allChannels;
    }

    /**
     * @param allChannels the allChannels to set
     */
    public static void setAllChannels(ChannelGroup allChannels) {
        NeddyBenchmark.allChannels = allChannels;
    }

    /**
     * Application help.
     *
     * @param args
     * @return
     */
    private static String getHelp(String[] args) {
        String help = "NeddyBenchmark - Parameters\n\n"
                + "Only one of the following parameters can be used at a time:\n"
                + "\tstatic\tExecutes the static benchmark.\n"
                + "\tws\tExecutes the predefined Websocket benchmark.\n"
                + "\trest\tExecutes the REST interface benchmark.\n"
                + "\n"
                + "The rest of the parameters, specific for every benchmark are configured in the properties file.\n";
        return help;
    }
}
