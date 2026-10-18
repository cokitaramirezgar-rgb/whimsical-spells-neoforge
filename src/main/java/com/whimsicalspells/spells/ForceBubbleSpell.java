package com.whimsicalspells.spells;

import com.whimsicalspells.TaskManager;
import com.whimsicalspells.WhimsicalSpells;
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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Burbuja de fuerza: una cúpula fija que protege a los aliados de dentro
 * (resistencia + absorción) y detiene los proyectiles enemigos. El cuerpo a cuerpo la atraviesa.
 */
public class ForceBubbleSpell extends AbstractSpell {
    private final ResourceLocation spellId = WhimsicalSpells.id("force_bubble");

    private static final int CAST_TIME_TICKS = 40; // 2 s de conjuración
    private static final int MAX_LEVEL = 3;
    private static final double[] RADIUS = {4.0, 5.0, 6.0};
    private static final int[] SECONDS = {8, 12, 16};
    /** true = todos los jugadores cuentan como aliados (cooperativo). false = solo tú, tus mascotas y tu equipo. */
    private static final boolean ALL_PLAYERS_ARE_ALLIES = true;

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.HOLY_RESOURCE)
            .setMaxLevel(MAX_LEVEL)
            .setCooldownSeconds(45)
            .build();

    public ForceBubbleSpell() {
        this.baseManaCost = 60;
        this.manaCostPerLevel = 15;
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
        if (level instanceof ServerLevel server) {
            int l = Math.max(1, Math.min(spellLevel, MAX_LEVEL));
            server.playSound(null, entity.blockPosition(), SoundEvents.BEACON_ACTIVATE,
                    SoundSource.PLAYERS, 1.0f, 1.3f);
            runBubble(server.dimension(), entity.getUUID(), entity.position(), RADIUS[l - 1], SECONDS[l - 1] * 20);
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    private static void runBubble(ResourceKey<Level> dimension, UUID casterId, Vec3 center, double radius, int ticks) {
        int[] age = {0};
        Set<UUID> absorbed = new HashSet<>();

        TaskManager.add(server -> {
            ServerLevel level = server.getLevel(dimension);
            if (level == null) return true;
            if (age[0] >= ticks) {
                level.playSound(null, net.minecraft.core.BlockPos.containing(center),
                        SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.0f, 1.3f);
                return true;
            }

            Entity casterEntity = level.getEntity(casterId);
            LivingEntity caster = casterEntity instanceof LivingEntity le ? le : null;
            RandomSource random = level.getRandom();

            // Cúpula (media esfera) de partículas
            for (int i = 0; i < 14; i++) {
                double y = random.nextDouble();                 // 0..1: solo la mitad de arriba
                double theta = random.nextDouble() * Math.PI * 2;
                double q = Math.sqrt(1 - y * y);
                level.sendParticles(ParticleTypes.END_ROD,
                        center.x + Math.cos(theta) * q * radius,
                        center.y + y * radius,
                        center.z + Math.sin(theta) * q * radius,
                        1, 0, 0, 0, 0);
            }

            AABB box = new AABB(center, center).inflate(radius + 1);

            // Detiene los proyectiles que no vienen de un aliado
            if (age[0] % 2 == 0) {
                for (Projectile projectile : level.getEntitiesOfClass(Projectile.class, box)) {
                    if (projectile.position().distanceToSqr(center) > radius * radius) continue;
                    Entity owner = projectile.getOwner();
                    if (owner instanceof LivingEntity o && isAlly(caster, o)) continue;
                    level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                            projectile.getX(), projectile.getY(), projectile.getZ(), 10, 0.2, 0.2, 0.2, 0.3);
                    projectile.discard();
                }
            }

            // Cada segundo: resistencia a los aliados de dentro; absorción una sola vez por aliado
            if (age[0] % 20 == 0) {
                int remaining = ticks - age[0];
                for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
                    if (e.position().distanceToSqr(center) > radius * radius || !isAlly(caster, e)) continue;
                    e.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 0, false, false, true));
                    if (absorbed.add(e.getUUID())) {
                        e.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, remaining, 0, false, false, true));
                    }
                }
            }
            age[0]++;
            return false;
        });
    }

    private static boolean isAlly(LivingEntity caster, LivingEntity other) {
        if (caster == null) return other instanceof Player && ALL_PLAYERS_ARE_ALLIES;
        if (other == caster || caster.isAlliedTo(other)) return true;
        if (other instanceof OwnableEntity pet && caster.getUUID().equals(pet.getOwnerUUID())) return true;
        return ALL_PLAYERS_ARE_ALLIES && other instanceof Player;
    }
}
