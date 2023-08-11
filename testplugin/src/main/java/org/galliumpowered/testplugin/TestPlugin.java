package org.galliumpowered.testplugin;

import com.google.inject.Inject;
import org.galliumpowered.Gallium;
import org.galliumpowered.annotation.PluginLifecycleListener;
import org.galliumpowered.plugin.PluginContainer;
import org.galliumpowered.plugin.PluginLifecycleState;
import org.galliumpowered.testplugin.commands.TestCommands;

public class TestPlugin {
    @Inject
    private PluginContainer pluginContainer;

    @PluginLifecycleListener(PluginLifecycleState.ENABLED)
    public void onPluginEnable() {
        Gallium.getCommandManager().registerCommand(new TestCommands(), pluginContainer);
    }
}
