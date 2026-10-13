package com.whimsicalspells.spells;

import com.whimsicalspells.DetectionManager;
import com.whimsicalspells.WhimsicalSpells;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Meehkeit: el hechizo que detecta mimics. Aquí detecta cofres trampa. */
public class MeehkeitSpell extends AbstractSpell {
    private final ResourceLocation spellId = WhimsicalSpells.id("meehkeit");

    private static final int CAST_TIME_TICKS = 50; // 2.5 s de conjuración
    private static final int MAX_LEVEL = 3;
    /** Radio de detección en bloques según el nivel (1, 2, 3). */
    private static final int[] RADIUS = {16, 32, 48};
    private static final int MAX_MARKS = 40;

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.ENDER_RESOURCE)
            .setMaxLevel(MAX_LEVEL)
            .setCooldownSeconds(20)
            .build();

    public MeehkeitSpell() {
        this.baseManaCost = 20;
        this.manaCostPerLevel = 5;
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
            BlockPos origin = player.blockPosition();
            List<BlockPos> found = scan(server, origin, RADIUS[l - 1]);

            if (found.isEmpty()) {
                player.displayClientMessage(Component.translatable("ui.whimsical_spells.meehkeit_none"), true);
                server.playSound(null, origin, SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.6f, 1.4f);
            } else {
                BlockPos nearest = found.get(0);
                int distance = (int) Math.round(Math.sqrt(nearest.distSqr(origin)));
                player.displayClientMessage(Component.translatable("ui.whimsical_spells.meehkeit_found",
                        found.size(), distance, Component.translatable(directionKey(origin, nearest))), true);
                DetectionManager.start(player, found.size() > MAX_MARKS ? found.subList(0, MAX_MARKS) : found);
                server.playSound(null, origin, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.0f, 0.7f);
            }
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    /** Cofres trampa dentro del radio, de más cercano a más lejano. Solo mira chunks ya cargados. */
    private static List<BlockPos> scan(ServerLevel level, BlockPos origin, int radius) {
        List<BlockPos> result = new ArrayList<>();
        ChunkPos center = new ChunkPos(origin);
        int chunkRadius = (radius >> 4) + 1;
        double maxSqr = (double) radius * radius;

        for (int cx = center.x - chunkRadius; cx <= center.x + chunkRadius; cx++) {
            for (int cz = center.z - chunkRadius; cz <= center.z + chunkRadius; cz++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(cx, cz);
                if (chunk == null) continue;
                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    if (be.getType() != BlockEntityType.TRAPPED_CHEST) continue;
                    // Un cofre doble son dos bloques: cuenta solo una mitad
                    if (be.getBlockState().getValue(ChestBlock.TYPE) == ChestType.RIGHT) continue;
                    BlockPos pos = be.getBlockPos();
                    if (pos.distSqr(origin) <= maxSqr) {
                        result.add(pos.immutable());
                    }
                }
            }
        }
        result.sort(Comparator.comparingDouble(p -> p.distSqr(origin)));
        return result;
    }

    private static String directionKey(BlockPos from, BlockPos to) {
        int dx = to.getX() - from.getX();
        int dz = to.getZ() - from.getZ();
        if (Math.abs(dz) >= Math.abs(dx)) {
            return dz < 0 ? "ui.whimsical_spells.dir_north" : "ui.whimsical_spells.dir_south";
        }
        return dx > 0 ? "ui.whimsical_spells.dir_east" : "ui.whimsical_spells.dir_west";
    }
}
