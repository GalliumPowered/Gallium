package org.galliumpowered.pagination;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.Map;

public class PaginationUtils {
    private static final Map<Character, Integer> CHAR_WIDTHS = Map.ofEntries(
            Map.entry('!', 2), Map.entry(',', 2), Map.entry('.', 2), Map.entry(':', 2),
            Map.entry(';', 2), Map.entry('i', 2), Map.entry('|', 2),
            Map.entry('\'', 3), Map.entry('l', 3),
            Map.entry(' ', 4), Map.entry('I', 4), Map.entry('[', 4), Map.entry(']', 4),
            Map.entry('"', 5), Map.entry('(', 5), Map.entry(')', 5), Map.entry('<', 5),
            Map.entry('>', 5), Map.entry('`', 5), Map.entry('f', 5), Map.entry('k', 5),
            Map.entry('{', 5), Map.entry('}', 5),
            Map.entry('@', 7), Map.entry('~', 7),
            Map.entry('«', 10), Map.entry('»', 10) // I swear this isn't right.
    );

    private static final Component ARROWS = Component.text("«").clickEvent(ClickEvent.runCommand("/page prev"))
                    .appendSpace()
                    .append(Component.text("»").clickEvent(ClickEvent.runCommand("/page next")));

    private static int getCharWidth(char c) {
        return CHAR_WIDTHS.getOrDefault(c, 6);
    }

    private static int getStringWidth(String text) {
        return text.chars().map(c -> getCharWidth((char) c)).sum();
    }

    /**
     * Generate the final title of a pagination list
     *
     * @param list The list to generate the title for
     * @param maxWidth Max width (pixels) of the title
     * @return Final header with the padding
     */
    public static Component generateTitle(PaginationList list, int maxWidth) {
        return center(list.getTitle(), list.getPadding(), maxWidth);
    }

    /**
     * Put a Component in the center, and surround it with select padding
     * @param component Component to center
     * @param padding Padding around the component
     * @param maxWidth The maximum width, in pixels
     * @return Centered component
     */
    public static Component center(Component component, Component padding, int maxWidth) {
        String content = PlainTextComponentSerializer.plainText().serialize(component);
        String paddingContent = PlainTextComponentSerializer.plainText().serialize(padding);
        if (getStringWidth(content) >= maxWidth) {
            // No padding needed
            return component;
        }

        // Centre the title text and surround it with padding
        // -2 for the spaces
        int paddingWidth = (maxWidth - getStringWidth(content)) / 2;
        int paddingRepeat = paddingWidth / getStringWidth(paddingContent);

        TextComponent.Builder builder = Component.text();

        builder.append(repeat(padding, paddingRepeat));
        builder.appendSpace();
        builder.append(component);
        builder.appendSpace();
        builder.append(repeat(padding, paddingRepeat));

        return builder.build();
    }

    /**
     * Generate the bottom of a pagination list
     *
     * @param padding The padding of the bottom
     * @param maxWidth The max width (characters) of the bottom
     * @return Component for the bottom with the padding
     */
    public static Component generateBottom(Component padding, int maxWidth) {
        return center(ARROWS, padding, maxWidth);
    }

    private static Component repeat(Component component, int repetitions) {
        TextComponent.Builder builder = Component.text();
        for (int i = 0; i < repetitions; i++) {
            builder.append(component);
        }
        return builder.build();
    }
}
