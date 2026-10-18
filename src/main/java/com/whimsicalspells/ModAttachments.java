package com.whimsicalspells;

import com.whimsicalspells.ability.AbilityData;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, WhimsicalSpells.MODID);

    /** Datos de habilidades pasivas del jugador. Se guardan con el jugador y sobreviven a la muerte. */
    public static final Supplier<AttachmentType<AbilityData>> ABILITIES = ATTACHMENTS.register("abilities",
            () -> AttachmentType.builder(AbilityData::new)
                    .serialize(AbilityData.CODEC)
                    .copyOnDeath()
                    .build());
}
