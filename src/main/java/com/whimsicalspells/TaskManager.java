package com.whimsicalspells;

import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.List;

/** Ejecuta tareas que duran varios ticks (efectos de zona, control de mobs, cúpulas...). */
@EventBusSubscriber(modid = WhimsicalSpells.MODID)
public class TaskManager {
    @FunctionalInterface
    public interface Task {
        /** Se llama una vez por tick. Devuelve true cuando la tarea ha terminado. */
        boolean tick(MinecraftServer server);
    }

    private static final List<Task> ACTIVE = new ArrayList<>();

    public static void add(Task task) {
        ACTIVE.add(task);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (ACTIVE.isEmpty()) return;
        MinecraftServer server = event.getServer();
        ACTIVE.removeIf(task -> {
            try {
                return task.tick(server);
            } catch (Exception e) {
                e.printStackTrace();
                return true; // una tarea con error no debe tumbar el servidor
            }
        });
    }
}
