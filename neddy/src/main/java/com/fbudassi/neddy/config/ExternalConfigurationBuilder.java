package com.fbudassi.neddy.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * This class builds the external configuration object.
 *
 * @author juan
 */
class ExternalConfigurationBuilder {

    /**
     * Build a properties out of several locations.
     *
     * @param configs
     * @param locations
     */
    static void build(Properties configs, Comparable[] locations) {
        List<Comparable> listLocations = Arrays.asList(locations);
        //sort by priority.
        Collections.sort(listLocations);

        //reverse the list so we can load from low priority to high priority.
        Collections.reverse(listLocations);

        for (Comparable comparable : listLocations) {
            Properties props = PropertiesLoader.loadFromFileResource(comparable.toString());
            configs.putAll(props);
        }
    }
}
