package org.galliumpowered.command.console;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.galliumpowered.command.CommandCaller;
import org.galliumpowered.world.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ConsoleCommandCaller implements CommandCaller {
    private Logger logger = LogManager.getLogger("Gallium/Console");

    private static final Map<Character, String> COLOR_CODE_MAP = new HashMap<>();

    // Minecraft colour codes and their respective colours for log4j to understand
    static {
        COLOR_CODE_MAP.put('0', "\u001B[30m"); // Black
        COLOR_CODE_MAP.put('1', "\u001B[34m"); // Dark Blue
        COLOR_CODE_MAP.put('2', "\u001B[32m"); // Dark Green
        COLOR_CODE_MAP.put('3', "\u001B[36m"); // Dark Aqua
        COLOR_CODE_MAP.put('4', "\u001B[31m"); // Dark Red
        COLOR_CODE_MAP.put('5', "\u001B[35m"); // Dark Purple
        COLOR_CODE_MAP.put('6', "\u001B[33m"); // Gold
        COLOR_CODE_MAP.put('7', "\u001B[37m"); // Gray
        COLOR_CODE_MAP.put('8', "\u001B[90m"); // Dark Gray
        COLOR_CODE_MAP.put('9', "\u001B[94m"); // Blue
        COLOR_CODE_MAP.put('a', "\u001B[92m"); // Green
        COLOR_CODE_MAP.put('b', "\u001B[96m"); // Aqua
        COLOR_CODE_MAP.put('c', "\u001B[91m"); // Red
        COLOR_CODE_MAP.put('d', "\u001B[95m"); // Light Purple
        COLOR_CODE_MAP.put('e', "\u001B[93m"); // Yellow
        COLOR_CODE_MAP.put('f', "\u001B[97m"); // White
        COLOR_CODE_MAP.put('r', "\u001B[0m");  // Reset
    }

    // my inner Brit cannot type colors for much longer
    // ChatGPT wrote this btw
    private static String minecraftToAnsiColors(String message) {
        if (!message.contains("§") || message.isEmpty()) {
            return message;
        }

        StringBuilder translatedMessage = new StringBuilder();

        for (int i = 0; i < message.length(); i++) {
            char currentChar = message.charAt(i);

            if (currentChar == '§' && i + 1 < message.length()) {
                char nextChar = message.charAt(i + 1);
                String ansiColor = COLOR_CODE_MAP.get(nextChar);

                if (ansiColor != null) {
                    translatedMessage.append(ansiColor);
                    i++;
                } else {
                    translatedMessage.append(currentChar);
                }
            } else {
                translatedMessage.append(currentChar);
            }
        }

        translatedMessage.append("\u001B[0m");

        return translatedMessage.toString();
    }

    @Override
    public Optional<Player> getPlayer() {
        return Optional.empty();
    }

    @Override
    public void sendMessage(Component component) {
        this.sendMessage(((TextComponent) component).content());
    }

    @Override
    public void sendMessage(String message) {
        logger.info(minecraftToAnsiColors(message));
    }
}
