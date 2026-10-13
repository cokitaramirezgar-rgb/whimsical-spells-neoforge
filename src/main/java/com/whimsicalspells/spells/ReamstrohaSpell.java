package com.whimsicalspells.spells;

import com.whimsicalspells.WaterSphereManager;
import com.whimsicalspells.WhimsicalSpells;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

/**
 * Reamstroha: manipulación de agua. El agua circundante se reúne en una enorme esfera sobre el objetivo
 * y cae en cascada, empapándolo (daño + lentitud + apaga el fuego).
 * - Más potente si llueve y si hay agua cerca del lanzador.
 * - Agachado: la cascada cae sobre ti y tus aliados (apaga el fuego y da resistencia).
 */
public class ReamstrohaSpell extends AbstractSpell {
    private final ResourceLocation spellId = WhimsicalSpells.id("reamstroha");

    private static final int CAST_TIME_TICKS = 40; // 2 s de conjuración
    private static final int MAX_LEVEL = 5;
    private static final double RANGE_BASE = 20.0;
    private static final double RANGE_PER_LEVEL = 2.0;
    private static final double RADIUS_BASE = 2.0;
    private static final double RADIUS_PER_LEVEL = 0.3;
    private static final double RAIN_BONUS = 0.5;       // +50% de daño si llueve
    private static final double MAX_WATER_BONUS = 0.5;  // hasta +50% con agua cerca
    private static final int WATER_SCAN_RADIUS = 5;
    private static final double WATER_BLOCKS_FOR_MAX = 150.0;

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.ICE_RESOURCE)
            .setMaxLevel(MAX_LEVEL)
            .setCooldownSeconds(12)
            .build();

    public ReamstrohaSpell() {
        this.baseManaCost = 35;
        this.manaCostPerLevel = 8;
        this.baseSpellPower = 5;
        this.spellPowerPerLevel = 1;
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
            boolean defensive = entity.isShiftKeyDown();

            UUID targetId;
            Vec3 position;
            if (defensive) {
                targetId = entity.getUUID();
                position = entity.position();
            } else {
                double range = RANGE_BASE + RANGE_PER_LEVEL * (spellLevel - 1);
                Vec3 start = entity.getEyePosition();
                Vec3 look = entity.getLookAngle();
                Vec3 end = start.add(look.scale(range));

                BlockHitResult block = server.clip(new ClipContext(start, end,
                        ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));
                double limitSqr = block.getType() == HitResult.Type.MISS ? range * range
                        : block.getLocation().distanceToSqr(start);

                AABB box = entity.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0);
                EntityHitResult hit = ProjectileUtil.getEntityHitResult(entity, start, end, box,
                        e -> e instanceof LivingEntity && e.isAlive() && !e.isSpectator()
                                && e.isPickable() && !entity.isAlliedTo(e),
                        limitSqr);

                if (hit != null) {
                    targetId = hit.getEntity().getUUID();
                    position = hit.getEntity().position();
                } else {
                    targetId = null;
                    position = block.getType() == HitResult.Type.MISS ? end : block.getLocation();
                }
            }

            boolean rain = server.isRainingAt(BlockPos.containing(position));
            double bonus = waterBonus(server, entity.blockPosition()) + (rain ? RAIN_BONUS : 0.0);
            float damage = (float) (getSpellPower(spellLevel, entity) * (1.0 + bonus));
            double radius = RADIUS_BASE + RADIUS_PER_LEVEL * spellLevel;

            WaterSphereManager.start(server, entity.getUUID(), targetId, position, radius,
                    damage, defensive, spellLevel, rain);

            if (rain && entity instanceof ServerPlayer player) {
                player.displayClientMessage(Component.translatable("ui.whimsical_spells.reamstroha_rain"), true);
            }
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    /** Bonus (0 a MAX_WATER_BONUS) según cuánta agua hay alrededor del lanzador. */
    private static double waterBonus(ServerLevel level, BlockPos center) {
        int count = 0;
        int r = WATER_SCAN_RADIUS;
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (level.getFluidState(center.offset(dx, dy, dz)).is(FluidTags.WATER)) {
                        count++;
                    }
                }
            }
        }
        return Math.min(MAX_WATER_BONUS, count / WATER_BLOCKS_FOR_MAX * MAX_WATER_BONUS);
    }
}
