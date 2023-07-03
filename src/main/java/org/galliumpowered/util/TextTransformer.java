package org.galliumpowered.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;

import javax.annotation.Nullable;

public class TextTransformer {
    public static net.minecraft.network.chat.Component toMinecraft(@Nullable Component component) {
        if (component == null) {
            return TextComponent.EMPTY;
        }

        MutableComponent serialized = net.minecraft.network.chat.Component.Serializer.fromJson(GsonComponentSerializer.gson().serialize(component));
        if (serialized != null) {
            return serialized;
        }

        return TextComponent.EMPTY;
    }
}