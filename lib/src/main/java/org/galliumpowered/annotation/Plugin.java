package org.galliumpowered.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Plugin {

    /**
     * The name of the plugin
     * @return the name
     */
    String name();

    /**
     * The ID of the plugin
     * @return the ID
     */
    String id();

    /**
     * The plugin's description
     * @return the description
     */
    String description();

    /**
     * The plugin authors
     * @return the authors
     */
    String[] authors();

    /**
     * The plugin's version
     * @return the version
     */
    String version();

}
