package com.whimsicalspells.client;

import com.whimsicalspells.WhimsicalSpells;
import com.whimsicalspells.ability.Ability;
import com.whimsicalspells.network.ToggleAbilityPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = WhimsicalSpells.MODID, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        while (ClientModEvents.FLIGHT_KEY.consumeClick()) {
            PacketDistributor.sendToServer(new ToggleAbilityPayload(Ability.FLIGHT.id()));
        }
        while (ClientModEvents.MEDITATION_KEY.consumeClick()) {
            PacketDistributor.sendToServer(new ToggleAbilityPayload(Ability.MEDITATION.id()));
        }
    }
}
