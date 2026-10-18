package com.whimsicalspells;

import com.whimsicalspells.ability.Ability;
import com.whimsicalspells.item.AbilityScrollItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(WhimsicalSpells.MODID);

    public static final DeferredItem<Item> FLIGHT_SCROLL = ITEMS.registerItem("ability_scroll_flight",
            props -> new AbilityScrollItem(Ability.FLIGHT, props.stacksTo(16).rarity(Rarity.RARE)));

    public static final DeferredItem<Item> MEDITATION_SCROLL = ITEMS.registerItem("ability_scroll_meditation",
            props -> new AbilityScrollItem(Ability.MEDITATION, props.stacksTo(16).rarity(Rarity.UNCOMMON)));

    public static void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(FLIGHT_SCROLL);
            event.accept(MEDITATION_SCROLL);
        }
    }
}
