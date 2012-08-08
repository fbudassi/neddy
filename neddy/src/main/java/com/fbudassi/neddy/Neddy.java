package com.fbudassi.neddy;

import com.fbudassi.neddy.config.Config;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Neddy main class.
 *
 * @author fbudassi
 */
public class Neddy implements Shutdownable, NeddyMXBean {

    private static final Logger logger = LoggerFactory.getLogger(Neddy.class);
    // Resources to be freed when shutdown happens.
    private ServerBootstrap bootstrap;
    private Timer timer;
    private static ChannelGroup ALL_CHANNELS;
    // Configuration variables.
    private static final String SERVERNAME = Config.getValue(Config.KEY_SERVERNAME);
    private static final int PORT_LISTENERS = Config.getIntValue(Config.KEY_PORT_LISTENERS);
    private static final int PORT_SPEAKERS = Config.getIntValue(Config.KEY_PORT_SPEAKERS);
    private static final boolean KEEPALIVE = Config.getBooleanValue(Config.KEY_KEEPALIVE);
    private static final boolean TCPNODELAY = Config.getBooleanValue(Config.KEY_TCPNODELAY);
    private static final int TIMEOUT = Config.getIntValue(Config.KEY_TIMEOUT);

    /**
     * Static constructor.
     */
    static {
        // ChannelGroup of all open channels (server + clients).
        setAllChannels(new DefaultChannelGroup("all-opened-channels"));
    }

    /**
     * Constructor.
     */
    public Neddy() {
        registerMXBean();
    }

    /**
     * Server starting point.
     *
     * @param args
     */
    public static void main(String[] args) {
        new Neddy().start();
    }

    /**
     * Start the server and bind to ports.
     */
    public void start() {
        logger.info("Starting up {} on ports: {} (for listeners) and {} (for speakers)", new Object[]{SERVERNAME, PORT_LISTENERS, PORT_SPEAKERS});

        // Registers a shutdown hook to free resources of this class.
        Runtime.getRuntime().addShutdownHook(new ShutdownThread(this, "Netty Shutdown Thread"));

        // Create timer used to close expired keep-alive connections.
        this.setTimer(new HashedWheelTimer());

        // Configure the server.
        this.setBootstrap(new ServerBootstrap(
                new NioServerSocketChannelFactory(
                Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool())));

        // Set up the event pipeline factory.
        getBootstrap().setPipelineFactory(new NeddyPipelineFactory(getTimer()));

        // Set some necessary or convenient socket options.
        // http://download.oracle.com/javase/6/docs/api/java/net/SocketOptions.html
        getBootstrap().setOption("child.tcpNoDelay", TCPNODELAY); // disable Nagle's algorithm
        getBootstrap().setOption("child.keepAlive", KEEPALIVE);  // keep alive connections
        getBootstrap().setOption("child.connectTimeoutMillis", TIMEOUT); // connection timeout
        getBootstrap().setOption("child.reuseAddress", true);

        // Bind and start accepting incoming connections.
        Channel listenersChannel = getBootstrap().bind(new InetSocketAddress(PORT_LISTENERS));
        Neddy.getAllChannels().add(listenersChannel);
        Channel speakersChannel = getBootstrap().bind(new InetSocketAddress(PORT_SPEAKERS));
        Neddy.getAllChannels().add(speakersChannel);
    }

    /**
     * Registers an MXBean used to get some info from the server.
     */
    private void registerMXBean() {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        String mxBeanName = "com.fbudassi.neddy:type=Neddy";
        try {
            mbs.registerMBean(this, new ObjectName(mxBeanName));
        } catch (Exception e) {
            logger.error("Unable to register {} MXBean", this.getClass().getCanonicalName(), e);
        }
    }

    /**
     * Frees all the server resources.
     */
    @Override
    public void shutdown() {
        // Release timer resources.
        getTimer().stop();

        // Close all connections and server sockets.
        ChannelGroupFuture groupFuture = Neddy.getAllChannels().close();
        groupFuture.awaitUninterruptibly();

        // Shutdown the selector loop (boss and workers).
        getBootstrap().getFactory().releaseExternalResources();
    }

    /**
     * Gets the number of clients open connections currently opened.
     *
     * @return The number of open channels.
     */
    @Override
    public int getOpenClientConnectionsNumber() {
        // Subtract 1 because the server channel is added to the ChannelGroup.
        return getAllChannels().size() - 1;
    }

    /**
     * @return the bootstrap
     */
    public ServerBootstrap getBootstrap() {
        return bootstrap;
    }

    /**
     * @param bootstrap the bootstrap to set
     */
    public void setBootstrap(ServerBootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    /**
     * @return the timer
     */
    public Timer getTimer() {
        return timer;
    }

    /**
     * @param timer the timer to set
     */
    public void setTimer(Timer timer) {
        this.timer = timer;
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
