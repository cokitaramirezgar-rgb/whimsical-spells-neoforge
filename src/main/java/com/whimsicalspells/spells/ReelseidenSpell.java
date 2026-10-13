package com.whimsicalspells.spells;

import com.whimsicalspells.WhimsicalSpells;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Reelseiden: el hechizo que corta casi cualquier cosa.
 * Un tajo en línea recta que abre una rendija vertical en los bloques (hasta la dureza de la piedra)
 * y mata de un golpe a cualquier mob o jugador que alcance. Se detiene ante un bloque más duro que la piedra.
 */
public class ReelseidenSpell extends AbstractSpell {
    private final ResourceLocation spellId = WhimsicalSpells.id("reelseiden");

    // --- Ajustes (fáciles de cambiar) ---
    private static final int CAST_TIME_TICKS = 50;  // 2.5 s de conjuración
    private static final int MAX_LEVEL = 3;
    private static final double[] RANGE = {10.0, 14.0, 18.0};
    /** Dureza máxima que puede cortar: piedra = 1.5, adoquín/madera = 2.0. Deepslate (3.0), menas y obsidiana no. */
    private static final float HARDNESS_LIMIT = 2.0f;
    /** La rendija mide (2 * HALF_HEIGHT + 1) bloques de alto. */
    private static final int HALF_HEIGHT = 2;
    /** Daño del tajo: suficiente para matar de un golpe. Los jugadores en creativo no se ven afectados. */
    private static final float KILL_DAMAGE = 1000.0f;

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.LEGENDARY)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(MAX_LEVEL)
            .setCooldownSeconds(90)
            .build();

    public ReelseidenSpell() {
        this.baseManaCost = 80;
        this.manaCostPerLevel = 20;
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
            int l = Math.max(1, Math.min(spellLevel, MAX_LEVEL));
            double range = RANGE[l - 1];

            Vec3 start = entity.getEyePosition();
            Vec3 look = entity.getLookAngle();

            // 1) Recorre el tajo y decide qué bloques se cortan; se detiene ante algo demasiado duro
            Set<BlockPos> cut = new LinkedHashSet<>();
            double reach = range;
            for (double d = 0.5; d <= range; d += 0.5) {
                Vec3 p = start.add(look.scale(d));
                BlockPos centerPos = BlockPos.containing(p);
                BlockState centerState = server.getBlockState(centerPos);
                if (isSolidAndTooHard(server, centerPos, centerState)) {
                    reach = d;
                    break;
                }
                for (int dy = -HALF_HEIGHT; dy <= HALF_HEIGHT; dy++) {
                    BlockPos pos = centerPos.above(dy);
                    if (isCuttable(server, pos, server.getBlockState(pos))) {
                        cut.add(pos);
                    }
                }
            }
            Vec3 end = start.add(look.scale(reach));

            // 2) Mata a lo que cruce el tajo
            AABB area = new AABB(start, end).inflate(1.5);
            for (LivingEntity target : server.getEntitiesOfClass(LivingEntity.class, area,
                    e -> e != entity && e.isAlive() && !entity.isAlliedTo(e))) {
                if (target.getBoundingBox().inflate(0.6).clip(start, end).isPresent()) {
                    target.hurt(server.damageSources().indirectMagic(entity, entity), KILL_DAMAGE);
                    server.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                            target.getX(), target.getY(0.5), target.getZ(), 6, 0.3, 0.4, 0.3, 0.02);
                }
            }

            // 3) Corta los bloques, con polvo de humo de fogata
            for (BlockPos pos : cut) {
                if (entity instanceof ServerPlayer player) {
                    BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(server, pos, server.getBlockState(pos), player);
                    NeoForge.EVENT_BUS.post(event);
                    if (event.isCanceled()) continue; // respeta protecciones de otros mods
                }
                server.destroyBlock(pos, true, entity);
                server.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 2, 0.3, 0.3, 0.3, 0.01);
            }

            // 4) Estela del tajo
            double length = start.distanceTo(end);
            for (double d = 1.0; d <= length; d += 1.0) {
                Vec3 p = start.add(look.scale(d));
                server.sendParticles(ParticleTypes.SWEEP_ATTACK, p.x, p.y, p.z, 1, 0, 0, 0, 0);
                server.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, p.x, p.y, p.z, 2, 0.2, 0.4, 0.2, 0.01);
            }
            server.playSound(null, entity.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP,
                    SoundSource.PLAYERS, 1.2f, 0.6f);
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    private static boolean isCuttable(ServerLevel level, BlockPos pos, BlockState state) {
        if (state.isAir() || !state.getFluidState().isEmpty()) return false;
        float hardness = state.getDestroySpeed(level, pos);
        return hardness >= 0 && hardness <= HARDNESS_LIMIT;
    }

    private static boolean isSolidAndTooHard(ServerLevel level, BlockPos pos, BlockState state) {
        if (state.isAir() || !state.getFluidState().isEmpty()) return false;
        float hardness = state.getDestroySpeed(level, pos);
        return hardness < 0 || hardness > HARDNESS_LIMIT;
    }
}
