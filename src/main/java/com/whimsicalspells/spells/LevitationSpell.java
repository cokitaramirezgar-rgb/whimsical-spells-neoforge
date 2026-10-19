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

/** Levitation: eleva a la criatura que miras o, si no miras a ninguna, a ti. Si es a ti, caes despacio al terminar. */
public class LevitationSpell extends WhimsicalSpell {
    private static final int MAX_LEVEL = 3;
    private static final double RANGE = 20.0;
    private static final int[] DURATION_SECONDS = {4, 6, 8};
    private static final int SLOW_FALL_EXTRA_TICKS = 100;

    public LevitationSpell() {
        // cooldown 15 s, maná 20 + 5/nivel, conjuración 1 s
        super("levitation", SchoolRegistry.EVOCATION_RESOURCE, SpellRarity.UNCOMMON, MAX_LEVEL,
                15, 20, 5, 20, 1, 0);
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (level instanceof ServerLevel server) {
            int ticks = DURATION_SECONDS[clampLevel(spellLevel) - 1] * 20;
            LivingEntity target = Targeting.livingInSight(entity, RANGE, e -> e != entity);
            if (target == null) target = entity;

            target.addEffect(new MobEffectInstance(MobEffects.LEVITATION, ticks, 0));
            if (target == entity) {
                // Caída suave cuando termina, para no morir
                entity.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, ticks + SLOW_FALL_EXTRA_TICKS, 0));
            }
            server.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY() + 0.1, target.getZ(),
                    20, 0.4, 0.05, 0.4, 0.05);
            server.sendParticles(ParticleTypes.CLOUD, target.getX(), target.getY() + 0.1, target.getZ(),
                    10, 0.4, 0.05, 0.4, 0.02);
            server.playSound(null, target.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE,
                    SoundSource.PLAYERS, 0.8f, 1.5f);
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }
}
