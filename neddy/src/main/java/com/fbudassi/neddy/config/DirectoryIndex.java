package com.fbudassi.neddy.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to get the default index file names in a web directory.
 *
 * @author fbudassi
 */
public final class DirectoryIndex {

    private static final ExternalConfiguration EXTERNAL_CONFIGURATION;
    // Property file paths
    private static final String PROPERTIES_DEFAULTS = "directoryindex.properties";
    private static final String PROPERTIES_ETC = "/etc/neddy_directoryindex.properties";
    private static final String PROPERTIES_CURRENT_DIR = "./neddy_directoryindex.properties";
    // File names list.
    private static final List<String> fileNames = new ArrayList<String>();

    /**
     * Static constructor.
     */
    static {
        // Loads the external configuration file.
        EXTERNAL_CONFIGURATION = new ExternalConfiguration(PriorityResource.build(PROPERTIES_CURRENT_DIR, PROPERTIES_ETC, PROPERTIES_DEFAULTS));

        // Loads the list of the names from the configuration file.
        int i = 1;
        String value;
        while ((value = DirectoryIndex.EXTERNAL_CONFIGURATION.getValue(String.valueOf(i))) != null) {
            fileNames.add(value);
            i++;
        }

    }

    /**
     * Gets a list of default file names for a web directory. Useful if the
     * requested URI is not a file but a directory.
     *
     * @return
     */
    public static List<String> getFileNames() {
        return fileNames;
    }
}
