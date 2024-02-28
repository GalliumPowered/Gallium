package org.galliumpowered.testplugin;

import com.google.inject.Inject;
import org.apache.logging.log4j.Logger;
import org.galliumpowered.Gallium;
import org.galliumpowered.annotation.PluginLifecycleListener;
import org.galliumpowered.plugin.PluginContainer;
import org.galliumpowered.plugin.PluginLifecycleState;
import org.galliumpowered.testplugin.commands.TestCommands;
import org.galliumpowered.testplugin.listeners.BlockBreakListener;
import org.galliumpowered.testplugin.listeners.BlockPlaceListener;
import org.galliumpowered.testplugin.listeners.PlayerDeathListener;

public class TestPlugin {
    @Inject
    private PluginContainer pluginContainer;

    @Inject
    private Logger logger;

    @PluginLifecycleListener(PluginLifecycleState.ENABLED)
    public void onPluginEnable() {
        Gallium.getCommandManager().registerCommand(new TestCommands(), pluginContainer);
        Gallium.getEventManager().registerEvent(new BlockBreakListener(logger));
        Gallium.getEventManager().registerEvent(new BlockPlaceListener(logger));
        Gallium.getEventManager().registerEvent(new PlayerDeathListener());
    }
}
