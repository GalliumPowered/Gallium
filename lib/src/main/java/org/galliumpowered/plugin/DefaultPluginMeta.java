package org.galliumpowered.plugin;

import org.galliumpowered.plugin.metadata.PluginMetadata;

public class DefaultPluginMeta implements PluginMetadata {

   private String name;
   private String id;
   private String description;
   private String[] authors;
   private String version;
   private String mainClass;

    public DefaultPluginMeta(String name, String id, String description, String[] authors, String version, String mainClass) {
        this.name = name;
        this.id = id;
        this.description = description;
        this.authors = authors;
        this.version = version;
        this.mainClass = mainClass;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String[] getAuthors() {
        return authors;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String getMainClass() {
        return mainClass;
    }

}
