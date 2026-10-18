package com.whimsicalspells.spells;

import com.whimsicalspells.TaskManager;
import com.whimsicalspells.WhimsicalSpells;
import com.whimsicalspells.util.TargetUtil;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

import java.util.UUID;

/** Venda de humo: el humo se convierte en una venda sobre los ojos del objetivo. */
public class SmokeBlindfoldSpell extends AbstractSpell {
    private final ResourceLocation spellId = WhimsicalSpells.id("smoke_blindfold");

    private static final int CAST_TIME_TICKS = 20; // 1 s de conjuración
    private static final int MAX_LEVEL = 3;
    private static final double RANGE = 16.0;
    private static final int[] SECONDS = {4, 6, 8};

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(MAX_LEVEL)
            .setCooldownSeconds(12)
            .build();

    public SmokeBlindfoldSpell() {
        this.baseManaCost = 25;
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 0;
        this.castTime = CAST_TIME_TICKS;
    }

    @Override
    public ResourceLocation getSpellResource() { return spellId; }

    @Override
    public DefaultConfig getDefaultConfig() { return defaultConfig; }

    @Override
    public CastType getCastType() { return CastType.LONG; }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (level instanceof ServerLevel server) {
            int l = Math.max(1, Math.min(spellLevel, MAX_LEVEL));
            LivingEntity target = TargetUtil.pickEntity(entity, RANGE);

            if (target == null) {
                if (entity instanceof ServerPlayer player) {
                    player.displayClientMessage(Component.translatable("ui.whimsical_spells.no_target"), true);
                }
            } else {
                int ticks = SECONDS[l - 1] * 20;
                target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, ticks, 0));
                server.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                        target.getX(), target.getEyeY(), target.getZ(), 25, 0.3, 0.2, 0.3, 0.02);
                server.playSound(null, target.blockPosition(), SoundEvents.FIRECHARGE_USE,
                        SoundSource.PLAYERS, 0.8f, 0.7f);

                // Los mobs casi ignoran la ceguera: además pierden su objetivo y tropiezan
                if (target instanceof Mob) {
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ticks, 1));
                    followBlindMob(server.dimension(), target.getUUID(), ticks);
                }
            }
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    private static void followBlindMob(ResourceKey<Level> dimension, UUID mobId, int ticks) {
        int[] age = {0};
        TaskManager.add(server -> {
            ServerLevel level = server.getLevel(dimension);
            if (level == null) return true;
            Entity found = level.getEntity(mobId);
            if (!(found instanceof Mob mob) || !mob.isAlive() || age[0] >= ticks) return true;

            if (age[0] % 5 == 0) {
                mob.setTarget(null);
                mob.getNavigation().stop();
            }
            if (age[0] % 10 == 0) {
                level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                        mob.getX(), mob.getEyeY(), mob.getZ(), 3, 0.25, 0.15, 0.25, 0.01);
            }
            age[0]++;
            return false;
        });
    }
}
