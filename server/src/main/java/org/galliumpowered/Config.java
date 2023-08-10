package org.galliumpowered;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Properties;

public class Config {
    private Properties prop;
    public Config() {
        File config = Gallium.getConfig();
        if (!config.exists()) {
            try (OutputStream output = Files.newOutputStream(config.toPath())) {
                prop = new Properties();
                prop.setProperty("database-url", "sqlite:gallium.db");
                prop.store(output, "Gallium config file");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Properties getProp() {
        try {
            FileReader reader = new FileReader(Gallium.getConfig().getAbsolutePath());
            prop = new Properties();
            prop.load(reader);
            return prop;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public String getDatabaseConnectionURL() {
        return "jdbc:" + getProp().getProperty("database-url");
    }
}
