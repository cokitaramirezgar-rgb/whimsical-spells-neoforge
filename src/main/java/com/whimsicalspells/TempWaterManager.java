package com.whimsicalspells;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Retira el agua creada por Reamstroha cuando se acaba su duración. */
@EventBusSubscriber(modid = WhimsicalSpells.MODID)
public class TempWaterManager {
    private static final List<Entry> ACTIVE = new ArrayList<>();

    private static class Entry {
        final ResourceKey<Level> dimension;
        final BlockPos pos;
        int ticksLeft;

        Entry(ResourceKey<Level> dimension, BlockPos pos, int ticksLeft) {
            this.dimension = dimension;
            this.pos = pos;
            this.ticksLeft = ticksLeft;
        }
    }

    public static void add(ServerLevel level, BlockPos pos, int ticks) {
        ACTIVE.add(new Entry(level.dimension(), pos.immutable(), ticks));
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (ACTIVE.isEmpty()) return;
        MinecraftServer server = event.getServer();

        Iterator<Entry> it = ACTIVE.iterator();
        while (it.hasNext()) {
            Entry entry = it.next();
            if (--entry.ticksLeft > 0) continue;

            ServerLevel level = server.getLevel(entry.dimension);
            if (level != null && level.isLoaded(entry.pos)) {
                BlockState state = level.getBlockState(entry.pos);
                // Solo se retira si sigue siendo una fuente de agua normal
                if (state.is(Blocks.WATER) && state.getFluidState().isSource()) {
                    level.setBlock(entry.pos, Blocks.AIR.defaultBlockState(), 3);
                }
            }
            it.remove();
        }
    }
}
