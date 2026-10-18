package com.whimsicalspells.ability;

import com.whimsicalspells.ModAttachments;
import com.whimsicalspells.flight.ManaHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.phys.Vec3;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Lógica de las habilidades pasivas: activar/desactivar, consumo de maná, XP y niveles. Todo en el servidor. */
public class AbilityManager {
    // --- Equilibrio (fáciles de cambiar) ---
    /** Maná mínimo para activar el vuelo. */
    private static final float FLIGHT_START_MANA = 15f;
    /** Maná por segundo VOLANDO (en el suelo no gasta). Mejora con el nivel: 6.0 -> 4.0. */
    private static float flightCostPerSecond(int level) { return 6.0f - 0.5f * (level - 1); }
    /** Velocidad de vuelo: 0.04 (más lento que creativo, 0.05) -> x1.6 en el nivel 5. */
    private static float flightSpeed(int level) { return 0.04f * (1.0f + 0.15f * (level - 1)); }
    /** Maná extra por segundo al meditar (agachado y quieto): 1.0 -> 3.0. */
    private static float meditationRegenPerSecond(int level) { return 1.0f + 0.5f * (level - 1); }
    /** Bloques^2 mínimos recorridos por segundo para que volar dé XP (evita subir de nivel flotando sin moverse). */
    private static final double FLIGHT_XP_MIN_MOVE_SQR = 4.0;
    private static final int SLOW_FALL_TICKS = 100; // 5 s de caída lenta al terminar el vuelo en el aire
    private static final float DEFAULT_FLYING_SPEED = 0.05f;

    /** Marca en los datos del jugador: si quedó puesta tras un cierre brusco, el vuelo se revoca al entrar. */
    private static final String FLIGHT_TAG = "whimsical_spells_flight";

    private static final Map<Ability, Set<UUID>> ACTIVE = new EnumMap<>(Ability.class);
    private static final Map<UUID, Vec3> LAST_POS = new HashMap<>();

    static {
        for (Ability a : Ability.values()) {
            ACTIVE.put(a, new HashSet<>());
        }
    }

    public static boolean isActive(ServerPlayer player, Ability ability) {
        return ACTIVE.get(ability).contains(player.getUUID());
    }

    // ---------------------------------------------------------------- activar / desactivar

    public static void toggle(ServerPlayer player, Ability ability) {
        AbilityData data = player.getData(ModAttachments.ABILITIES);
        if (!data.isUnlocked(ability)) {
            player.displayClientMessage(Component.translatable("ui.whimsical_spells.ability_locked", ability.displayName()), true);
            return;
        }
        if (isActive(player, ability)) {
            deactivate(player, ability, false);
            return;
        }
        if (ability == Ability.FLIGHT) {
            enableFlight(player, data);
        } else {
            ACTIVE.get(ability).add(player.getUUID());
            player.displayClientMessage(Component.translatable("ui.whimsical_spells.ability_on", ability.displayName()), true);
            playToggleSound(player);
        }
    }

    private static void enableFlight(ServerPlayer player, AbilityData data) {
        if (player.isCreative() || player.isSpectator()) {
            player.displayClientMessage(Component.translatable("ui.whimsical_spells.flight_creative"), true);
            return;
        }
        if (ManaHelper.get(player) < FLIGHT_START_MANA) {
            player.displayClientMessage(Component.translatable("ui.whimsical_spells.flight_no_mana"), true);
            return;
        }
        ACTIVE.get(Ability.FLIGHT).add(player.getUUID());
        LAST_POS.put(player.getUUID(), player.position());
        player.getPersistentData().putBoolean(FLIGHT_TAG, true);

        Abilities abilities = player.getAbilities();
        abilities.mayfly = true;
        abilities.flying = true; // despega al activar
        abilities.setFlyingSpeed(flightSpeed(Math.max(1, data.getLevel(Ability.FLIGHT))));
        player.onUpdateAbilities();

        player.displayClientMessage(Component.translatable("ui.whimsical_spells.flight_on"), true);
        playToggleSound(player);
    }

    public static void deactivate(ServerPlayer player, Ability ability, boolean outOfMana) {
        if (!ACTIVE.get(ability).remove(player.getUUID())) return;

        if (ability == Ability.FLIGHT) {
            revokeFlight(player);
            // Caída suave para no morir al terminar el vuelo en el aire
            if (!player.isCreative() && !player.isSpectator() && !player.onGround()) {
                player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, SLOW_FALL_TICKS, 0, false, false, true));
            }
            player.displayClientMessage(Component.translatable(
                    outOfMana ? "ui.whimsical_spells.flight_out_of_mana" : "ui.whimsical_spells.flight_off"), true);
        } else {
            player.displayClientMessage(Component.translatable("ui.whimsical_spells.ability_off", ability.displayName()), true);
        }
    }

    /** Quita el permiso de vuelo que dimos nosotros (nunca el de creativo/espectador). */
    private static void revokeFlight(ServerPlayer player) {
        ACTIVE.get(Ability.FLIGHT).remove(player.getUUID());
        LAST_POS.remove(player.getUUID());
        player.getPersistentData().remove(FLIGHT_TAG);
        if (player.isCreative() || player.isSpectator()) return;

        Abilities abilities = player.getAbilities();
        abilities.mayfly = false;
        abilities.flying = false;
        abilities.setFlyingSpeed(DEFAULT_FLYING_SPEED);
        player.onUpdateAbilities();
    }

    /** Quita todas las habilidades activas sin mensajes (al salir, morir, etc.). */
    public static void deactivateAllQuietly(ServerPlayer player) {
        ACTIVE.get(Ability.MEDITATION).remove(player.getUUID());
        if (ACTIVE.get(Ability.FLIGHT).contains(player.getUUID())
                || player.getPersistentData().getBoolean(FLIGHT_TAG)) {
            revokeFlight(player);
        }
    }

    /** Al entrar: si el vuelo quedó marcado tras un cierre brusco, se revoca. */
    public static void sanitizeOnLogin(ServerPlayer player) {
        if (player.getPersistentData().getBoolean(FLIGHT_TAG)) {
            revokeFlight(player);
        }
    }

    private static void playToggleSound(ServerPlayer player) {
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE,
                SoundSource.PLAYERS, 0.5f, 1.6f);
    }

    // ---------------------------------------------------------------- tick

    public static void tick(ServerPlayer player) {
        UUID id = player.getUUID();
        boolean flight = ACTIVE.get(Ability.FLIGHT).contains(id);
        boolean meditation = ACTIVE.get(Ability.MEDITATION).contains(id);
        if (!flight && !meditation) return;

        if (flight) tickFlight(player);
        if (meditation) tickMeditation(player);
    }

    private static void tickFlight(ServerPlayer player) {
        if (player.isCreative() || player.isSpectator()) {
            revokeFlight(player);
            return;
        }
        AbilityData data = player.getData(ModAttachments.ABILITIES);
        int level = Math.max(1, data.getLevel(Ability.FLIGHT));

        // Mantiene el permiso (otros efectos o cambios de modo pueden quitarlo)
        Abilities abilities = player.getAbilities();
        if (!abilities.mayfly) {
            abilities.mayfly = true;
            player.onUpdateAbilities();
        }

        if (!abilities.flying) {
            return; // activado pero en el suelo: no gasta maná ni da XP
        }

        // Consumo continuo mientras vuelas
        if (!ManaHelper.drain(player, flightCostPerSecond(level) / 20f)) {
            deactivate(player, Ability.FLIGHT, true);
            return;
        }
        if (player.tickCount % 3 == 0 && player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    player.getX(), player.getY() + 0.1, player.getZ(), 1, 0.2, 0.0, 0.2, 0.01);
        }

        // Cada segundo: XP si te has movido de verdad, y barra de progreso
        if (player.tickCount % 20 == 0) {
            Vec3 now = player.position();
            Vec3 before = LAST_POS.put(player.getUUID(), now);
            if (before != null && before.distanceToSqr(now) >= FLIGHT_XP_MIN_MOVE_SQR) {
                gainXp(player, data, Ability.FLIGHT, 1);
            }
            player.displayClientMessage(progress(Ability.FLIGHT, data), true);
        }
    }

    private static void tickMeditation(ServerPlayer player) {
        if (player.tickCount % 20 != 0) return;

        boolean still = player.isShiftKeyDown() && player.onGround()
                && player.getDeltaMovement().horizontalDistanceSqr() < 0.0004;
        if (!still) return;

        float missing = ManaHelper.max(player) - ManaHelper.get(player);
        if (missing <= 0f) return; // maná lleno: no hay nada que entrenar

        AbilityData data = player.getData(ModAttachments.ABILITIES);
        int level = Math.max(1, data.getLevel(Ability.MEDITATION));
        ManaHelper.add(player, Math.min(meditationRegenPerSecond(level), missing));
        gainXp(player, data, Ability.MEDITATION, 1);

        player.serverLevel().sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 1.0, player.getZ(),
                8, 0.5, 0.8, 0.5, 0.1);
        player.displayClientMessage(progress(Ability.MEDITATION, data), true);
    }

    // ---------------------------------------------------------------- XP

    private static void gainXp(ServerPlayer player, AbilityData data, Ability ability, int amount) {
        if (data.addXp(ability, amount)) {
            int level = data.getLevel(ability);
            player.sendSystemMessage(Component.translatable("ui.whimsical_spells.ability_levelup",
                    ability.displayName(), level).withStyle(ChatFormatting.GOLD));
            player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP,
                    SoundSource.PLAYERS, 0.8f, 1.2f);
            player.serverLevel().sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    player.getX(), player.getY() + 1.0, player.getZ(), 15, 0.5, 0.7, 0.5, 0.1);
            if (ability == Ability.FLIGHT && isActive(player, Ability.FLIGHT)) {
                player.getAbilities().setFlyingSpeed(flightSpeed(level)); // aplica la nueva velocidad
                player.onUpdateAbilities();
            }
        }
    }

    /** Línea de progreso: nombre, nivel, barra de XP propia de la habilidad y XP actual / necesaria. */
    public static Component progress(Ability ability, AbilityData data) {
        int level = data.getLevel(ability);
        if (level >= ability.maxLevel()) {
            return Component.translatable("ui.whimsical_spells.ability_progress_max", ability.displayName(), level);
        }
        int need = Ability.xpForNext(level);
        int xp = data.getXp(ability);
        int filled = Math.min(10, xp * 10 / need);
        String bar = "█".repeat(filled) + "░".repeat(10 - filled);
        return Component.translatable("ui.whimsical_spells.ability_progress",
                ability.displayName(), level, bar, xp, need);
    }
}
