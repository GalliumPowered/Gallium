package org.galliumpowered;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
    private static final Logger log = LogManager.getLogger("Gallium/Startup");
    public static Mod mod;
    public static void main(String[] args) {
        mod = new Mod();
        log.info("Launching server");
        net.minecraft.server.Main.main(args);
    }
}