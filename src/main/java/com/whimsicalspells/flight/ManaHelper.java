package com.whimsicalspells.flight;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.server.level.ServerPlayer;

/** Único punto de contacto con el maná de Iron's: si su API cambia, solo hay que tocar este archivo. */
public class ManaHelper {
    public static float get(ServerPlayer player) {
        return MagicData.getPlayerMagicData(player).getMana();
    }

    public static float max(ServerPlayer player) {
        return (float) player.getAttributeValue(AttributeRegistry.MAX_MANA);
    }

    /** Resta maná si hay suficiente. Devuelve false (sin restar nada) si no alcanza. */
    public static boolean drain(ServerPlayer player, float amount) {
        MagicData data = MagicData.getPlayerMagicData(player);
        if (data.getMana() < amount) return false;
        data.setMana(data.getMana() - amount);
        return true;
    }

    /** Suma maná sin pasar del máximo. */
    public static void add(ServerPlayer player, float amount) {
        MagicData data = MagicData.getPlayerMagicData(player);
        data.setMana(Math.min(max(player), data.getMana() + amount));
    }
}
