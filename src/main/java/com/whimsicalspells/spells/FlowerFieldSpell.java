package com.whimsicalspells.spells;

import com.whimsicalspells.WhimsicalSpells;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;

public class FlowerFieldSpell extends AbstractSpell {
    private final ResourceLocation spellId = WhimsicalSpells.id("flower_field");

    private static final int CAST_TIME_TICKS = 40; // 2 s de conjuración

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(SchoolRegistry.NATURE_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(20)
            .build();

    private static final Block[] COMMON = {
            Blocks.POPPY, Blocks.DANDELION, Blocks.AZURE_BLUET, Blocks.OXEYE_DAISY,
            Blocks.CORNFLOWER, Blocks.RED_TULIP, Blocks.ORANGE_TULIP, Blocks.WHITE_TULIP,
            Blocks.PINK_TULIP, Blocks.ALLIUM, Blocks.BLUE_ORCHID, Blocks.LILY_OF_THE_VALLEY
    };
    private static final float SUNFLOWER_CHANCE = 0.02f;
    private static final float DENSITY = 0.55f;

    public FlowerFieldSpell() {
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 1;
        this.castTime = CAST_TIME_TICKS;
        this.baseManaCost = 20;
    }

    @Override
    public ResourceLocation getSpellResource() { return spellId; }

    @Override
    public DefaultConfig getDefaultConfig() { return defaultConfig; }

    @Override
    public CastType getCastType() { return CastType.LONG; }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (level instanceof ServerLevel server) {
            int radius = 3 + spellLevel * 2;
            BlockPos center = entity.blockPosition();
            RandomSource random = level.getRandom();

            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dz * dz > radius * radius) continue;
                    if (random.nextFloat() > DENSITY) continue;

                    BlockPos spot = findSpot(server, center.offset(dx, 0, dz), center.getY());
                    if (spot == null) continue;

                    if (random.nextFloat() < SUNFLOWER_CHANCE && server.isEmptyBlock(spot.above())) {
                        DoublePlantBlock.placeAt(server, Blocks.SUNFLOWER.defaultBlockState(), spot, 2);
                    } else {
                        Block flower = COMMON[random.nextInt(COMMON.length)];
                        server.setBlock(spot, flower.defaultBlockState(), 2);
                    }
                    server.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                            spot.getX() + 0.5, spot.getY() + 0.5, spot.getZ() + 0.5, 1, 0.2, 0.2, 0.2, 0);
                }
            }
            server.playSound(null, center, SoundEvents.GRASS_PLACE, SoundSource.PLAYERS, 1.0f, 1.0f);
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    private static BlockPos findSpot(ServerLevel level, BlockPos column, int baseY) {
        for (int y = baseY + 3; y >= baseY - 3; y--) {
            BlockPos pos = new BlockPos(column.getX(), y, column.getZ());
            BlockState below = level.getBlockState(pos.below());
            if (level.isEmptyBlock(pos) && below.is(BlockTags.DIRT)) {
                return pos;
            }
        }
        return null;
    }
}
