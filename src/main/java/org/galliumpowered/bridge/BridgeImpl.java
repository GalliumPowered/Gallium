package org.galliumpowered.bridge;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import org.galliumpowered.Gallium;
import org.galliumpowered.Mod;
import org.galliumpowered.api.annotations.Args;
import org.galliumpowered.commandsys.args.ArgsTypeTranslator;
import org.galliumpowered.world.entity.Player;
import org.galliumpowered.api.world.entity.player.PlayerImpl;
import org.galliumpowered.exceptions.CommandException;
import org.galliumpowered.commandsys.CommandCaller;
import org.galliumpowered.commandsys.CommandCallerImpl;
import org.galliumpowered.commandsys.CommandContextImpl;
import org.galliumpowered.commandsys.MCommand;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class BridgeImpl implements NMSBridge {
    @Override
    public void registerCommand(String alias, String permission) {
        LiteralArgumentBuilder node = createNodeBuilder(alias, permission);
                node.then(
                        RequiredArgumentBuilder.<CommandSourceStack, String>argument("arguments", StringArgumentType.greedyString())
                        .suggests(this::suggest)
                        .executes(this::executeCommand)
                );
        Mod.getMinecraftServer().getCommands().getDispatcher().register(node);
    }

    @Override
    public void registerCommand(String alias, String permission, Args[] args) {
        LiteralArgumentBuilder node = createNodeBuilder(alias, permission);

        for (Args arg : args) {
            node.then(RequiredArgumentBuilder.<CommandSourceStack, String>argument(arg.name(), ArgsTypeTranslator.getAsMinecraft(arg.type()))
                    .suggests(this::suggest)
                    .executes(this::executeCommand)
            );
        }
        Mod.getMinecraftServer().getCommands().getDispatcher().register(node);
    }

    private LiteralArgumentBuilder createNodeBuilder(String alias, String permission) {
        LiteralArgumentBuilder node = LiteralArgumentBuilder.<CommandSourceStack>literal(alias)
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
                .executes(this::executeCommand);

        return node;
    }

    private CompletableFuture<Suggestions> suggest(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder suggestionsBuilder) {
        SuggestionsBuilder builder = suggestionsBuilder.createOffset(suggestionsBuilder.getInput().indexOf(" ") + 1);
        return CompletableFuture.supplyAsync(() -> {
            String[] args = builder.getInput().split(" ");
            if (args.length == 0) {
                return builder.build();
            }

            String alias = args[0].toLowerCase();
            alias = alias.startsWith("/") ? alias.substring(1) : alias;

            MCommand command = Gallium.getCommandManager().getCommands().get(alias);
            if (command == null) {
                return builder.build();
            }

            CommandCaller caller;
            try {
                try {
                    caller = new CommandCallerImpl(ctx.getSource().getPlayerOrException());
                } catch (CommandSyntaxException e) {
                    caller = new CommandCallerImpl(null);
                }
            } catch (Exception e) {
                throw new CommandException(e);
            }

            String[] input = Arrays.copyOfRange(args, 1, args.length);
            for (String suggestion : command.suggest(new CommandContextImpl(caller, args, ctx))) {
                builder.suggest(suggestion);
            }

            return builder.build();
        });
    }

    private int executeCommand(CommandContext<CommandSourceStack> ctx) {
        // FIXME: This might not properly account for permissions. It should though
        String[] args = ctx.getInput().split(" ");
        if (args.length == 0) {
            return 0;
        }

        String alias = args[0].toLowerCase();
        alias = alias.startsWith("/") ? alias.substring(1) : alias;

        AtomicReference<MCommand> command = new AtomicReference<>(Gallium.getCommandManager().getCommands().get(alias));

        // Check against subcommands, but only if there could be a subcommand
        if (args.length > 1) {
            Gallium.getCommandManager().getSubcommands().forEach((parent, sub) -> {
                if (parent == command.get()) {
                    for (String subAlias : sub.getCommand().aliases()) {
                        if (subAlias.equals(args[1])) {
                            command.set(sub);
                        }
                    }
                }
            });
        }

        if (command.get() == null) {
             return 0;
        }

        Method method = command.get().getMethod();

        try {
            CommandCaller caller;
            try {
                caller = new CommandCallerImpl(ctx.getSource().getPlayerOrException());
            } catch (CommandSyntaxException e) {
                caller = new CommandCallerImpl(null);
            }
            method.invoke(command.get().getCaller(), new CommandContextImpl(caller, args, ctx));
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
