package com.whimsicalspells.ability;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.whimsicalspells.ModAttachments;
import com.whimsicalspells.WhimsicalSpells;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Arrays;

/**
 * /habilidades            -> muestra el progreso de tus habilidades pasivas
 * /habilidades dar <jugador> <habilidad>   (solo operadores: desbloquea una habilidad)
 * (también disponible como /abilities)
 */
@EventBusSubscriber(modid = WhimsicalSpells.MODID)
public class AbilityCommands {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("habilidades")
                .executes(ctx -> showProgress(ctx.getSource().getPlayerOrException(), ctx.getSource()))
                .then(Commands.literal("dar")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("jugador", EntityArgument.player())
                                .then(Commands.argument("habilidad", StringArgumentType.word())
                                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                                Arrays.stream(Ability.values()).map(Ability::id), builder))
                                        .executes(ctx -> give(ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "jugador"),
                                                StringArgumentType.getString(ctx, "habilidad"))))));

        LiteralCommandNode<CommandSourceStack> node = event.getDispatcher().register(root);
        event.getDispatcher().register(Commands.literal("abilities").redirect(node));
    }

    private static int showProgress(ServerPlayer player, CommandSourceStack source) {
        AbilityData data = player.getData(ModAttachments.ABILITIES);
        source.sendSuccess(() -> Component.translatable("command.whimsical_spells.header"), false);
        for (Ability ability : Ability.values()) {
            if (data.isUnlocked(ability)) {
                Component line = AbilityManager.progress(ability, data);
                source.sendSuccess(() -> Component.literal("  ").append(line), false);
            } else {
                source.sendSuccess(() -> Component.literal("  ").append(
                        Component.translatable("command.whimsical_spells.locked", ability.displayName())), false);
            }
        }
        return 1;
    }

    private static int give(CommandSourceStack source, ServerPlayer target, String id) {
        Ability ability = Ability.byId(id);
        if (ability == null) {
            source.sendFailure(Component.translatable("command.whimsical_spells.unknown", id));
            return 0;
        }
        target.getData(ModAttachments.ABILITIES).unlock(ability);
        source.sendSuccess(() -> Component.translatable("command.whimsical_spells.given",
                ability.displayName(), target.getDisplayName()), true);
        target.sendSystemMessage(Component.translatable("ui.whimsical_spells.ability_unlocked", ability.displayName()));
        return 1;
    }
}
