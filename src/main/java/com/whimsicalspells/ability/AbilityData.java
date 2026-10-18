package com.whimsicalspells.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.HashMap;
import java.util.Map;

/** Progreso de habilidades pasivas de un jugador: una entrada por habilidad aprendida (nivel y XP). */
public class AbilityData {
    public record Entry(int level, int xp) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.INT.fieldOf("level").forGetter(Entry::level),
                Codec.INT.fieldOf("xp").forGetter(Entry::xp)
        ).apply(i, Entry::new));
    }

    public static final Codec<AbilityData> CODEC = Codec.unboundedMap(Codec.STRING, Entry.CODEC)
            .xmap(map -> new AbilityData(new HashMap<>(map)), data -> data.entries);

    private final Map<String, Entry> entries;

    public AbilityData() {
        this(new HashMap<>());
    }

    private AbilityData(Map<String, Entry> entries) {
        this.entries = entries;
    }

    public boolean isUnlocked(Ability ability) {
        return entries.containsKey(ability.id());
    }

    public void unlock(Ability ability) {
        entries.putIfAbsent(ability.id(), new Entry(1, 0));
    }

    public int getLevel(Ability ability) {
        Entry e = entries.get(ability.id());
        return e == null ? 0 : e.level();
    }

    public int getXp(Ability ability) {
        Entry e = entries.get(ability.id());
        return e == null ? 0 : e.xp();
    }

    /** Suma XP. Devuelve true si la habilidad subió de nivel. */
    public boolean addXp(Ability ability, int amount) {
        Entry e = entries.get(ability.id());
        if (e == null || e.level() >= ability.maxLevel()) return false;

        int level = e.level();
        int xp = e.xp() + amount;
        boolean leveled = false;
        while (level < ability.maxLevel() && xp >= Ability.xpForNext(level)) {
            xp -= Ability.xpForNext(level);
            level++;
            leveled = true;
        }
        if (level >= ability.maxLevel()) xp = 0;
        entries.put(ability.id(), new Entry(level, xp));
        return leveled;
    }
}
