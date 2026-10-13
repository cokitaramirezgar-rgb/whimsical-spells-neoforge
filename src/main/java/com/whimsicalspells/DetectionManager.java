package com.whimsicalspells;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/** Mantiene unos segundos las columnas de partículas que marcan lo detectado por Meehkeit (solo las ve el lanzador). */
@EventBusSubscriber(modid = WhimsicalSpells.MODID)
public class DetectionManager {
    private static final int DURATION_TICKS = 200; // 10 s
    private static final int PULSE_EVERY = 8;

    private static final List<Mark> ACTIVE = new ArrayList<>();

    private static class Mark {
        final UUID player;
        final ResourceKey<Level> dimension;
        final List<BlockPos> positions;
        int age = 0;

        Mark(UUID player, ResourceKey<Level> dimension, List<BlockPos> positions) {
            this.player = player;
            this.dimension = dimension;
            this.positions = positions;
        }
    }

    public static void start(ServerPlayer player, List<BlockPos> positions) {
        ACTIVE.removeIf(m -> m.player.equals(player.getUUID()));
        ACTIVE.add(new Mark(player.getUUID(), player.level().dimension(), positions));
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (ACTIVE.isEmpty()) return;
        MinecraftServer server = event.getServer();

        Iterator<Mark> it = ACTIVE.iterator();
        while (it.hasNext()) {
            Mark mark = it.next();
            ServerPlayer player = server.getPlayerList().getPlayer(mark.player);
            if (player == null || mark.age > DURATION_TICKS || player.level().dimension() != mark.dimension
                    || !(player.level() instanceof ServerLevel level)) {
                it.remove();
                continue;
            }
            if (mark.age % PULSE_EVERY == 0) {
                for (BlockPos pos : mark.positions) {
                    for (int i = 0; i < 6; i++) {
                        level.sendParticles(player, ParticleTypes.SOUL_FIRE_FLAME, true,
                                pos.getX() + 0.5, pos.getY() + 1.0 + i * 0.6, pos.getZ() + 0.5,
                                1, 0.1, 0.1, 0.1, 0.0);
                    }
                    level.sendParticles(player, ParticleTypes.ENCHANT, true,
                            pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 8, 0.4, 0.4, 0.4, 0.3);
                }
            }
            mark.age++;
        }
    }
}
