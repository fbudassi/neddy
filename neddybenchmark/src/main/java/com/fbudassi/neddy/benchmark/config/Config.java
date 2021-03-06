package com.fbudassi.neddy.benchmark.config;

/**
 * Utility class to centralise the access to the configuration files. It also
 * has some constants for all the configuration keys used and some wrapper
 * methods.
 *
 * @author federico
 */
public final class Config {

    private static final ExternalConfiguration EXTERNAL_CONFIGURATION;
    //Property file paths
    private static final String PROPERTIES_DEFAULTS = "defaults.properties";
    private static final String PROPERTIES_CURRENT_DIR = "./neddybenchmark.properties";
    //General key constants
    public static final String KEY_TIMEOUT = "com.fbudassi.neddy.benchmark.timeout";
    public static final String KEY_TCPNODELAY = "com.fbudassi.neddy.benchmark.tcpnodelay";
    public static final String KEY_KEEPALIVE = "com.fbudassi.neddy.benchmark.keepalive";
    public static final String KEY_USERAGENT = "com.fbudassi.neddy.benchmark.useragent";
    //Server key constants
    public static final String KEY_SERVER_PORT = "com.fbudassi.neddy.benchmark.serverport";
    public static final String KEY_REST_PORT = "com.fbudassi.neddy.benchmark.restport";
    public static final String KEY_SERVER_ADDRESS = "com.fbudassi.neddy.benchmark.serveraddress";
    //General benchmark constants
    public static final String KEY_NUMADDRESSES = "com.fbudassi.neddy.benchmark.numaddresses";
    public static final String KEY_NUMPORTS = "com.fbudassi.neddy.benchmark.numports";
    public static final String KEY_CLIENT_PORTSTART = "com.fbudassi.neddy.benchmark.clientportstart";
    public static final String KEY_CLIENT_BASEADDRESS = "com.fbudassi.neddy.benchmark.clientbaseaddress";
    public static final String KEY_DELAY = "com.fbudassi.neddy.benchmark.delay";
    //Static Content benchmark constants
    public static final String KEY_SERVER_PATH = "com.fbudassi.neddy.benchmark.serverpath";
    //Rest benchmark constants
    public static final String KEY_SPEAKER_NUMCATEGORIES = "com.fbudassi.neddy.benchmark.speaker.numcategories";
    public static final String KEY_SPEAKER_DELAYBETWEENMESSAGES = "com.fbudassi.neddy.benchmark.speaker.delaybetweenmessages";
    public static final String KEY_RESOURCE_CATEGORY = "com.fbudassi.neddy.benchmark.resource.category";
    public static final String KEY_RESOURCE_LISTENERS = "com.fbudassi.neddy.benchmark.resource.listeners";
    //Websocket benchmark constants
    public static final String KEY_WEBSOCKET_PING = "com.fbudassi.neddy.benchmark.websocket.ping";
    public static final String KEY_LISTENER_NUMCATEGORIES = "com.fbudassi.neddy.benchmark.listener.numcategories";
    public static final String KEY_RESOURCE_LISTENER = "com.fbudassi.neddy.benchmark.resource.listener";

    /**
     * Static constructor.
     */
    static {
        EXTERNAL_CONFIGURATION = new ExternalConfiguration(PriorityResource.build(PROPERTIES_CURRENT_DIR, PROPERTIES_DEFAULTS));
    }

    /**
     * It returns the unique instances of the ExternalConfiguration reader.
     *
     * @return
     */
    public static ExternalConfiguration getExternalConfiguration() {
        return Config.EXTERNAL_CONFIGURATION;
    }

    /**
     * ExternalConfiguration.getValue wrapper method handy to avoid long lines.
     *
     * @param key
     * @return
     */
    public static String getValue(String key) {
        return Config.EXTERNAL_CONFIGURATION.getValue(key);
    }

    /**
     * ExternalConfiguration.getIntValue wrapper method handy to avoid long
     * lines.
     *
     * @param key
     * @return
     */
    public static int getIntValue(String key) {
        return Config.EXTERNAL_CONFIGURATION.getIntValue(key);
    }

    /**
     * ExternalConfiguration.getIntValue wrapper method handy to avoid long
     * lines.
     *
     * @param key
     * @return
     */
    public static boolean getBooleanValue(String key) {
        return Config.EXTERNAL_CONFIGURATION.getBooleanValue(key);
    }
}
