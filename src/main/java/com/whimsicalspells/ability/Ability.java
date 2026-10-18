package com.whimsicalspells.ability;

import net.minecraft.network.chat.Component;

/** Habilidades pasivas: no necesitan libro, se aprenden con un pergamino de habilidad y se activan con una tecla. */
public enum Ability {
    FLIGHT("flight", 5),
    MEDITATION("meditation", 5);

    private final String id;
    private final int maxLevel;

    Ability(String id, int maxLevel) {
        this.id = id;
        this.maxLevel = maxLevel;
    }

    public String id() { return id; }
    public int maxLevel() { return maxLevel; }

    public Component displayName() {
        return Component.translatable("ability.whimsical_spells." + id);
    }

    /** XP necesaria para pasar de este nivel al siguiente. */
    public static int xpForNext(int level) {
        return 100 * level;
    }

    public static Ability byId(String id) {
        for (Ability a : values()) {
            if (a.id.equals(id)) return a;
        }
        return null;
    }
}
