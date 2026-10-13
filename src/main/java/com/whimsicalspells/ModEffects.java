package com.whimsicalspells;

import com.whimsicalspells.effects.ManaSuppressionEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, WhimsicalSpells.MODID);

    public static final DeferredHolder<MobEffect, ManaSuppressionEffect> MANA_SUPPRESSION =
            EFFECTS.register("mana_suppression", ManaSuppressionEffect::new);
}
