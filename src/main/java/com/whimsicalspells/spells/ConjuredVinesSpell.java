package com.whimsicalspells.spells;

import com.whimsicalspells.TaskManager;
import com.whimsicalspells.WhimsicalSpells;
import com.whimsicalspells.util.TargetUtil;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

/** Enredaderas conjuradas: brotan enredaderas que inmovilizan y dañan a los enemigos de una zona. */
public class ConjuredVinesSpell extends AbstractSpell {
    private final ResourceLocation spellId = WhimsicalSpells.id("conjured_vines");

    private static final int CAST_TIME_TICKS = 20; // 1 s de conjuración
    private static final int MAX_LEVEL = 3;
    private static final double RANGE = 20.0;
    private static final double RADIUS = 2.0;
    private static final int[] SECONDS = {3, 4, 5};
    private static final float DAMAGE_FACTOR = 0.5f; // daño por segundo = poder del hechizo * este factor

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.NATURE_RESOURCE)
            .setMaxLevel(MAX_LEVEL)
            .setCooldownSeconds(15)
            .build();

    public ConjuredVinesSpell() {
        this.baseManaCost = 30;
        this.manaCostPerLevel = 8;
        this.baseSpellPower = 3;
        this.spellPowerPerLevel = 1;
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
        if (level instanceof ServerLevel server) {
            int l = Math.max(1, Math.min(spellLevel, MAX_LEVEL));
            LivingEntity target = TargetUtil.pickEntity(entity, RANGE);
            Vec3 center = target != null ? target.position() : TargetUtil.pickPoint(entity, RANGE);
            float damagePerSecond = getSpellPower(spellLevel, entity) * DAMAGE_FACTOR;

            server.playSound(null, net.minecraft.core.BlockPos.containing(center),
                    SoundEvents.GRASS_BREAK, SoundSource.PLAYERS, 1.2f, 0.6f);
            runVines(server.dimension(), entity.getUUID(), center, SECONDS[l - 1] * 20, damagePerSecond);
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    private static void runVines(ResourceKey<Level> dimension, UUID casterId, Vec3 center, int ticks, float damagePerSecond) {
        int[] age = {0};
        TaskManager.add(server -> {
            ServerLevel level = server.getLevel(dimension);
            if (level == null || age[0] >= ticks) return true;

            Entity casterEntity = level.getEntity(casterId);
            LivingEntity caster = casterEntity instanceof LivingEntity le ? le : null;
            RandomSource random = level.getRandom();

            // Las enredaderas, como columnas de partículas verdes que crecen del suelo
            for (int i = 0; i < 6; i++) {
                double angle = random.nextDouble() * Math.PI * 2;
                double r = RADIUS * (0.3 + 0.7 * random.nextDouble());
                double growth = Math.min(2.2, 0.3 + age[0] * 0.15);
                double y = random.nextDouble() * growth;
                level.sendParticles(ParticleTypes.COMPOSTER,
                        center.x + Math.cos(angle) * r, center.y + y, center.z + Math.sin(angle) * r,
                        1, 0.05, 0.05, 0.05, 0);
            }

            // Cada medio segundo: inmoviliza. Cada segundo: daño.
            if (age[0] % 10 == 0) {
                List<LivingEntity> victims = affected(level, center, caster);
                for (LivingEntity e : victims) {
                    e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 6, false, true));
                    if (age[0] % 20 == 0) {
                        DamageSource source = caster != null
                                ? level.damageSources().indirectMagic(caster, caster)
                                : level.damageSources().magic();
                        e.hurt(source, damagePerSecond);
                        level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                                e.getX(), e.getY(0.5), e.getZ(), 6, 0.3, 0.4, 0.3, 0);
                    }
                }
            }
            age[0]++;
            return false;
        });
    }

    /** Enemigos dentro del círculo que están en el suelo (los voladores no quedan atrapados). */
    private static List<LivingEntity> affected(ServerLevel level, Vec3 center, LivingEntity caster) {
        AABB box = new AABB(center.x - RADIUS, center.y - 1, center.z - RADIUS,
                center.x + RADIUS, center.y + 3, center.z + RADIUS);
        return level.getEntitiesOfClass(LivingEntity.class, box, e -> {
            if (!e.isAlive() || e == caster || e.isSpectator() || !e.onGround()) return false;
            if (caster != null && caster.isAlliedTo(e)) return false;
            double dx = e.getX() - center.x, dz = e.getZ() - center.z;
            return dx * dx + dz * dz <= RADIUS * RADIUS;
        });
    }
}
