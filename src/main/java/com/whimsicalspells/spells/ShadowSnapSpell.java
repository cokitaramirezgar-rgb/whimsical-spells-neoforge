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
import net.minecraft.world.level.Level;

/** Shadow Snap: clava la sombra del objetivo y lo deja inmóvil. */
public class ShadowSnapSpell extends WhimsicalSpell {
    private static final int MAX_LEVEL = 3;
    private static final double RANGE = 24.0;
    private static final int[] DURATION_SECONDS = {3, 5, 7};

    public ShadowSnapSpell() {
        // cooldown 20 s, maná 25 + 5/nivel, conjuración 1 s
        super("shadow_snap", SchoolRegistry.ENDER_RESOURCE, SpellRarity.RARE, MAX_LEVEL,
                20, 25, 5, 20, 1, 0);
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (level instanceof ServerLevel server) {
            int l = clampLevel(spellLevel);
            LivingEntity target = Targeting.livingInSight(entity, RANGE, Targeting.nonAlly(entity));
            if (target == null) {
                Targeting.tell(entity, "ui.whimsical_spells.no_target");
            } else {
                target.addEffect(new MobEffectInstance(ModEffects.SHADOW_BIND, DURATION_SECONDS[l - 1] * 20, 0, false, true, true));
                server.sendParticles(ParticleTypes.SQUID_INK, target.getX(), target.getY() + 0.1, target.getZ(),
                        30, 0.5, 0.05, 0.5, 0.02);
                server.sendParticles(ParticleTypes.SMOKE, target.getX(), target.getY(0.5), target.getZ(),
                        20, 0.4, 0.6, 0.4, 0.02);
                server.playSound(null, target.blockPosition(), SoundEvents.ENDERMAN_TELEPORT,
                        SoundSource.PLAYERS, 0.8f, 0.5f);
            }
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }
}
