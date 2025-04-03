package org.galliumpowered.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.galliumpowered.world.entity.Player;

import java.util.Optional;

public class ConsoleCommandCaller implements CommandCaller {
    Logger logger = LogManager.getLogger("Gallium/Console");

    @Override
    public Optional<Player> getPlayer() {
        return Optional.empty();
    }

    @Override
    public void sendMessage(Component component) {
        // ._.
        logger.info("\n{}", ANSIComponentSerializer.ansi().serialize(component));
    }

    @Override
    public void sendMessage(String message) {
        logger.info(message);
    }
}