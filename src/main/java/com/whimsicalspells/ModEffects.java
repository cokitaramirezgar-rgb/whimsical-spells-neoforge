package com.whimsicalspells;

import com.whimsicalspells.effects.ManaSuppressionEffect;
import com.whimsicalspells.effects.ShadowBindEffect;
import com.whimsicalspells.effects.SleepEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, WhimsicalSpells.MODID);

    public static final DeferredHolder<MobEffect, ManaSuppressionEffect> MANA_SUPPRESSION =
            EFFECTS.register("mana_suppression", ManaSuppressionEffect::new);

    public static final DeferredHolder<MobEffect, SleepEffect> SLEEP =
            EFFECTS.register("sleep", SleepEffect::new);

    public static final DeferredHolder<MobEffect, ShadowBindEffect> SHADOW_BIND =
            EFFECTS.register("shadow_bind", ShadowBindEffect::new);
}
