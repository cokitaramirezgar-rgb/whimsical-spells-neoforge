package com.whimsicalspells.spells;

import com.whimsicalspells.ModEffects;
import com.whimsicalspells.WhimsicalSpells;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/** Supresión de maná: ocultas tu presencia mágica y los mobs te detectan desde mucho menos distancia. */
public class ManaSuppressionSpell extends AbstractSpell {
    private final ResourceLocation spellId = WhimsicalSpells.id("mana_suppression");

    private static final int CAST_TIME_TICKS = 60; // 3 s de conjuración
    private static final int MAX_LEVEL = 3;
    /** Duración en segundos según el nivel (1, 2, 3). La visibilidad restante está en ModEvents. */
    private static final int[] DURATION_SECONDS = {40, 70, 110};

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.ENDER_RESOURCE)
            .setMaxLevel(MAX_LEVEL)
            .setCooldownSeconds(45)
            .build();

    public ManaSuppressionSpell() {
        this.baseManaCost = 40;
        this.manaCostPerLevel = 10;
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
            entity.addEffect(new MobEffectInstance(ModEffects.MANA_SUPPRESSION,
                    DURATION_SECONDS[l - 1] * 20, l - 1, false, false, true));

            server.sendParticles(ParticleTypes.REVERSE_PORTAL,
                    entity.getX(), entity.getY(0.5), entity.getZ(), 40, 0.6, 0.8, 0.6, 0.1);
            server.playSound(null, entity.blockPosition(), SoundEvents.BEACON_DEACTIVATE,
                    SoundSource.PLAYERS, 0.8f, 1.5f);
            if (entity instanceof ServerPlayer player) {
                player.displayClientMessage(Component.translatable("ui.whimsical_spells.suppression_on"), true);
            }
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }
}
