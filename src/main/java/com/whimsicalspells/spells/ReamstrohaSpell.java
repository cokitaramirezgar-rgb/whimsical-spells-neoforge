package com.whimsicalspells.spells;

import com.whimsicalspells.TempWaterManager;
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
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Reamstroha: manipulación de agua.
 * - Normal: conjura agua temporal donde miras (más niveles = más área y más duración).
 * - Agachado: retira el agua de la zona que miras.
 */
public class ReamstrohaSpell extends AbstractSpell {
    private final ResourceLocation spellId = WhimsicalSpells.id("reamstroha");

    private static final int CAST_TIME_TICKS = 40; // 2 s de conjuración
    private static final int MAX_LEVEL = 5;
    private static final double RANGE_BASE = 16.0;
    private static final double RANGE_PER_LEVEL = 4.0;
    private static final int DURATION_BASE_SECONDS = 20;
    private static final int DURATION_PER_LEVEL_SECONDS = 10;

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.ICE_RESOURCE)
            .setMaxLevel(MAX_LEVEL)
            .setCooldownSeconds(10)
            .build();

    public ReamstrohaSpell() {
        this.baseManaCost = 20;
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 0;
        this.castTime = CAST_TIME_TICKS;
    }

    @Override
    public ResourceLocation getSpellResource() { return spellId; }

    @Override
    public DefaultConfig getDefaultConfig() { return defaultConfig; }

    @Override
    public CastType getCastType() { return CastType.LONG; }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (level instanceof ServerLevel server) {
            boolean drain = entity.isShiftKeyDown();
            double range = RANGE_BASE + RANGE_PER_LEVEL * (spellLevel - 1);
            Vec3 start = entity.getEyePosition();
            Vec3 end = start.add(entity.getLookAngle().scale(range));

            BlockHitResult hit = server.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER,
                    drain ? ClipContext.Fluid.SOURCE_ONLY : ClipContext.Fluid.NONE, entity));

            if (hit.getType() == HitResult.Type.MISS) {
                tell(entity, "ui.whimsical_spells.reamstroha_no_target");
            } else if (drain) {
                drainWater(server, hit.getBlockPos(), (spellLevel - 1) / 2 + 1, entity);
            } else {
                placeWater(server, hit.getBlockPos().relative(hit.getDirection()), spellLevel, entity);
            }
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    private void placeWater(ServerLevel server, BlockPos center, int spellLevel, LivingEntity caster) {
        if (server.dimensionType().ultraWarm()) {
            server.sendParticles(ParticleTypes.CLOUD,
                    center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5, 15, 0.4, 0.3, 0.4, 0.05);
            server.playSound(null, center, SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.0f, 1.0f);
            tell(caster, "ui.whimsical_spells.reamstroha_evaporates");
            return;
        }

        int r = (spellLevel - 1) / 2; // niveles 1-2: 1 bloque, 3-4: 3x3, 5: radio 2
        int ticks = (DURATION_BASE_SECONDS + DURATION_PER_LEVEL_SECONDS * (spellLevel - 1)) * 20;
        int placed = 0;

        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                if (dx * dx + dz * dz > r * r + r) continue;
                BlockPos pos = center.offset(dx, 0, dz);
                BlockState state = server.getBlockState(pos);
                if (state.canBeReplaced() && state.getFluidState().isEmpty()) {
                    server.setBlock(pos, Blocks.WATER.defaultBlockState(), 3);
                    TempWaterManager.add(server, pos, ticks);
                    server.sendParticles(ParticleTypes.SPLASH,
                            pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5, 10, 0.3, 0.2, 0.3, 0.1);
                    placed++;
                }
            }
        }
        if (placed > 0) {
            server.playSound(null, center, SoundEvents.BUCKET_EMPTY, SoundSource.PLAYERS, 1.0f, 1.0f);
        } else {
            tell(caster, "ui.whimsical_spells.reamstroha_no_space");
        }
    }

    private void drainWater(ServerLevel server, BlockPos center, int radius, LivingEntity caster) {
        int removed = 0;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dy * dy + dz * dz > radius * radius + 1) continue;
                    BlockPos pos = center.offset(dx, dy, dz);
                    BlockState state = server.getBlockState(pos);
                    if (state.is(Blocks.WATER) && state.getFluidState().isSource()) {
                        server.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                        removed++;
                    }
                }
            }
        }
        if (removed > 0) {
            server.sendParticles(ParticleTypes.FALLING_WATER,
                    center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5, 25, 0.8, 0.5, 0.8, 0.1);
            server.playSound(null, center, SoundEvents.BUCKET_FILL, SoundSource.PLAYERS, 1.0f, 1.0f);
        } else {
            tell(caster, "ui.whimsical_spells.reamstroha_no_water");
        }
    }

    private static void tell(LivingEntity entity, String key) {
        if (entity instanceof ServerPlayer player) {
            player.displayClientMessage(Component.translatable(key), true);
        }
    }
}
