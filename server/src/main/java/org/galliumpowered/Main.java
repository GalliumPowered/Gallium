package org.galliumpowered;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.galliumpowered.plugin.PluginManager;

public class Main {
    private static final Logger log = LogManager.getLogger("Gallium/Startup");
    public static Mod mod;
    public static void main(String[] args) {
        for (String arg : args) {
            if (arg.equals("--testplugin")) {
                PluginManager.loadTestPlugin = true;
            }
        }
        mod = new Mod();
        log.info("Launching server");
        net.minecraft.server.Main.main(args);
    }
}