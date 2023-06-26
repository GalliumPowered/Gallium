package net.zenoc.gallium;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.Main;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.zenoc.gallium.api.world.entity.Player;
import net.zenoc.gallium.api.world.entity.player.PlayerImpl;
import net.zenoc.gallium.bridge.NMSBridge;
import net.zenoc.gallium.commandsys.*;
import net.zenoc.gallium.exceptions.CommandException;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class BridgeImpl implements NMSBridge {
    @Override
    public void registerCommand(String alias, String permission) {
        Mod.getMinecraftServer().getCommands().getDispatcher().register(LiteralArgumentBuilder.<CommandSourceStack>literal(alias)
                .requires(commandSourceStack -> {
                    if (commandSourceStack.getDisplayName().getContents().equals("Server")) {
                        return true;
                    } else {
                        ServerPlayer serverPlayer;
                        try {
                            serverPlayer = commandSourceStack.getPlayerOrException();
                        } catch (CommandSyntaxException e) {
                            throw new RuntimeException(e);
                        }
                        return Gallium.getPermissionManager().playerHasPermission(
                                new PlayerImpl(serverPlayer),
                                permission
                        );
                    }
                })
                .executes(this::executeCommand)
                // TODO: Command suggestion for args
                .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("arguments", StringArgumentType.greedyString())
                        .executes(this::executeCommand)
                )
        );

    }

    private int executeCommand(CommandContext<CommandSourceStack> ctx) {
        // FIXME: This might not properly account for permissions. It should though
        String[] args = ctx.getInput().split(" ");
        if (args.length == 0) {
            return 0;
        }

        String alias = args[0].toLowerCase();
        alias = alias.startsWith("/") ? alias.substring(1) : alias;

        MCommand command = Gallium.getCommandManager().getCommands().get(alias);
        if (command == null) {
            return 0;
        }

        Method method = command.getMethod();

        try {
            CommandCaller caller;
            try {
                caller = new CommandCallerImpl(ctx.getSource().getPlayerOrException());
            } catch (CommandSyntaxException e) {
                caller = new CommandCallerImpl(null);
            }
            method.invoke(command.getCaller(), new CommandContextImpl(caller, args));
        } catch (Exception e) {
            throw new CommandException(e);
        }
        return 1;
    }

    @Override
    public Optional<Player> getPlayerByName(String s) {
        AtomicReference<Player> player = new AtomicReference<>(null);
        Mod.getMinecraftServer().getPlayerList().getPlayers().stream()
                .filter(serverPlayer -> serverPlayer.getName().getContents().equals(s))
                .findFirst().ifPresent(serverPlayer -> player.set(new PlayerImpl(serverPlayer)));

        return Optional.of(player.get());
    }

    @Override
    public String getServerVersion() {
        return Mod.getMinecraftServer().getServerVersion();
    }
}
