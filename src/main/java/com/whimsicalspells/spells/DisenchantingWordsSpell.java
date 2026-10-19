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
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.ArrayList;

/** Disenchanting Words: quita los efectos negativos a ti o al aliado que miras. */
public class DisenchantingWordsSpell extends WhimsicalSpell {
    private static final double RANGE = 12.0;

    public DisenchantingWordsSpell() {
        // 1 nivel, cooldown 25 s, maná 30, conjuración 1 s
        super("disenchanting_words", SchoolRegistry.HOLY_RESOURCE, SpellRarity.UNCOMMON, 1,
                25, 30, 0, 20, 1, 0);
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (level instanceof ServerLevel server) {
            LivingEntity target = Targeting.livingInSight(entity, RANGE, Targeting.ally(entity));
            if (target == null) target = entity;

            for (MobEffectInstance effect : new ArrayList<>(target.getActiveEffects())) {
                if (effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL) {
                    target.removeEffect(effect.getEffect());
                }
            }
            target.setTicksFrozen(0);
            server.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY(0.5), target.getZ(),
                    25, 0.4, 0.7, 0.4, 0.05);
            server.sendParticles(ParticleTypes.ENCHANT, target.getX(), target.getY(0.5), target.getZ(),
                    25, 0.5, 0.7, 0.5, 0.3);
            server.playSound(null, target.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.8f, 1.5f);
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }
}
