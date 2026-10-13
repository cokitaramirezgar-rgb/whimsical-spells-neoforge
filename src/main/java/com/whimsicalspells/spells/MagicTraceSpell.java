package com.whimsicalspells.spells;

import com.mojang.datafixers.util.Pair;
import com.whimsicalspells.TraceManager;
import com.whimsicalspells.WhimsicalSpells;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class MagicTraceSpell extends AbstractSpell {
    private final ResourceLocation spellId = WhimsicalSpells.id("magic_trace");

    private static final int CAST_TIME_TICKS = 40; // 2 s de conjuración
    private static final int MAX_LEVEL = 3;
    private static final int[] RADIUS_CHUNKS = {40, 80, 120};

    private static final List<String> OVERWORLD = List.of(
            "stronghold",
            "village_plains", "village_desert", "village_savanna", "village_snowy", "village_taiga",
            "mansion");
    private static final List<String> NETHER = List.of("fortress");
    private static final List<String> END = List.of("end_city");
    /** Estructuras de otros mods (p. ej. torres de mago), formato "modid:nombre". Las que no existan se ignoran. */
    private static final List<String> EXTRA = List.of();

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.ENDER_RESOURCE)
            .setMaxLevel(MAX_LEVEL)
            .setCooldownSeconds(30)
            .build();

    public MagicTraceSpell() {
        this.baseManaCost = 30;
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 0;
        this.castTime = CAST_TIME_TICKS;
    }

    @Override
    public ResourceLocation getSpellResource() { return spellId; }

    @Override
    public DefaultConfig getDefaultConfig() { return defaultConfig; }

    @Override
    public CastType getCastType() { return CastType.LONG; }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (entity instanceof ServerPlayer player && level instanceof ServerLevel server) {
            int l = Math.max(1, Math.min(spellLevel, MAX_LEVEL));
            BlockPos target = findNearest(server, player.blockPosition(), RADIUS_CHUNKS[l - 1]);

            if (target == null) {
                player.displayClientMessage(Component.translatable("ui.whimsical_spells.trace_none"), true);
                server.playSound(null, player.blockPosition(), SoundEvents.FIRE_EXTINGUISH,
                        SoundSource.PLAYERS, 0.6f, 1.4f);
            } else {
                Vec3 origin = player.getEyePosition().add(0, -0.6, 0);
                Vec3 flat = new Vec3(target.getX() + 0.5 - origin.x, 0, target.getZ() + 0.5 - origin.z);
                if (flat.lengthSqr() < 16) {
                    player.displayClientMessage(Component.translatable("ui.whimsical_spells.trace_here"), true);
                } else {
                    TraceManager.start(player, origin, flat.normalize());
                    player.displayClientMessage(Component.translatable("ui.whimsical_spells.trace_found"), true);
                }
                server.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE,
                        SoundSource.PLAYERS, 1.0f, 1.0f);
            }
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    private static BlockPos findNearest(ServerLevel level, BlockPos origin, int radiusChunks) {
        Registry<Structure> registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        List<Holder<Structure>> holders = new ArrayList<>();
        for (String id : idsFor(level.dimension())) {
            registry.getHolder(ResourceKey.create(Registries.STRUCTURE, ResourceLocation.parse(id)))
                    .ifPresent(holders::add);
        }
        if (holders.isEmpty()) return null;

        Pair<BlockPos, Holder<Structure>> found = level.getChunkSource().getGenerator()
                .findNearestMapStructure(level, HolderSet.direct(holders), origin, radiusChunks, false);
        return found == null ? null : found.getFirst();
    }

    private static List<String> idsFor(ResourceKey<Level> dimension) {
        List<String> ids = new ArrayList<>();
        if (dimension == Level.OVERWORLD) ids.addAll(OVERWORLD);
        else if (dimension == Level.NETHER) ids.addAll(NETHER);
        else if (dimension == Level.END) ids.addAll(END);
        ids.addAll(EXTRA);
        return ids;
    }
}
