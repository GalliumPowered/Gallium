package org.galliumpowered;

import net.minecraft.server.Main;
import net.minecrell.terminalconsole.SimpleTerminalConsole;

public class GalliumConsole extends SimpleTerminalConsole {
    @Override
    protected boolean isRunning() {
        return Mod.getMinecraftServer().isRunning();
    }

    @Override
    protected void runCommand(String command) {
        Main.dedicatedServer.handleConsoleInput(command, Main.dedicatedServer.createCommandSourceStack());
    }

    @Override
    protected void shutdown() {
        Mod.getMinecraftServer().halt(false);
    }
}
