package com.whimsicalspells;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Retira los bloques temporales de hechizos (agua de Aqua Create, luz de Lighting) cuando acaba su duración. */
@EventBusSubscriber(modid = WhimsicalSpells.MODID)
public class TempBlockManager {
    private static final List<Entry> ACTIVE = new ArrayList<>();

    private static class Entry {
        final ResourceKey<Level> dimension;
        final BlockPos pos;
        final Block block;
        final ParticleOptions ambient;
        int ticksLeft;

        Entry(ResourceKey<Level> dimension, BlockPos pos, Block block, int ticksLeft, ParticleOptions ambient) {
            this.dimension = dimension;
            this.pos = pos;
            this.block = block;
            this.ticksLeft = ticksLeft;
            this.ambient = ambient;
        }
    }

    /** ambient: partículas que se repiten cada segundo sobre el bloque (puede ser null). */
    public static void add(ServerLevel level, BlockPos pos, Block block, int ticks, ParticleOptions ambient) {
        ACTIVE.add(new Entry(level.dimension(), pos.immutable(), block, ticks, ambient));
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (ACTIVE.isEmpty()) return;
        MinecraftServer server = event.getServer();

        Iterator<Entry> it = ACTIVE.iterator();
        while (it.hasNext()) {
            Entry entry = it.next();
            ServerLevel level = server.getLevel(entry.dimension);
            if (level == null) {
                it.remove();
                continue;
            }
            if (entry.ambient != null && entry.ticksLeft % 20 == 0 && level.hasChunkAt(entry.pos)) {
                level.sendParticles(entry.ambient, entry.pos.getX() + 0.5, entry.pos.getY() + 0.5,
                        entry.pos.getZ() + 0.5, 2, 0.15, 0.15, 0.15, 0.01);
            }
            if (--entry.ticksLeft > 0) continue;

            if (level.hasChunkAt(entry.pos)) {
                removeBlock(level, entry);
                it.remove();
            } else {
                entry.ticksLeft = 200; // chunk descargado: reintenta más tarde
            }
        }
    }

    /** Al cerrar el servidor se limpian todos los bloques temporales para que no queden para siempre. */
    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        MinecraftServer server = event.getServer();
        for (Entry entry : ACTIVE) {
            ServerLevel level = server.getLevel(entry.dimension);
            if (level != null && level.hasChunkAt(entry.pos)) {
                removeBlock(level, entry);
            }
        }
        ACTIVE.clear();
    }

    private static void removeBlock(ServerLevel level, Entry entry) {
        BlockState state = level.getBlockState(entry.pos);
        if (!state.is(entry.block)) return;
        // El agua solo se retira si sigue siendo una fuente normal
        if (entry.block == Blocks.WATER && !state.getFluidState().isSource()) return;
        level.setBlock(entry.pos, Blocks.AIR.defaultBlockState(), 3);
    }
}
