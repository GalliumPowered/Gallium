package org.galliumpowered.annotation;

import org.galliumpowered.plugin.PluginLifecycleState;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A plugin lifecycle listener
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface PluginLifecycleListener {
    PluginLifecycleState value();
}
