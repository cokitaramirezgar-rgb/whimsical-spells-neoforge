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

    // Pendiente: WARD_BARRIER
}
