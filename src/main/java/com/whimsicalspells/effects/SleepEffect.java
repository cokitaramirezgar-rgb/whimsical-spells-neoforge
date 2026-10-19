package com.whimsicalspells.effects;

import com.whimsicalspells.WhimsicalSpells;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/** Sueño: no se puede mover, saltar ni hacer daño. Se rompe al recibir daño (ver ModEvents). */
public class SleepEffect extends MobEffect {
    public SleepEffect() {
        super(MobEffectCategory.HARMFUL, 0x4B3F9E);
        addAttributeModifier(Attributes.MOVEMENT_SPEED, WhimsicalSpells.id("sleep_speed"),
                -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        addAttributeModifier(Attributes.JUMP_STRENGTH, WhimsicalSpells.id("sleep_jump"),
                -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        addAttributeModifier(Attributes.ATTACK_DAMAGE, WhimsicalSpells.id("sleep_damage"),
                -1000.0, AttributeModifier.Operation.ADD_VALUE);
    }
}
