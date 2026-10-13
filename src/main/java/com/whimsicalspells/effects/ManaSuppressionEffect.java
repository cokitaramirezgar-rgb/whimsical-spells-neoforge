package com.whimsicalspells.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Efecto sin lógica propia: los mobs te detectan desde mucho menos distancia (ver ModEvents). */
public class ManaSuppressionEffect extends MobEffect {
    public ManaSuppressionEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x8A6BFF);
    }
}
