package org.galliumpowered.internal.plugin;

import com.google.inject.Inject;
import org.galliumpowered.Gallium;
import org.galliumpowered.annotation.PluginLifecycleListener;
import org.galliumpowered.internal.plugin.commands.GamemodeCommand;
import org.galliumpowered.internal.plugin.commands.PingCommand;
import org.galliumpowered.internal.plugin.commands.permissions.GroupmodCommand;
import org.galliumpowered.internal.plugin.commands.permissions.PlayermodCommand;
import org.galliumpowered.internal.plugin.commands.plugin.PluginInfoCommand;
import org.galliumpowered.internal.plugin.commands.plugin.PluginListCommand;
import org.galliumpowered.internal.plugin.listeners.PlayerJoinListener;
import org.galliumpowered.plugin.PluginContainer;
import org.galliumpowered.plugin.PluginLifecycleState;
import org.galliumpowered.internal.plugin.commands.GalliumCommand;
import org.galliumpowered.annotation.Plugin;
import org.apache.logging.log4j.Logger;

@Plugin(name = "Gallium",
        id = "gallium",
        description = "Gallium internal plugin",
        authors = { "GalliumPowered" },
        version = "1.0")
public class GalliumPlugin {
    @Inject
    private Logger log;

    @Inject
    private PluginContainer pluginContainer;

    @PluginLifecycleListener(PluginLifecycleState.ENABLED)
    public void onPluginEnable() {

        // Command registration
        Gallium.getCommandManager().registerCommand(new GalliumCommand(), pluginContainer);
        Gallium.getCommandManager().registerCommand(new PluginListCommand(), pluginContainer);
        Gallium.getCommandManager().registerCommand(new PingCommand(), pluginContainer);
        Gallium.getCommandManager().registerCommand(new PlayermodCommand(), pluginContainer);
        Gallium.getCommandManager().registerCommand(new GroupmodCommand(), pluginContainer);
        Gallium.getCommandManager().registerCommand(new GamemodeCommand(), pluginContainer);
        Gallium.getCommandManager().registerCommand(new PluginInfoCommand(), pluginContainer);
//        Gallium.getCommandManager().registerCommand(new TestCommand(), pluginContainer);

        // Listener registration
        Gallium.getEventManager().registerListener(new PlayerJoinListener());
    }
}
