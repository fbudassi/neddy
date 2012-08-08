package com.fbudassi.neddy.config;

import java.io.Serializable;
import java.util.Properties;

/**
 * Represents configuration loaded from an external file. This class uses a list
 * of strings that should resolve to an external file or a package resource.
 *
 * @author juan
 * @author fbudassi
 */
public class ExternalConfiguration implements Serializable {

    private static final long serialVersionUID = 1L;
    private final Properties configs;

    /**
     * Build an external configuration out of a list of locations. The list of
     * locations will be sorted using Collections.sort method. All the paths
     * will be obtained using the toString method of each element of the list of
     * locations.
     *
     * @param locations
     */
    public ExternalConfiguration(Comparable... locations) {
        configs = new Properties();
        ExternalConfigurationBuilder.build(configs, locations);
    }

    /**
     * Convenience method to get a value for a key. This delegates to
     * properties.getProperty and so behaves the same way.
     *
     * @param key
     * @return
     */
    public String getValue(String key) {
        return configs.getProperty(key);
    }

    /**
     * Convenience method to get a value for a key. This delegates to
     * properties.getProperty and so behaves the same way.
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public String getValue(String key, String defaultValue) {
        return configs.getProperty(key, defaultValue);
    }

    /**
     * Convenience method to get a value for a key. This delegates to
     * properties.getProperty and so behaves the same way.
     *
     * @param key
     * @return
     */
    public int getIntValue(String key) {
        return Integer.parseInt(configs.getProperty(key));
    }

    /**
     * Convenience method to get a value for a key. This delegates to
     * properties.getProperty and so behaves the same way.
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public int getIntValue(String key, String defaultValue) {
        return Integer.parseInt(configs.getProperty(key, defaultValue));
    }

    /**
     * Convenience method to get a value for a key. This delegates to
     * properties.getProperty and so behaves the same way.
     *
     * @param key
     * @return
     */
    public boolean getBooleanValue(String key) {
        return Boolean.parseBoolean(configs.getProperty(key));
    }

    /**
     * Convenience method to get a value for a key. This delegates to
     * properties.getProperty and so behaves the same way.
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public boolean getBooleanValue(String key, String defaultValue) {
        return Boolean.parseBoolean(configs.getProperty(key, defaultValue));
    }

    /**
     * Get all the current configurations on a mutable properties object.
     *
     * @return
     */
    public Properties getConfigs() {
        return configs;
    }
}
