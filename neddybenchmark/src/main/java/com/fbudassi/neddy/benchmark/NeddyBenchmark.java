package com.fbudassi.neddy.benchmark;

import com.fbudassi.neddy.benchmark.benchmarks.Benchmark;
import com.fbudassi.neddy.benchmark.benchmarks.BenchmarkFactory;
import java.util.concurrent.Executors;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NeddyBenchmark implements Shutdownable {

    private static final Logger logger = LoggerFactory.getLogger(NeddyBenchmark.class);
    private static final String VERSION = "1.0.0";

    //Allowed command line parameters
    public enum ParameterEnum {

        STATIC, WS, REST
    }
    // Resources to be freed when shutdown happens.
    private static ClientBootstrap bootstrap;
    private static ChannelGroup allChannels;

    /**
     * Static constructor.
     */
    static {
        // Configure the client bootstrap.
        bootstrap = new ClientBootstrap(
                new NioClientSocketChannelFactory(
                Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool()));

        // ChannelGroup of all open channels.
        allChannels = new DefaultChannelGroup("neddybenchmark");
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
        } catch (Exception ex) {
            logger.error("Error in NeddyBenchmark.", ex);
        }
    }

    /**
     * Start the benchmark and go to the chosen one.
     *
     * @param args
     */
    public void start(String[] args) throws InterruptedException, Exception {
        // Process command line parameters
        if (args.length != 1) {
            System.out.print(getHelp(args));
            System.exit(-1);
        }

        ParameterEnum benchmarkEnum = null;
        try {
            benchmarkEnum = ParameterEnum.valueOf(args[0].replace("-", "").toUpperCase());
        } catch (IllegalArgumentException iae) {
            System.out.print(getHelp(args));
            logger.error("Program called with invalid parameter. Shutting down.");
            System.exit(-1);
        }

        logger.info("Starting up {}.", NeddyBenchmark.class.getSimpleName());

        // Registers a shutdown hook to free resources of this class.
        Runtime.getRuntime().addShutdownHook(new ShutdownThread(this, "NettyBenchmark Shutdown Thread"));

        // Gets the proper benchmark class.
        Benchmark benchmark = BenchmarkFactory.getBenchmark(benchmarkEnum);

        // Setup the proper event pipeline factory for the benchmark.
        ChannelPipelineFactory pipelineFactory = benchmark.getPipeline();
        getBootstrap().setPipelineFactory(pipelineFactory);

        // Set some necessary or convenient socket options in the bootstrap.
        benchmark.configureBootstrap(getBootstrap());

        // Execute the requested benchmark.
        benchmark.execute();
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
     * @return the allChannels
     */
    public static ChannelGroup getAllChannels() {
        return allChannels;
    }

    /**
     * Application help.
     *
     * @param args
     * @return
     */
    private static String getHelp(String[] args) {
        String help = "NeddyBenchmark " + VERSION + "\n\n"
                + "Only one of the following parameters can be used at a time:\n"
                + "\tstatic\tExecutes the static benchmark.\n"
                + "\tws\tExecutes the predefined Websocket benchmark.\n"
                + "\trest\tExecutes the REST interface benchmark.\n"
                + "\n"
                + "The rest of the parameters, specific for every benchmark are configured in the properties file.\n";
        return help;
    }
}
