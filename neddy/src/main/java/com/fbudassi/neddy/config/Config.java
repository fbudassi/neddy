package com.fbudassi.neddy.config;

/**
 * Utility class to centralise the access to the configuration files. It also
 * has some constants for all the configuration keys used and some wrapper
 * methods.
 *
 * @author fbudassi
 */
public final class Config {

    private static final ExternalConfiguration EXTERNAL_CONFIGURATION;
    //Property file paths
    private static final String PROPERTIES_DEFAULTS = "defaults.properties";
    private static final String PROPERTIES_ETC = "/etc/neddy.properties";
    private static final String PROPERTIES_CURRENT_DIR = "./neddy.properties";
    //Key constants
    public static final String KEY_PORT_LISTENERS = "com.fbudassi.neddy.port.listeners";
    public static final String KEY_PORT_SPEAKERS = "com.fbudassi.neddy.port.speakers";
    public static final String KEY_WWWROOT = "com.fbudassi.neddy.wwwroot";
    public static final String KEY_TIMEOUT = "com.fbudassi.neddy.timeout";
    public static final String KEY_KEEPALIVE_TIMEOUT = "com.fbudassi.neddy.keepalivetimeout";
    public static final String KEY_SERVERNAME = "com.fbudassi.neddy.servername";
    public static final String KEY_TCPNODELAY = "com.fbudassi.neddy.tcpnodelay";
    public static final String KEY_KEEPALIVE = "com.fbudassi.neddy.keepalive";
    public static final String KEY_RESOURCE_LISTENER = "com.fbudassi.neddy.resource.listener";
    public static final String KEY_RESOURCE_CATEGORY = "com.fbudassi.neddy.resource.category";
    public static final String KEY_RESOURCE_LISTENERS = "com.fbudassi.neddy.resource.listeners";

    /**
     * Static constructor.
     */
    static {
        EXTERNAL_CONFIGURATION = new ExternalConfiguration(PriorityResource.build(PROPERTIES_CURRENT_DIR, PROPERTIES_ETC, PROPERTIES_DEFAULTS));
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
