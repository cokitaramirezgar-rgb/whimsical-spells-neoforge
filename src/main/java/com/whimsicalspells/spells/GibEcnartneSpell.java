package com.whimsicalspells.spells;

import com.whimsicalspells.WhimsicalSpells;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;

/** Gib ecnartne ("¡dame entrada!"): abre puertas, trampillas y portones cercanos, incluidos los de hierro. */
public class GibEcnartneSpell extends AbstractSpell {
    private final ResourceLocation spellId = WhimsicalSpells.id("gib_ecnartne");

    private static final int RADIUS = 10;

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(SchoolRegistry.ENDER_RESOURCE)
            .setMaxLevel(1) // un solo nivel: más nivel solo ampliaba el radio
            .setCooldownSeconds(10)
            .build();

    public GibEcnartneSpell() {
        this.baseManaCost = 15;
        this.manaCostPerLevel = 0;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 0;
        this.castTime = 0;
    }

    @Override
    public ResourceLocation getSpellResource() { return spellId; }

    @Override
    public DefaultConfig getDefaultConfig() { return defaultConfig; }

    @Override
    public CastType getCastType() { return CastType.INSTANT; }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (level instanceof ServerLevel server) {
            BlockPos origin = entity.blockPosition();
            int opened = 0;

            for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-RADIUS, -RADIUS, -RADIUS),
                    origin.offset(RADIUS, RADIUS, RADIUS))) {
                BlockState state = server.getBlockState(pos);
                if (!isClosedAccess(state)) continue;
                if (!allowed(server, pos, state, entity)) continue;

                if (open(server, pos.immutable(), state, entity)) {
                    opened++;
                    server.sendParticles(ParticleTypes.END_ROD,
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 4, 0.3, 0.4, 0.3, 0.02);
                }
            }

            if (entity instanceof ServerPlayer player) {
                player.displayClientMessage(Component.translatable(
                        opened > 0 ? "ui.whimsical_spells.gib_opened" : "ui.whimsical_spells.gib_none", opened), true);
            }
            if (opened > 0) {
                server.playSound(null, origin, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0f, 1.2f);
            }
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    private static boolean isClosedAccess(BlockState state) {
        if (state.getBlock() instanceof DoorBlock) {
            return !state.getValue(DoorBlock.OPEN) && state.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER;
        }
        if (state.getBlock() instanceof TrapDoorBlock) return !state.getValue(TrapDoorBlock.OPEN);
        if (state.getBlock() instanceof FenceGateBlock) return !state.getValue(FenceGateBlock.OPEN);
        return false;
    }

    /** Respeta mods de protección de zonas (se comprueba como una rotura de bloque del jugador). */
    private static boolean allowed(ServerLevel level, BlockPos pos, BlockState state, LivingEntity caster) {
        if (caster instanceof ServerPlayer player) {
            BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(level, pos, state, player);
            NeoForge.EVENT_BUS.post(event);
            return !event.isCanceled();
        }
        return true;
    }

    private static boolean open(ServerLevel level, BlockPos pos, BlockState state, LivingEntity caster) {
        if (state.getBlock() instanceof DoorBlock door) {
            door.setOpen(caster, level, state, pos, true);
            return true;
        }
        if (state.getBlock() instanceof TrapDoorBlock) {
            level.setBlock(pos, state.setValue(TrapDoorBlock.OPEN, true), 2);
            level.playSound(null, pos, SoundEvents.WOODEN_TRAPDOOR_OPEN, SoundSource.BLOCKS, 1.0f, 1.0f);
            return true;
        }
        if (state.getBlock() instanceof FenceGateBlock) {
            level.setBlock(pos, state.setValue(FenceGateBlock.OPEN, true), 2);
            level.playSound(null, pos, SoundEvents.FENCE_GATE_OPEN, SoundSource.BLOCKS, 1.0f, 1.0f);
            return true;
        }
        return false;
    }
}
