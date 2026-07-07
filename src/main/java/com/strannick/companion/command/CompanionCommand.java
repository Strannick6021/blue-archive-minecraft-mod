package com.strannick.companion.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class CompanionCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("companion")
                .then(Commands.literal("follow")
                        .executes(ctx -> executeFollow(ctx.getSource())))
                .then(Commands.literal("defend")
                        .executes(ctx -> executeDefend(ctx.getSource())))
                .then(Commands.literal("mining")
                        .then(Commands.argument("block", StringArgumentType.word())
                                .executes(ctx -> executeMining(ctx.getSource(), StringArgumentType.getString(ctx, "block")))))
        );
    }

    private static int executeFollow(CommandSourceStack source) {
        if (source.getPlayer() == null) {
            source.sendFailure(Component.literal("Only players can use this command!"));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("Companion will now follow you!"), false);
        return 1;
    }

    private static int executeDefend(CommandSourceStack source) {
        if (source.getPlayer() == null) {
            source.sendFailure(Component.literal("Only players can use this command!"));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("Companion is now in defense mode!"), false);
        return 1;
    }

    private static int executeMining(CommandSourceStack source, String blockType) {
        if (source.getPlayer() == null) {
            source.sendFailure(Component.literal("Only players can use this command!"));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("Companion will now mine " + blockType + "!"), false);
        return 1;
    }
}