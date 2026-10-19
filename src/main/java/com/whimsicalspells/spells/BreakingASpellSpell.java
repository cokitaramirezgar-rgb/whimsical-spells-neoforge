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

/** Breaking a Spell: rompe los efectos positivos (los hechizos protectores y los buffs) del objetivo que miras. */
public class BreakingASpellSpell extends WhimsicalSpell {
    private static final double RANGE = 20.0;

    public BreakingASpellSpell() {
        // 1 nivel, cooldown 30 s, maná 40, conjuración 1.5 s
        super("breaking_a_spell", SchoolRegistry.EVOCATION_RESOURCE, SpellRarity.UNCOMMON, 1,
                30, 40, 0, 30, 1, 0);
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (level instanceof ServerLevel server) {
            LivingEntity target = Targeting.livingInSight(entity, RANGE, Targeting.nonAlly(entity));
            if (target == null) {
                Targeting.tell(entity, "ui.whimsical_spells.no_target");
            } else {
                int removed = 0;
                for (MobEffectInstance effect : new ArrayList<>(target.getActiveEffects())) {
                    if (effect.getEffect().value().getCategory() == MobEffectCategory.BENEFICIAL) {
                        target.removeEffect(effect.getEffect());
                        removed++;
                    }
                }
                Targeting.tell(entity, removed > 0 ? "ui.whimsical_spells.spell_break_result"
                        : "ui.whimsical_spells.spell_break_none", removed);
                server.sendParticles(ParticleTypes.ENCHANTED_HIT, target.getX(), target.getY(0.5), target.getZ(),
                        30, 0.5, 0.7, 0.5, 0.2);
                server.playSound(null, target.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.8f, 1.2f);
            }
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }
}
