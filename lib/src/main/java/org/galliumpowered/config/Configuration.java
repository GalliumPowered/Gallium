package org.galliumpowered.config;

import java.nio.file.Path;

/**
 * A configuration file.
 */
public interface Configuration {
    /**
     * The directory that the configuration file is in.
     * @return Configuration directory
     */
    Path getDirectory();

    /**
     * Name of the configuration file
     * @return Config file name
     */
    String getName();

    /**
     * Sets key to a value
     * @param key Key
     * @param value Value
     */
    void setValue(String key, String value);

    /**
     * Gets the value of a key
     * @param key Key
     * @return Key value
     */
    String getValue(String key);
}
