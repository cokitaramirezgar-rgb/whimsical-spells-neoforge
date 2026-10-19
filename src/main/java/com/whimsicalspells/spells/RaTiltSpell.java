package com.whimsicalspells.spells;

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
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Ra Tilt: un pilar de luz sagrada cae del cielo sobre el objetivo. Doble daño a no-muertos. */
public class RaTiltSpell extends WhimsicalSpell {
    private static final int MAX_LEVEL = 3;
    private static final double[] RANGE = {24.0, 30.0, 36.0};
    private static final double[] RADIUS = {2.5, 3.0, 3.5};
    private static final float UNDEAD_MULTIPLIER = 2.0f;
    private static final double PILLAR_HEIGHT = 24.0;

    public RaTiltSpell() {
        // cooldown 15 s, maná 40 + 10/nivel, conjuración 2 s, poder 8 + 3/nivel
        super("ra_tilt", SchoolRegistry.HOLY_RESOURCE, SpellRarity.EPIC, MAX_LEVEL,
                15, 40, 10, 40, 8, 3);
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (level instanceof ServerLevel server) {
            int l = clampLevel(spellLevel);
            double range = RANGE[l - 1];
            double radius = RADIUS[l - 1];
            float damage = getSpellPower(spellLevel, entity);

            // Objetivo: la criatura que miras o, si no, el punto del suelo que miras
            Vec3 center;
            LivingEntity target = Targeting.livingInSight(entity, range, Targeting.nonAlly(entity));
            if (target != null) {
                center = target.position();
            } else {
                BlockHitResult hit = Targeting.blockInSight(entity, range);
                if (hit.getType() == HitResult.Type.MISS) {
                    Targeting.tell(entity, "ui.whimsical_spells.no_target");
                    super.onCast(level, spellLevel, entity, castSource, playerMagicData);
                    return;
                }
                center = hit.getLocation();
            }

            AABB area = new AABB(center.x - radius, center.y - 1.0, center.z - radius,
                    center.x + radius, center.y + 4.0, center.z + radius);
            for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class, area,
                    e -> e != entity && e.isAlive() && !entity.isAlliedTo(e))) {
                double dx = victim.getX() - center.x, dz = victim.getZ() - center.z;
                if (dx * dx + dz * dz > radius * radius) continue;
                boolean undead = victim.getType().is(EntityTypeTags.UNDEAD);
                victim.hurt(server.damageSources().indirectMagic(entity, entity),
                        undead ? damage * UNDEAD_MULTIPLIER : damage);
                if (undead) victim.igniteForSeconds(4.0f);
            }

            // El pilar de luz
            for (double y = 0; y <= PILLAR_HEIGHT; y += 0.5) {
                server.sendParticles(ParticleTypes.END_ROD, center.x, center.y + y, center.z, 2, 0.3, 0.0, 0.3, 0.0);
            }
            server.sendParticles(ParticleTypes.GLOW, center.x, center.y + 0.3, center.z, 25, radius * 0.5, 0.2, radius * 0.5, 0.1);
            server.playSound(null, BlockPos.containing(center), SoundEvents.LIGHTNING_BOLT_THUNDER,
                    SoundSource.PLAYERS, 0.8f, 1.6f);
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }
}
