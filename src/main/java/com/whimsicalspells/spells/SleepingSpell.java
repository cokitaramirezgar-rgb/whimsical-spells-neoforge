package com.whimsicalspells.spells;

import com.whimsicalspells.ModEffects;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

/** Sleeping: duerme al objetivo. No puede moverse ni atacar, y despierta al recibir daño. */
public class SleepingSpell extends WhimsicalSpell {
    private static final int MAX_LEVEL = 3;
    private static final double RANGE = 16.0;
    private static final int[] DURATION_SECONDS = {5, 8, 12};

    public SleepingSpell() {
        // cooldown 30 s, maná 35 + 5/nivel, conjuración 1.5 s
        super("sleeping", SchoolRegistry.ENDER_RESOURCE, SpellRarity.RARE, MAX_LEVEL,
                30, 35, 5, 30, 1, 0);
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (level instanceof ServerLevel server) {
            int l = clampLevel(spellLevel);
            LivingEntity target = Targeting.livingInSight(entity, RANGE, Targeting.nonAlly(entity));
            if (target == null) {
                Targeting.tell(entity, "ui.whimsical_spells.no_target");
            } else {
                target.addEffect(new MobEffectInstance(ModEffects.SLEEP, DURATION_SECONDS[l - 1] * 20, 0, false, true, true));
                if (target instanceof Mob mob) {
                    mob.setTarget(null);
                }
                server.sendParticles(ParticleTypes.CLOUD, target.getX(), target.getY(1.0), target.getZ(),
                        15, 0.4, 0.3, 0.4, 0.01);
                server.sendParticles(ParticleTypes.NOTE, target.getX(), target.getY(1.2), target.getZ(),
                        4, 0.3, 0.2, 0.3, 0.0);
                server.playSound(null, target.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE,
                        SoundSource.PLAYERS, 0.8f, 0.5f);
            }
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }
}
