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

/** Strengthening: fuerza para ti y tus aliados cercanos; desde el nivel 2 también algo de resistencia al daño. */
public class StrengtheningSpell extends WhimsicalSpell {
    private static final int MAX_LEVEL = 3;
    private static final double ALLY_RADIUS = 5.0;
    private static final int DURATION_SECONDS = 45;

    public StrengtheningSpell() {
        // cooldown 45 s, maná 40 + 10/nivel, conjuración 1 s
        super("strengthening", SchoolRegistry.EVOCATION_RESOURCE, SpellRarity.RARE, MAX_LEVEL,
                45, 40, 10, 20, 1, 0);
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (level instanceof ServerLevel server) {
            int l = clampLevel(spellLevel);
            for (LivingEntity target : Targeting.withAllies(entity, ALLY_RADIUS)) {
                target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, DURATION_SECONDS * 20, l - 1));
                if (l >= 2) {
                    target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, DURATION_SECONDS * 20, 0));
                }
                server.sendParticles(ParticleTypes.ANGRY_VILLAGER, target.getX(), target.getY(1.0), target.getZ(),
                        3, 0.4, 0.4, 0.4, 0.0);
                server.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY(0.7), target.getZ(),
                        10, 0.4, 0.5, 0.4, 0.1);
            }
            server.playSound(null, entity.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.8f, 0.7f);
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }
}
