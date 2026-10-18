package com.whimsicalspells.item;

import com.whimsicalspells.ModAttachments;
import com.whimsicalspells.ability.Ability;
import com.whimsicalspells.ability.AbilityData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/** Pergamino de habilidad: al usarlo (clic derecho) aprendes la habilidad pasiva y se consume. */
public class AbilityScrollItem extends Item {
    private final Ability ability;

    public AbilityScrollItem(Ability ability, Properties properties) {
        super(properties);
        this.ability = ability;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            AbilityData data = player.getData(ModAttachments.ABILITIES);
            if (data.isUnlocked(ability)) {
                player.displayClientMessage(Component.translatable("ui.whimsical_spells.ability_known", ability.displayName()), true);
                return InteractionResultHolder.fail(stack);
            }
            data.unlock(ability);
            stack.consume(1, player);
            player.sendSystemMessage(Component.translatable("ui.whimsical_spells.ability_unlocked", ability.displayName())
                    .withStyle(ChatFormatting.GOLD));
            if (level instanceof ServerLevel server) {
                server.playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0f, 1.0f);
                server.sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 1.2, player.getZ(),
                        40, 0.6, 0.8, 0.6, 0.5);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(getDescriptionId() + ".desc").withStyle(ChatFormatting.GRAY));
    }
}
