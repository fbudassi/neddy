package com.fbudassi.neddy.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads properties from a file.
 *
 * @author juan
 */
class PropertiesLoader {

    private final static Logger logger = LoggerFactory.getLogger(PropertiesLoader.class);

    /**
     * Loads a properties file from a file resource. This if the file resource
     * doesn't exist, this will try to find a class path resource.
     *
     * @param fileResource
     * @return
     */
    static Properties loadFromFileResource(String fileResource) {
        Properties ret = new Properties();

        try {
            File file = new File(fileResource);
            if (file.exists() && file.canRead()) {
                ret.load(new FileInputStream(file));
            } else if (!file.exists()) {
                ret = loadFromClasspathResource(ret, fileResource);
            }
        } catch (Exception ex) {
            logger.error("Error in loadFromFileResource", ex);
        } finally {
            return ret;
        }
    }

    /**
     * Load a a properties file from a classpath resource. This tries to load a
     * classpath resource that starts with / if not, it will add the /
     * character.
     *
     * @param props
     * @param resource
     * @return
     * @throws IOException
     */
    static Properties loadFromClasspathResource(Properties props, String resource) throws IOException {
        if (!resource.startsWith("/")) {
            resource = "/" + resource;
        }

        InputStream is = PropertiesLoader.class.getResourceAsStream(resource);

        if (is != null) {
            props.load(is);
        }
        return props;
    }
}
