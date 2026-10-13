package com.whimsicalspells.spells;

import com.whimsicalspells.WhimsicalSpells;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

public class RepairItemSpell extends AbstractSpell {
    private final ResourceLocation spellId = WhimsicalSpells.id("repair_item");

    // --- Ajustes de equilibrio ---
    private static final int CAST_TIME_TICKS = 60; // 3 s de conjuración (es muy útil)
    private static final int MAX_LEVEL = 5;
    private static final float HAND_MIN = 0.20f, HAND_MAX = 0.50f;
    private static final float ARMOR_MIN = 0.12f, ARMOR_MAX = 0.35f;
    private static final float FATIGUE_STEP = 0.25f;
    private static final float MIN_FACTOR = 0.25f;
    private static final long FATIGUE_RESET_TICKS = 12000;

    private static final String TAG_COUNT = "WhimsicalRepairCount";
    private static final String TAG_TIME = "WhimsicalRepairTime";

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.HOLY_RESOURCE)
            .setMaxLevel(MAX_LEVEL)
            .setCooldownSeconds(120)
            .build();

    public RepairItemSpell() {
        this.manaCostPerLevel = 0;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 0;
        this.castTime = CAST_TIME_TICKS;
        this.baseManaCost = 100;
    }

    @Override
    public ResourceLocation getSpellResource() { return spellId; }

    @Override
    public DefaultConfig getDefaultConfig() { return defaultConfig; }

    @Override
    public CastType getCastType() { return CastType.LONG; }

    private static float lerpLevel(float min, float max, int level) {
        int l = Math.max(1, Math.min(level, MAX_LEVEL));
        return min + (max - min) * (l - 1) / (MAX_LEVEL - 1);
    }

    public static float handPercent(int level) { return lerpLevel(HAND_MIN, HAND_MAX, level); }
    public static float armorPercent(int level) { return lerpLevel(ARMOR_MIN, ARMOR_MAX, level); }

    @Override
    public CastResult canBeCastedBy(int spellLevel, CastSource castSource, MagicData playerMagicData, Player player) {
        if (player.isShiftKeyDown()) {
            if (!hasDamagedArmor(player)) {
                return new CastResult(CastResult.Type.FAILURE,
                        Component.translatable("ui.whimsical_spells.repair_no_armor"));
            }
        } else if (findHandTarget(player).isEmpty()) {
            return new CastResult(CastResult.Type.FAILURE,
                    Component.translatable("ui.whimsical_spells.repair_no_item"));
        }
        return super.canBeCastedBy(spellLevel, castSource, playerMagicData, player);
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (entity instanceof Player player && level instanceof ServerLevel server) {
            long now = server.getGameTime();
            float shown = 0f;
            boolean armorMode = player.isShiftKeyDown();

            if (armorMode) {
                float base = armorPercent(spellLevel);
                for (ItemStack piece : player.getArmorSlots()) {
                    if (piece.isDamaged()) {
                        shown = Math.max(shown, repair(piece, base, now));
                    }
                }
            } else {
                ItemStack stack = findHandTarget(player);
                if (!stack.isEmpty()) {
                    shown = repair(stack, handPercent(spellLevel), now);
                }
            }

            if (shown > 0f) {
                server.sendParticles(ParticleTypes.ENCHANT,
                        player.getX(), player.getY() + 1.2, player.getZ(), 30, 0.5, 0.5, 0.5, 0.5);
                server.playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE,
                        SoundSource.PLAYERS, 1.0f, 1.2f);
                player.displayClientMessage(Component.translatable(
                        armorMode ? "ui.whimsical_spells.repair_armor_done" : "ui.whimsical_spells.repair_done",
                        Math.round(shown * 100)), true);
            }
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    /** Repara una pieza aplicando su fatiga. La cuenta se guarda en el componente de datos personalizados del objeto. */
    private static float repair(ItemStack stack, float basePercent, long now) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        int uses = tag.getInt(TAG_COUNT);
        if (now - tag.getLong(TAG_TIME) > FATIGUE_RESET_TICKS) {
            uses = 0;
        }
        float factor = Math.max(MIN_FACTOR, 1f - FATIGUE_STEP * uses);
        float percent = basePercent * factor;
        int heal = Math.max(1, Math.round(stack.getMaxDamage() * percent));
        stack.setDamageValue(Math.max(0, stack.getDamageValue() - heal));
        tag.putInt(TAG_COUNT, uses + 1);
        tag.putLong(TAG_TIME, now);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return percent;
    }

    private static boolean hasDamagedArmor(Player player) {
        for (ItemStack piece : player.getArmorSlots()) {
            if (piece.isDamaged()) return true;
        }
        return false;
    }

    private static ItemStack findHandTarget(Player player) {
        ItemStack main = player.getMainHandItem();
        if (main.isDamaged()) return main;
        ItemStack off = player.getOffhandItem();
        if (off.isDamaged()) return off;
        return ItemStack.EMPTY;
    }
}
