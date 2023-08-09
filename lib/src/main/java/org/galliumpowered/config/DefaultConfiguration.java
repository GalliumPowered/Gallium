package org.galliumpowered.config;

import org.galliumpowered.exceptions.ConfigurationException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class DefaultConfiguration implements Configuration {
    private Path directory;
    private String name;
    private Properties properties;
    private Path path;

    public DefaultConfiguration(Path directory, String name) {
        this.directory = directory;
        this.name = name;
        this.path = Path.of(directory.toString(), name);

        this.properties = new Properties();

        File dir = new File(directory.toUri());
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            if (!path.toFile().exists()) {
                path.toFile().createNewFile();
            }

            FileReader reader = new FileReader(path.toFile());
            properties.load(reader);
        } catch (IOException e) {
            throw new ConfigurationException(e);
        }
    }
    @Override
    public Path getDirectory() {
        return directory;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setValue(String key, String value) {
        if (getValue(key) == null) {
            properties.setProperty(key, value);
            writeConfig();
        }
    }

    @Override
    public String getValue(String key) {
        return properties.getProperty(key);
    }

    protected void writeConfig() {
        try (OutputStream output = Files.newOutputStream(path)) {
            properties.store(output, "A Gallium configuration file");
        } catch (IOException e) {
            throw new ConfigurationException(e);
        }
    }
}
