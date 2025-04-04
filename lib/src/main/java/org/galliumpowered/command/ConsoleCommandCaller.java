package org.galliumpowered.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.galliumpowered.Gallium;
import org.galliumpowered.pagination.PaginationList;
import org.galliumpowered.pagination.PaginationUtils;
import org.galliumpowered.world.entity.Player;

import java.util.Optional;

public class ConsoleCommandCaller implements CommandCaller {
    private static final int MAX_WIDTH = 320;
    private final Logger logger = LogManager.getLogger("Gallium/Console");

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

    @Override
    public void sendPaginationList(PaginationList paginationList) {
        sendMessage(PaginationUtils.generateTitle(paginationList, MAX_WIDTH)
                .appendNewline()
                .append(Component.join(JoinConfiguration.newlines(),
                        paginationList.getContents().subList(0,
                                Math.min(paginationList.getContents().size(), getMaxChatLines()))))
                .appendNewline()
                .append(PaginationUtils.generateBottom(paginationList.getPadding(), MAX_WIDTH)));

        Gallium.getPaginationManager().submit(this, paginationList);
    }

    @Override
    public int getMaxChatLines() {
        return 30;
    }

    @Override
    public String getPaginationIdentifier() {
        return "console";
    }
}