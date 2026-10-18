package com.whimsicalspells.client;

import com.whimsicalspells.WhimsicalSpells;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = WhimsicalSpells.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
    public static final String CATEGORY = "key.categories.whimsical_spells";

    public static final KeyMapping FLIGHT_KEY =
            new KeyMapping("key.whimsical_spells.toggle_flight", GLFW.GLFW_KEY_G, CATEGORY);
    public static final KeyMapping MEDITATION_KEY =
            new KeyMapping("key.whimsical_spells.toggle_meditation", GLFW.GLFW_KEY_H, CATEGORY);

    @SubscribeEvent
    public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        event.register(FLIGHT_KEY);
        event.register(MEDITATION_KEY);
    }
}
