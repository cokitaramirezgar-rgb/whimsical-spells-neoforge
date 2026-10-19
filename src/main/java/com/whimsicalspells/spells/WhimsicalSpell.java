package com.whimsicalspells.spells;

import com.whimsicalspells.WhimsicalSpells;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import net.minecraft.resources.ResourceLocation;

/** Base para quitar el código repetido de cada hechizo. castTicks = 0 -> instantáneo; > 0 -> con conjuración. */
public abstract class WhimsicalSpell extends AbstractSpell {
    private final ResourceLocation spellId;
    private final DefaultConfig defaultConfig;
    private final CastType castType;
    private final int maxSpellLevel;

    protected WhimsicalSpell(String name, ResourceLocation school, SpellRarity rarity, int maxLevel,
                             int cooldownSeconds, int baseMana, int manaPerLevel, int castTicks,
                             int basePower, int powerPerLevel) {
        this.spellId = WhimsicalSpells.id(name);
        this.maxSpellLevel = maxLevel;
        this.defaultConfig = new DefaultConfig()
                .setMinRarity(rarity)
                .setSchoolResource(school)
                .setMaxLevel(maxLevel)
                .setCooldownSeconds(cooldownSeconds)
                .build();
        this.baseManaCost = baseMana;
        this.manaCostPerLevel = manaPerLevel;
        this.baseSpellPower = basePower;
        this.spellPowerPerLevel = powerPerLevel;
        this.castTime = castTicks;
        this.castType = castTicks > 0 ? CastType.LONG : CastType.INSTANT;
    }

    @Override
    public ResourceLocation getSpellResource() { return spellId; }

    @Override
    public DefaultConfig getDefaultConfig() { return defaultConfig; }

    @Override
    public CastType getCastType() { return castType; }

    /** Nivel del hechizo limitado a 1..max (por si un pergamino trae un nivel fuera de rango). */
    protected int clampLevel(int spellLevel) {
        return Math.max(1, Math.min(spellLevel, maxSpellLevel));
    }
}
