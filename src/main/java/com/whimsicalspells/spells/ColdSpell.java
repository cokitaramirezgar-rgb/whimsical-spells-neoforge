package com.whimsicalspells.spells;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/** Cold Spell: una oleada de frío a tu alrededor que congela y ralentiza a los enemigos y apaga su fuego. */
public class ColdSpell extends WhimsicalSpell {
    private static final int MAX_LEVEL = 3;
    private static final double[] RADIUS = {4.0, 5.0, 6.0};

    public ColdSpell() {
        // cooldown 15 s, maná 30 + 5/nivel, conjuración 1.5 s
        super("cold_spell", SchoolRegistry.ICE_RESOURCE, SpellRarity.UNCOMMON, MAX_LEVEL,
                15, 30, 5, 30, 1, 0);
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (level instanceof ServerLevel server) {
            int l = clampLevel(spellLevel);
            double radius = RADIUS[l - 1];
            for (LivingEntity target : server.getEntitiesOfClass(LivingEntity.class,
                    entity.getBoundingBox().inflate(radius),
                    e -> e != entity && e.isAlive() && !entity.isAlliedTo(e)
                            && e.distanceToSqr(entity) <= radius * radius)) {
                target.setTicksFrozen(target.getTicksFrozen() + 100 + 40 * l);
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80 + 20 * l, 0));
                target.clearFire();
                server.sendParticles(ParticleTypes.SNOWFLAKE, target.getX(), target.getY(0.5), target.getZ(),
                        15, 0.4, 0.6, 0.4, 0.02);
            }
            server.sendParticles(ParticleTypes.SNOWFLAKE, entity.getX(), entity.getY(0.3), entity.getZ(),
                    80, radius * 0.5, 0.3, radius * 0.5, 0.05);
            server.playSound(null, entity.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.8f, 0.7f);
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }
}
