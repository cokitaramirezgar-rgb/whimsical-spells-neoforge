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

/** Aqua Breathe: respirar bajo el agua, para ti y tus aliados cercanos. */
public class AquaBreatheSpell extends WhimsicalSpell {
    private static final int MAX_LEVEL = 3;
    private static final double ALLY_RADIUS = 6.0;
    private static final int[] DURATION_SECONDS = {90, 180, 300};

    public AquaBreatheSpell() {
        // instantáneo, cooldown 10 s, maná 15 + 5/nivel
        super("aqua_breathe", SchoolRegistry.ICE_RESOURCE, SpellRarity.COMMON, MAX_LEVEL,
                10, 15, 5, 0, 1, 0);
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (level instanceof ServerLevel server) {
            int ticks = DURATION_SECONDS[clampLevel(spellLevel) - 1] * 20;
            for (LivingEntity target : Targeting.withAllies(entity, ALLY_RADIUS)) {
                target.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, ticks, 0));
                server.sendParticles(ParticleTypes.SPLASH, target.getX(), target.getY(1.0), target.getZ(),
                        15, 0.4, 0.5, 0.4, 0.1);
            }
            server.playSound(null, entity.blockPosition(), SoundEvents.PLAYER_SPLASH, SoundSource.PLAYERS, 0.8f, 1.2f);
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }
}
