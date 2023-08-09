package org.galliumpowered.command.console;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.galliumpowered.command.CommandCaller;
import org.galliumpowered.world.entity.Player;

import java.util.Optional;

public class ConsoleCommandCaller implements CommandCaller {
    private Logger logger = LogManager.getLogger("Gallium/Console command caller");

    @Override
    public Optional<Player> getPlayer() {
        return Optional.empty();
    }

    @Override
    public void sendMessage(Component component) {
        // TODO: colours
        this.sendMessage(((TextComponent) component).content());
    }

    @Override
    public void sendMessage(String message) {
        logger.info("[CMD OUT] {}", message);
    }
}
