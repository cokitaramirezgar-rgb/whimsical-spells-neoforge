package com.whimsicalspells;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/** Dibuja durante unos segundos la línea de partículas del hechizo "Rastro de poder mágico" (solo la ve el lanzador). */
@EventBusSubscriber(modid = WhimsicalSpells.MODID)
public class TraceManager {
    private static final double LENGTH = 40.0;
    private static final double SPACING = 1.25;
    private static final double GROW_PER_TICK = 2.0;
    private static final int DURATION_TICKS = 120;

    private static final List<Trace> ACTIVE = new ArrayList<>();

    private static class Trace {
        final UUID player;
        final ResourceKey<Level> dimension;
        final Vec3 origin;
        final Vec3 direction;
        int age = 0;

        Trace(UUID player, ResourceKey<Level> dimension, Vec3 origin, Vec3 direction) {
            this.player = player;
            this.dimension = dimension;
            this.origin = origin;
            this.direction = direction;
        }
    }

    public static void start(ServerPlayer player, Vec3 origin, Vec3 direction) {
        ACTIVE.removeIf(t -> t.player.equals(player.getUUID()));
        ACTIVE.add(new Trace(player.getUUID(), player.level().dimension(), origin, direction));
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (ACTIVE.isEmpty()) return;
        MinecraftServer server = event.getServer();

        Iterator<Trace> it = ACTIVE.iterator();
        while (it.hasNext()) {
            Trace trace = it.next();
            ServerPlayer player = server.getPlayerList().getPlayer(trace.player);
            if (player == null || trace.age > DURATION_TICKS || player.level().dimension() != trace.dimension
                    || !(player.level() instanceof ServerLevel level)) {
                it.remove();
                continue;
            }
            if (trace.age % 2 == 0) {
                double head = Math.min(LENGTH, trace.age * GROW_PER_TICK);
                for (double d = 2.0; d <= head; d += SPACING) {
                    Vec3 p = trace.origin.add(trace.direction.scale(d));
                    level.sendParticles(player, ParticleTypes.END_ROD, false,
                            p.x, p.y, p.z, 1, 0, 0, 0, 0);
                }
            }
            trace.age++;
        }
    }
}
