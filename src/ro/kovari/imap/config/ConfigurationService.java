/*
 * Project: imap2local
 *
 * Copyright (c) Attila Kovari
 * All rights reserved.
 *
 * MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ro.kovari.imap.config;

import ro.kovari.imap.Main;
import ro.kovari.imap.exception.ConfigurationException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;


/** Handles loading and basic validation of the application configuration */
public class ConfigurationService {

    private static final Logger logger = Logger.getLogger(ConfigurationService.class.getName());
    private static final File baseDir;



    static {
        String location = Main.class.getProtectionDomain().getCodeSource()
                .getLocation().getPath();

        try { // handle directory names with spaces
            location = URLDecoder.decode(location, StandardCharsets.UTF_8.name());

        } catch (UnsupportedEncodingException e) {
            logger.log(Level.SEVERE, "Unsupported encoding", e);
            System.exit(1); // not much left to do
        }

        baseDir = location.endsWith(".jar") ?
                new File(location).getParentFile() : new File(location);
    }



    /**
     * Get the configuration properties from the configuration file
     * @return The configuration {@link Properties}
     */
    private static Properties getConfigurationProperties() throws ConfigurationException {
        Properties configProperties = new Properties();
        try (FileReader reader = new FileReader(new File(baseDir, "imap2local.properties"))) {
            configProperties.load(reader);

        } catch (IOException e) {
            throw new ConfigurationException("Error loading configuration", e);
        }
        return configProperties;
    }



    /**
     * Get the application configuration
     * @return the application configuration
     * @throws ConfigurationException in case of invalid configuration
     */
    public static Configuration getImapConfiguration() throws ConfigurationException {
        Properties properties = getConfigurationProperties();

        boolean partialFetchEnabled = Boolean.valueOf(
                properties.getProperty("partialFetchEnabled", "true")
        );

        int fetchSize;
        try {
            fetchSize = Integer.valueOf(properties.getProperty("fetchSize", "1000000"));

        } catch (NumberFormatException e) {
            throw new ConfigurationException("Invalid fetch size!", e);
        }

        // if property is not present, set 'sslTrustedHosts' to null; this will exclude it
        // from the imap session properties
        String sslTrustedHosts = properties.getProperty("sslTrustedHosts", null);
        return new Configuration(partialFetchEnabled, fetchSize, sslTrustedHosts);
    }
}
