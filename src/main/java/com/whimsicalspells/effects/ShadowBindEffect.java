package com.whimsicalspells.effects;

import com.whimsicalspells.WhimsicalSpells;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/** Sombra clavada: no se puede mover ni saltar (sí puede atacar a lo que tenga al lado). */
public class ShadowBindEffect extends MobEffect {
    public ShadowBindEffect() {
        super(MobEffectCategory.HARMFUL, 0x1B1B2F);
        addAttributeModifier(Attributes.MOVEMENT_SPEED, WhimsicalSpells.id("shadow_bind_speed"),
                -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        addAttributeModifier(Attributes.JUMP_STRENGTH, WhimsicalSpells.id("shadow_bind_jump"),
                -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }
}
