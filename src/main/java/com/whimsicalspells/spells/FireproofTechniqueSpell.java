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

/** Fireproof Technique: resistencia al fuego para ti y tus aliados cercanos. */
public class FireproofTechniqueSpell extends WhimsicalSpell {
    private static final int MAX_LEVEL = 3;
    private static final double ALLY_RADIUS = 6.0;
    private static final int[] DURATION_SECONDS = {120, 240, 360};

    public FireproofTechniqueSpell() {
        // instantáneo, cooldown 20 s, maná 20 + 5/nivel
        super("fireproof_technique", SchoolRegistry.FIRE_RESOURCE, SpellRarity.UNCOMMON, MAX_LEVEL,
                20, 20, 5, 0, 1, 0);
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (level instanceof ServerLevel server) {
            int ticks = DURATION_SECONDS[clampLevel(spellLevel) - 1] * 20;
            for (LivingEntity target : Targeting.withAllies(entity, ALLY_RADIUS)) {
                target.clearFire();
                target.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, ticks, 0));
                server.sendParticles(ParticleTypes.SMALL_FLAME, target.getX(), target.getY(0.5), target.getZ(),
                        20, 0.4, 0.6, 0.4, 0.02);
            }
            server.playSound(null, entity.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.8f, 1.2f);
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }
}
