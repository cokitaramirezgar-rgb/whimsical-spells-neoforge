package com.whimsicalspells;

import com.whimsicalspells.spells.*;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSpells {
    public static final DeferredRegister<AbstractSpell> SPELLS =
            DeferredRegister.create(SpellRegistry.SPELL_REGISTRY_KEY, WhimsicalSpells.MODID);

    public static final DeferredHolder<AbstractSpell, AbstractSpell> FLOWER_FIELD =
            SPELLS.register("flower_field", FlowerFieldSpell::new);

    public static final DeferredHolder<AbstractSpell, AbstractSpell> REPAIR_ITEM =
            SPELLS.register("repair_item", RepairItemSpell::new);

    public static final DeferredHolder<AbstractSpell, AbstractSpell> MAGIC_TRACE =
            SPELLS.register("magic_trace", MagicTraceSpell::new);

    public static final DeferredHolder<AbstractSpell, AbstractSpell> JUBELADE =
            SPELLS.register("jubelade", JubeladeSpell::new);

    public static final DeferredHolder<AbstractSpell, AbstractSpell> MEDROZOBALT =
            SPELLS.register("medrozobalt", MedrozobaltSpell::new);

    public static final DeferredHolder<AbstractSpell, AbstractSpell> MANA_SUPPRESSION =
            SPELLS.register("mana_suppression", ManaSuppressionSpell::new);

    public static final DeferredHolder<AbstractSpell, AbstractSpell> REAMSTROHA =
            SPELLS.register("reamstroha", ReamstrohaSpell::new);

    public static final DeferredHolder<AbstractSpell, AbstractSpell> MEEHKEIT =
            SPELLS.register("meehkeit", MeehkeitSpell::new);

    public static final DeferredHolder<AbstractSpell, AbstractSpell> REELSEIDEN =
            SPELLS.register("reelseiden", ReelseidenSpell::new);

    public static final DeferredHolder<AbstractSpell, AbstractSpell> DRAGON_SLAVE =
            SPELLS.register("dragon_slave", DragonSlaveSpell::new);

    public static final DeferredHolder<AbstractSpell, AbstractSpell> RA_TILT =
            SPELLS.register("ra_tilt", RaTiltSpell::new);

    public static final DeferredHolder<AbstractSpell, AbstractSpell> SHADOW_SNAP =
            SPELLS.register("shadow_snap", ShadowSnapSpell::new);

    public static final DeferredHolder<AbstractSpell, AbstractSpell> SLEEPING =
            SPELLS.register("sleeping", SleepingSpell::new);

    public static final DeferredHolder<AbstractSpell, AbstractSpell> AQUA_CREATE =
            SPELLS.register("aqua_create", AquaCreateSpell::new);

    public static final DeferredHolder<AbstractSpell, AbstractSpell> AQUA_BREATHE =
            SPELLS.register("aqua_breathe", AquaBreatheSpell::new);

    public static final DeferredHolder<AbstractSpell, AbstractSpell> LEVITATION =
            SPELLS.register("levitation", LevitationSpell::new);

    public static final DeferredHolder<AbstractSpell, AbstractSpell> LIGHTING =
            SPELLS.register("lighting", LightingSpell::new);

    public static final DeferredHolder<AbstractSpell, AbstractSpell> FIREPROOF_TECHNIQUE =
            SPELLS.register("fireproof_technique", FireproofTechniqueSpell::new);

    public static final DeferredHolder<AbstractSpell, AbstractSpell> WARMTH_SPELL =
            SPELLS.register("warmth_spell", WarmthSpell::new);

    public static final DeferredHolder<AbstractSpell, AbstractSpell> COLD_SPELL =
            SPELLS.register("cold_spell", ColdSpell::new);

    public static final DeferredHolder<AbstractSpell, AbstractSpell> DISENCHANTING_WORDS =
            SPELLS.register("disenchanting_words", DisenchantingWordsSpell::new);

    public static final DeferredHolder<AbstractSpell, AbstractSpell> BREAKING_A_SPELL =
            SPELLS.register("breaking_a_spell", BreakingASpellSpell::new);

    public static final DeferredHolder<AbstractSpell, AbstractSpell> BOOST =
            SPELLS.register("boost", BoostSpell::new);

    public static final DeferredHolder<AbstractSpell, AbstractSpell> STRENGTHENING =
            SPELLS.register("strengthening", StrengtheningSpell::new);

    // Pendiente: WARD_BARRIER
}
