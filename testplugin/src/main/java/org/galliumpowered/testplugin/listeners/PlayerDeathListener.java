package org.galliumpowered.testplugin.listeners;

import net.kyori.adventure.text.Component;
import org.galliumpowered.Gallium;
import org.galliumpowered.annotation.EventListener;
import org.galliumpowered.chat.Colors;
import org.galliumpowered.event.player.PlayerDeathEvent;

public class PlayerDeathListener {
    @EventListener
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!event.isCancelled()) {
            Gallium.getServer().sendMsgToAll(Component.text(Colors.LIGHT_GREEN + event.getPlayer().getName() + " has pasta'd away!"));
        }
    }
}
