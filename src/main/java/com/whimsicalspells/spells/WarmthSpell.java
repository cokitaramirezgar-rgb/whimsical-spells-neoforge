package com.whimsicalspells.spells;

import com.whimsicalspells.util.Targeting;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/** Warmth Spell: quita el congelamiento y la lentitud del frío a ti y a tus aliados cercanos. */
public class WarmthSpell extends WhimsicalSpell {
    private static final int MAX_LEVEL = 3;
    private static final double[] RADIUS = {4.0, 6.0, 8.0};

    public WarmthSpell() {
        // instantáneo, cooldown 10 s, maná 15
        super("warmth_spell", SchoolRegistry.FIRE_RESOURCE, SpellRarity.COMMON, MAX_LEVEL,
                10, 15, 0, 0, 1, 0);
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (level instanceof ServerLevel server) {
            double radius = RADIUS[clampLevel(spellLevel) - 1];
            for (LivingEntity target : Targeting.withAllies(entity, radius)) {
                target.setTicksFrozen(0);
                target.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                server.sendParticles(ParticleTypes.SMALL_FLAME, target.getX(), target.getY(0.5), target.getZ(),
                        12, 0.4, 0.6, 0.4, 0.02);
                server.sendParticles(ParticleTypes.HAPPY_VILLAGER, target.getX(), target.getY(0.8), target.getZ(),
                        5, 0.4, 0.5, 0.4, 0.0);
            }
            server.playSound(null, entity.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.6f, 1.6f);
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }
}
