package com.whimsicalspells.spells;

import com.whimsicalspells.WhimsicalSpells;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Jubelade: el hechizo que convierte pétalos de flores en acero. */
public class JubeladeSpell extends AbstractSpell {
    private final ResourceLocation spellId = WhimsicalSpells.id("jubelade");

    private static final int MAX_LEVEL = 5;
    private static final double RANGE_BASE = 12.0;
    private static final double RANGE_PER_LEVEL = 3.0;

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.NATURE_RESOURCE)
            .setMaxLevel(MAX_LEVEL)
            .setCooldownSeconds(6)
            .build();

    public JubeladeSpell() {
        this.baseManaCost = 25;
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 4;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
    }

    @Override
    public ResourceLocation getSpellResource() { return spellId; }

    @Override
    public DefaultConfig getDefaultConfig() { return defaultConfig; }

    @Override
    public CastType getCastType() { return CastType.INSTANT; }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (level instanceof ServerLevel server) {
            float damage = getSpellPower(spellLevel, entity);
            double range = RANGE_BASE + RANGE_PER_LEVEL * (spellLevel - 1);

            Vec3 start = entity.getEyePosition().add(0, -0.2, 0);
            Vec3 look = entity.getLookAngle();
            Vec3 end = start.add(look.scale(range));

            BlockHitResult block = server.clip(new ClipContext(start, end,
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));
            if (block.getType() != HitResult.Type.MISS) {
                end = block.getLocation();
            }

            AABB area = new AABB(start, end).inflate(1.5);
            for (LivingEntity target : server.getEntitiesOfClass(LivingEntity.class, area,
                    e -> e != entity && e.isAlive() && !entity.isAlliedTo(e))) {
                if (target.getBoundingBox().inflate(0.5).clip(start, end).isPresent()) {
                    target.hurt(server.damageSources().indirectMagic(entity, entity), damage);
                    server.sendParticles(ParticleTypes.CRIT,
                            target.getX(), target.getY(0.5), target.getZ(), 8, 0.3, 0.3, 0.3, 0.2);
                }
            }

            double length = start.distanceTo(end);
            int i = 0;
            for (double d = 1.0; d <= length; d += 0.5, i++) {
                Vec3 p = start.add(look.scale(d));
                server.sendParticles(ParticleTypes.CHERRY_LEAVES, p.x, p.y, p.z, 2, 0.15, 0.15, 0.15, 0.02);
                if (i % 3 == 0) {
                    server.sendParticles(ParticleTypes.CRIT, p.x, p.y, p.z, 1, 0.05, 0.05, 0.05, 0.0);
                }
            }
            server.playSound(null, entity.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP,
                    SoundSource.PLAYERS, 1.0f, 1.4f);
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }
}
