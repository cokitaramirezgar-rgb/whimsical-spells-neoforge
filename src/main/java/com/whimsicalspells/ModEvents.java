package com.whimsicalspells;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

@EventBusSubscriber(modid = WhimsicalSpells.MODID)
public class ModEvents {
    /** Multiplicador de visibilidad según el nivel de Supresión de maná (nivel 1, 2, 3). */
    private static final double[] VISIBILITY = {0.5, 0.3, 0.15};

    @SubscribeEvent
    public static void onVisibility(LivingEvent.LivingVisibilityEvent event) {
        LivingEntity target = event.getEntity();
        MobEffectInstance effect = target.getEffect(ModEffects.MANA_SUPPRESSION);
        if (effect != null) {
            int index = Math.min(effect.getAmplifier(), VISIBILITY.length - 1);
            event.modifyVisibility(VISIBILITY[index]);
        }
    }

    /** Sleeping se rompe al recibir daño. */
    @SubscribeEvent
    public static void onDamaged(LivingDamageEvent.Post event) {
        LivingEntity entity = event.getEntity();
        if (event.getNewDamage() > 0 && entity.hasEffect(ModEffects.SLEEP)) {
            entity.removeEffect(ModEffects.SLEEP);
        }
    }
}
