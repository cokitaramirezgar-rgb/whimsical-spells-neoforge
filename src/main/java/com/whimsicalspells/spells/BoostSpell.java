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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/** Boost: más velocidad y salto para ti y tus aliados cercanos. */
public class BoostSpell extends WhimsicalSpell {
    private static final int MAX_LEVEL = 3;
    private static final double ALLY_RADIUS = 5.0;
    private static final int DURATION_SECONDS = 60;

    public BoostSpell() {
        // cooldown 30 s, maná 25 + 5/nivel, conjuración 1 s
        super("boost", SchoolRegistry.EVOCATION_RESOURCE, SpellRarity.UNCOMMON, MAX_LEVEL,
                30, 25, 5, 20, 1, 0);
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (level instanceof ServerLevel server) {
            int amplifier = clampLevel(spellLevel) - 1; // nivel 1 = I, nivel 3 = III
            for (LivingEntity target : Targeting.withAllies(entity, ALLY_RADIUS)) {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, DURATION_SECONDS * 20, amplifier));
                target.addEffect(new MobEffectInstance(MobEffects.JUMP, DURATION_SECONDS * 20, amplifier));
                server.sendParticles(ParticleTypes.CLOUD, target.getX(), target.getY() + 0.1, target.getZ(),
                        12, 0.4, 0.05, 0.4, 0.05);
            }
            server.playSound(null, entity.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.8f, 1.8f);
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }
}
