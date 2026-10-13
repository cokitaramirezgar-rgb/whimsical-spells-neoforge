package com.whimsicalspells;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Animación y efecto de Reamstroha: el agua se reúne en una esfera sobre el objetivo,
 * y luego cae en cascada empapándolo.
 */
@EventBusSubscriber(modid = WhimsicalSpells.MODID)
public class WaterSphereManager {
    private static final int CHARGE_TICKS = 40;  // la esfera se forma
    private static final int FALL_TICKS = 24;    // la cascada cae
    private static final int IMPACT_TICK = 5;    // tick de la cascada en el que golpea
    private static final double HOVER = 5.0;     // altura de la esfera sobre el objetivo

    private static final List<Sphere> ACTIVE = new ArrayList<>();

    private static class Sphere {
        final ResourceKey<Level> dimension;
        final UUID caster;
        final UUID target; // null = punto fijo
        Vec3 position;
        final double radius;
        final float damage;
        final boolean defensive;
        final int spellLevel;
        final boolean rain;
        int age = 0;

        Sphere(ResourceKey<Level> dimension, UUID caster, UUID target, Vec3 position, double radius,
               float damage, boolean defensive, int spellLevel, boolean rain) {
            this.dimension = dimension;
            this.caster = caster;
            this.target = target;
            this.position = position;
            this.radius = radius;
            this.damage = damage;
            this.defensive = defensive;
            this.spellLevel = spellLevel;
            this.rain = rain;
        }
    }

    public static void start(ServerLevel level, UUID caster, UUID target, Vec3 position, double radius,
                             float damage, boolean defensive, int spellLevel, boolean rain) {
        ACTIVE.add(new Sphere(level.dimension(), caster, target, position, radius, damage, defensive, spellLevel, rain));
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (ACTIVE.isEmpty()) return;
        MinecraftServer server = event.getServer();

        Iterator<Sphere> it = ACTIVE.iterator();
        while (it.hasNext()) {
            Sphere s = it.next();
            ServerLevel level = server.getLevel(s.dimension);
            if (level == null || s.age >= CHARGE_TICKS + FALL_TICKS) {
                it.remove();
                continue;
            }

            // Si el objetivo es una entidad, la esfera la sigue
            if (s.target != null) {
                Entity tracked = level.getEntity(s.target);
                if (tracked != null && tracked.isAlive()) {
                    s.position = tracked.position();
                }
            }
            Vec3 base = s.position;
            Vec3 center = base.add(0, HOVER, 0);

            if (s.age < CHARGE_TICKS) {
                charge(level, s, center);
            } else {
                int t = s.age - CHARGE_TICKS;
                cascade(level, s, base);
                if (t == IMPACT_TICK) {
                    impact(level, s, base);
                }
            }
            s.age++;
        }
    }

    /** Fase 1: el agua circundante se recoge y forma una esfera que va creciendo. */
    private static void charge(ServerLevel level, Sphere s, Vec3 center) {
        double r = s.radius * (0.3 + 0.7 * s.age / CHARGE_TICKS);
        var random = level.getRandom();

        if (s.age == 0) {
            level.playSound(null, net.minecraft.core.BlockPos.containing(center),
                    SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 1.0f, 0.6f);
        }
        // Piel de la esfera
        for (int i = 0; i < 18; i++) {
            Vec3 dir = randomDirection(random.nextDouble(), random.nextDouble());
            Vec3 p = center.add(dir.scale(r));
            level.sendParticles(ParticleTypes.DOLPHIN, p.x, p.y, p.z, 1, 0, 0, 0, 0);
        }
        // Hilos de agua que llegan desde fuera hacia la esfera
        for (int i = 0; i < 4; i++) {
            Vec3 dir = randomDirection(random.nextDouble(), random.nextDouble());
            Vec3 from = center.add(dir.scale(r * (1.8 + random.nextDouble())));
            Vec3 toward = center.subtract(from).normalize().scale(0.35);
            level.sendParticles(ParticleTypes.DOLPHIN, from.x, from.y, from.z, 0, toward.x, toward.y, toward.z, 1.0);
        }
    }

    /** Fase 2: la esfera se deshace y cae en cascada. */
    private static void cascade(ServerLevel level, Sphere s, Vec3 base) {
        var random = level.getRandom();
        for (int i = 0; i < 45; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double rr = Math.sqrt(random.nextDouble()) * s.radius;
            double y = random.nextDouble() * HOVER;
            level.sendParticles(ParticleTypes.FALLING_WATER,
                    base.x + Math.cos(angle) * rr, base.y + y, base.z + Math.sin(angle) * rr,
                    1, 0, 0, 0, 0);
        }
    }

    /** El golpe: daño y empapado al objetivo (o resistencia y fuego apagado si es defensivo). */
    private static void impact(ServerLevel level, Sphere s, Vec3 base) {
        Entity casterEntity = level.getEntity(s.caster);
        LivingEntity caster = casterEntity instanceof LivingEntity le ? le : null;

        double r = s.radius;
        AABB box = new AABB(base.x - r, base.y - 1, base.z - r, base.x + r, base.y + 3, base.z + r);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            double dx = e.getX() - base.x, dz = e.getZ() - base.z;
            if (dx * dx + dz * dz > r * r) continue;

            boolean friendly = e == caster || (caster != null && caster.isAlliedTo(e));
            if (s.defensive) {
                if (friendly) {
                    e.clearFire();
                    e.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, (6 + 2 * s.spellLevel) * 20, 0));
                }
            } else if (!friendly) {
                DamageSource source = caster != null
                        ? level.damageSources().indirectMagic(caster, caster)
                        : level.damageSources().magic();
                e.hurt(source, s.damage);
                e.clearFire();
                int slowTicks = (4 + s.spellLevel + (s.rain ? 2 : 0)) * 20;
                e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, slowTicks, 1));
                e.setDeltaMovement(e.getDeltaMovement().multiply(0.3, 0, 0.3).add(0, -0.6, 0));
                e.hurtMarked = true;
            }
        }

        level.sendParticles(ParticleTypes.SPLASH, base.x, base.y + 0.2, base.z, 80, r * 0.5, 0.2, r * 0.5, 0.3);
        level.playSound(null, net.minecraft.core.BlockPos.containing(base),
                SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.PLAYERS, 1.2f, 0.8f);
    }

    private static Vec3 randomDirection(double a, double b) {
        double u = a * 2 - 1;
        double theta = b * Math.PI * 2;
        double q = Math.sqrt(1 - u * u);
        return new Vec3(q * Math.cos(theta), u, q * Math.sin(theta));
    }
}
