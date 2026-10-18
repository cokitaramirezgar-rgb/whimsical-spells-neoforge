package com.whimsicalspells;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(WhimsicalSpells.MODID)
public class WhimsicalSpells {
    public static final String MODID = "whimsical_spells";

    public WhimsicalSpells(IEventBus modBus) {
        ModSpells.SPELLS.register(modBus);
        ModEffects.EFFECTS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModAttachments.ATTACHMENTS.register(modBus);
        modBus.addListener(ModItems::addToCreativeTab);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
