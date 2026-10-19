package com.whimsicalspells.spells;

import com.whimsicalspells.TempBlockManager;
import com.whimsicalspells.util.Targeting;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/** Lighting: una esfera de luz temporal que ilumina la zona donde miras (bloque de luz invisible + destellos). */
public class LightingSpell extends WhimsicalSpell {
    private static final int MAX_LEVEL = 3;
    private static final double RANGE = 24.0;
    private static final int[] DURATION_SECONDS = {60, 120, 240};

    public LightingSpell() {
        // instantáneo, cooldown 3 s, maná 10
        super("lighting", SchoolRegistry.HOLY_RESOURCE, SpellRarity.COMMON, MAX_LEVEL,
                3, 10, 0, 0, 1, 0);
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (level instanceof ServerLevel server) {
            BlockHitResult hit = Targeting.blockInSight(entity, RANGE);
            BlockPos pos = hit.getType() != HitResult.Type.MISS
                    ? hit.getBlockPos().relative(hit.getDirection())
                    : BlockPos.containing(entity.getEyePosition().add(entity.getLookAngle().scale(5.0)));

            if (!server.isEmptyBlock(pos)) {
                Targeting.tell(entity, "ui.whimsical_spells.no_space");
            } else {
                server.setBlock(pos, Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, 15), 3);
                TempBlockManager.add(server, pos, Blocks.LIGHT,
                        DURATION_SECONDS[clampLevel(spellLevel) - 1] * 20, ParticleTypes.END_ROD);
                server.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        20, 0.3, 0.3, 0.3, 0.05);
                server.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.8f, 1.8f);
            }
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }
}
