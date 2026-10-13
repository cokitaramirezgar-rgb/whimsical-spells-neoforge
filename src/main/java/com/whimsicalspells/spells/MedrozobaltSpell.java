package com.whimsicalspells.spells;

import com.whimsicalspells.WhimsicalSpells;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Medrozobalt: el hechizo que convierte un ataque en un torbellino. */
public class MedrozobaltSpell extends AbstractSpell {
    private final ResourceLocation spellId = WhimsicalSpells.id("medrozobalt");

    private static final int CAST_TIME_TICKS = 30; // 1.5 s de conjuración
    private static final int MAX_LEVEL = 5;
    private static final double RADIUS_BASE = 3.0;
    private static final double RADIUS_PER_LEVEL = 0.5;
    private static final double PUSH_BASE = 1.0;
    private static final double PUSH_PER_LEVEL = 0.2;
    private static final float DAMAGE_FACTOR = 0.5f;

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(MAX_LEVEL)
            .setCooldownSeconds(8)
            .build();

    public MedrozobaltSpell() {
        this.baseManaCost = 30;
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 4;
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
            float damage = getSpellPower(spellLevel, entity) * DAMAGE_FACTOR;
            double radius = RADIUS_BASE + RADIUS_PER_LEVEL * (spellLevel - 1);
            double push = PUSH_BASE + PUSH_PER_LEVEL * (spellLevel - 1);

            AABB box = entity.getBoundingBox().inflate(radius);
            for (LivingEntity target : server.getEntitiesOfClass(LivingEntity.class, box,
                    e -> e != entity && e.isAlive() && !entity.isAlliedTo(e)
                            && e.distanceToSqr(entity) <= radius * radius)) {
                Vec3 away = new Vec3(target.getX() - entity.getX(), 0, target.getZ() - entity.getZ());
                away = away.lengthSqr() < 1.0E-4 ? new Vec3(1, 0, 0) : away.normalize();

                target.hurt(server.damageSources().indirectMagic(entity, entity), damage);
                target.setDeltaMovement(target.getDeltaMovement().add(away.x * push, 0.45, away.z * push));
                target.hurtMarked = true;
            }

            for (int arm = 0; arm < 3; arm++) {
                for (double h = 0; h <= 2.2; h += 0.15) {
                    double r = 0.6 + (radius - 0.6) * (h / 2.2);
                    double angle = h * 4.0 + arm * (2 * Math.PI / 3);
                    server.sendParticles(ParticleTypes.CLOUD,
                            entity.getX() + Math.cos(angle) * r,
                            entity.getY() + h * 0.8,
                            entity.getZ() + Math.sin(angle) * r,
                            1, 0, 0, 0, 0.02);
                }
            }
            server.sendParticles(ParticleTypes.SWEEP_ATTACK,
                    entity.getX(), entity.getY(0.5), entity.getZ(), 3, radius * 0.4, 0.2, radius * 0.4, 0);
            server.playSound(null, entity.blockPosition(), SoundEvents.PLAYER_ATTACK_KNOCKBACK,
                    SoundSource.PLAYERS, 1.0f, 0.8f);
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }
}
