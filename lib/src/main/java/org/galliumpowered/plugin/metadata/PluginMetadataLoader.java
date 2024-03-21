package org.galliumpowered.plugin.metadata;

import org.galliumpowered.annotation.Plugin;
import org.galliumpowered.exceptions.BadPluginException;
import org.galliumpowered.plugin.DefaultPluginMeta;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Optional;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class PluginMetadataLoader {
    public static Optional<PluginMetadata> getPluginMetadata(File file) throws IOException, ClassNotFoundException {
        ZipFile zip = new ZipFile(file);
        if (zip.getEntry("Canary.inf") != null) {
            throw new BadPluginException("CanaryMod plugins are not natively supported. Please remove " + file.getName());
        } else if (zip.getEntry("plugin.yml") != null) {
            throw new BadPluginException("Bukkit plugins are not natively supported. Please remove " + file.getName());
        } else if (zip.getEntry("bungee.yml") != null) {
            throw new BadPluginException("BungeeCord plugins are not natively supported. Please remove " + file.getName());
        } else if (zip.getEntry("mcmod.info") != null) {
            throw new BadPluginException("Sponge plugins and Forge mods are not natively supported. Please remove " + file.getName());
        } else {
            PluginMetadata meta = null;
            String mainClass;
            URLClassLoader child = new URLClassLoader(new URL[]{file.toURI().toURL()}, PluginMetadataLoader.class.getClassLoader());

            // Get metadata from JSON
            if (zip.getEntry("plugin.json") != null) {
                InputStreamReader configReader = new InputStreamReader(child.getResourceAsStream("plugin.json"));
                meta = getPluginMetadataFromJson(configReader);
            } else {
                // Get metadata from annotation
                ZipEntry manifest = zip.getEntry("META-INF/MANIFEST.MF");
                if (manifest == null) {
                    throw new BadPluginException("Could not find META-INF/MANIFEST.MF manifest file in " + file.getName());
                } else {
                    InputStream manifestInput = zip.getInputStream(manifest);
                    InputStreamReader manifestReader = new InputStreamReader(manifestInput);
                    BufferedReader br = new BufferedReader(manifestReader);

                    Properties prop = new Properties();
                    prop.load(br);

                    mainClass = prop.getProperty("Main-Class");
                    Class<?> clazz = Class.forName(mainClass, false, child);
                    meta = getPluginMetaFromAnnotation(clazz);
                }
            }
            return Optional.of(meta);
        }
    }

    public static PluginMetadata getPluginMetaFromAnnotation(Class<?> javaPluginClass) {
        Plugin plugin = javaPluginClass.getAnnotation(Plugin.class);
        return new DefaultPluginMeta(plugin.name(), plugin.id().toLowerCase(), plugin.description(), plugin.authors(), plugin.version(), javaPluginClass.getName());
    }

    public static PluginMetadata getPluginMetadataFromJson(InputStreamReader configReader) {
        BufferedReader br = new BufferedReader(configReader);

        JSONTokener tokener = new JSONTokener(br);
        JSONObject json = new JSONObject(tokener);

        String name = json.getString("name");
        String id = json.getString("id").toLowerCase();
        String description = json.getString("description");
        String version = json.getString("version");
        String mainClass = json.getString("mainClass");

        JSONArray authorsJSON = json.getJSONArray("authors");
        String[] authors = new String[authorsJSON.length()];
        for (int i = 0; i < authorsJSON.length(); i++) {
            authors[i] = (String) authorsJSON.get(i);
        }:

        return new DefaultPluginMeta(name, id, description, authors, version, mainClass);
    }
}
