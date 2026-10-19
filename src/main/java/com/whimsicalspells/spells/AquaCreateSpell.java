package com.whimsicalspells.spells;

import com.whimsicalspells.TempBlockManager;
import com.whimsicalspells.util.Targeting;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/** Aqua Create: conjura agua temporal donde miras. Más nivel = más área y más duración. */
public class AquaCreateSpell extends WhimsicalSpell {
    private static final int MAX_LEVEL = 3;
    private static final double RANGE = 20.0;
    private static final int DURATION_BASE_SECONDS = 30;
    private static final int DURATION_PER_LEVEL_SECONDS = 15;

    public AquaCreateSpell() {
        // instantáneo, cooldown 4 s, maná 10 + 5/nivel
        super("aqua_create", SchoolRegistry.ICE_RESOURCE, SpellRarity.COMMON, MAX_LEVEL,
                4, 10, 5, 0, 1, 0);
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (level instanceof ServerLevel server) {
            int l = clampLevel(spellLevel);
            BlockHitResult hit = Targeting.blockInSight(entity, RANGE);
            if (hit.getType() == HitResult.Type.MISS) {
                Targeting.tell(entity, "ui.whimsical_spells.no_target");
            } else if (server.dimensionType().ultraWarm()) {
                BlockPos at = hit.getBlockPos();
                server.sendParticles(ParticleTypes.CLOUD, at.getX() + 0.5, at.getY() + 1.0, at.getZ() + 0.5, 15, 0.4, 0.3, 0.4, 0.05);
                server.playSound(null, at, SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.0f, 1.0f);
                Targeting.tell(entity, "ui.whimsical_spells.evaporates");
            } else {
                BlockPos center = hit.getBlockPos().relative(hit.getDirection());
                int r = l - 1; // nivel 1: 1 bloque, 2: 3x3, 3: radio 2
                int ticks = (DURATION_BASE_SECONDS + DURATION_PER_LEVEL_SECONDS * (l - 1)) * 20;
                int placed = 0;
                for (int dx = -r; dx <= r; dx++) {
                    for (int dz = -r; dz <= r; dz++) {
                        if (dx * dx + dz * dz > r * r + r) continue;
                        BlockPos pos = center.offset(dx, 0, dz);
                        BlockState state = server.getBlockState(pos);
                        if (state.canBeReplaced() && state.getFluidState().isEmpty()) {
                            server.setBlock(pos, Blocks.WATER.defaultBlockState(), 3);
                            TempBlockManager.add(server, pos, Blocks.WATER, ticks, null);
                            server.sendParticles(ParticleTypes.SPLASH, pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5,
                                    10, 0.3, 0.2, 0.3, 0.1);
                            placed++;
                        }
                    }
                }
                if (placed > 0) {
                    server.playSound(null, center, SoundEvents.BUCKET_EMPTY, SoundSource.PLAYERS, 1.0f, 1.0f);
                } else {
                    Targeting.tell(entity, "ui.whimsical_spells.no_space");
                }
            }
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }
}
