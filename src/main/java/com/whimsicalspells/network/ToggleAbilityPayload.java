package com.whimsicalspells.network;

import com.whimsicalspells.WhimsicalSpells;
import com.whimsicalspells.ability.Ability;
import com.whimsicalspells.ability.AbilityManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Del cliente al servidor: "activa/desactiva esta habilidad". */
public record ToggleAbilityPayload(String ability) implements CustomPacketPayload {
    public static final Type<ToggleAbilityPayload> TYPE = new Type<>(WhimsicalSpells.id("toggle_ability"));

    public static final StreamCodec<ByteBuf, ToggleAbilityPayload> STREAM_CODEC =
            ByteBufCodecs.STRING_UTF8.map(ToggleAbilityPayload::new, ToggleAbilityPayload::ability);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ToggleAbilityPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                Ability ability = Ability.byId(payload.ability());
                if (ability != null) {
                    AbilityManager.toggle(player, ability);
                }
            }
        });
    }
}
