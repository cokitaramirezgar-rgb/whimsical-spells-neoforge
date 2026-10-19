package com.whimsicalspells.spells;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
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
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Dragon Slave: el gran hechizo de fuego de Slayers. Rayo rojo + explosión al final. No rompe bloques. */
public class DragonSlaveSpell extends WhimsicalSpell {
    private static final int MAX_LEVEL = 3;
    private static final double[] RANGE = {28.0, 34.0, 40.0};
    private static final double BLAST_RADIUS_BASE = 4.0;
    private static final double BLAST_RADIUS_PER_LEVEL = 1.0;
    private static final float BLAST_DAMAGE_FACTOR = 0.6f; // la explosión hace esta fracción del daño del rayo
    private static final float BURN_SECONDS = 6.0f;

    public DragonSlaveSpell() {
        // cooldown 60 s, maná 90 + 20/nivel, conjuración 4 s (el "canto"), poder 12 + 4/nivel
        super("dragon_slave", SchoolRegistry.FIRE_RESOURCE, SpellRarity.LEGENDARY, MAX_LEVEL,
                60, 90, 20, 80, 12, 4);
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (level instanceof ServerLevel server) {
            int l = clampLevel(spellLevel);
            float damage = getSpellPower(spellLevel, entity);
            double range = RANGE[l - 1];

            Vec3 start = entity.getEyePosition().add(0, -0.2, 0);
            Vec3 look = entity.getLookAngle();
            Vec3 end = start.add(look.scale(range));
            BlockHitResult block = server.clip(new ClipContext(start, end,
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));
            if (block.getType() != HitResult.Type.MISS) {
                end = block.getLocation();
            }

            Set<UUID> hit = new HashSet<>();

            // 1) El rayo: daña y prende fuego a todo lo que cruza
            AABB area = new AABB(start, end).inflate(2.0);
            for (LivingEntity target : server.getEntitiesOfClass(LivingEntity.class, area,
                    e -> e != entity && e.isAlive() && !entity.isAlliedTo(e))) {
                if (target.getBoundingBox().inflate(1.0).clip(start, end).isPresent()) {
                    target.hurt(server.damageSources().indirectMagic(entity, entity), damage);
                    target.igniteForSeconds(BURN_SECONDS);
                    hit.add(target.getUUID());
                }
            }

            // 2) La explosión final (sin romper bloques)
            double blastRadius = BLAST_RADIUS_BASE + BLAST_RADIUS_PER_LEVEL * (l - 1);
            AABB blastArea = new AABB(end, end).inflate(blastRadius);
            for (LivingEntity target : server.getEntitiesOfClass(LivingEntity.class, blastArea,
                    e -> e != entity && e.isAlive() && !entity.isAlliedTo(e) && !hit.contains(e.getUUID())
                            && e.distanceToSqr(end) <= blastRadius * blastRadius)) {
                target.hurt(server.damageSources().explosion(entity, entity), damage * BLAST_DAMAGE_FACTOR);
                target.igniteForSeconds(BURN_SECONDS);
            }

            // 3) Efectos visuales: haz rojo y bola de fuego
            DustParticleOptions red = new DustParticleOptions(new Vector3f(1.0f, 0.1f, 0.05f), 2.0f);
            double length = start.distanceTo(end);
            for (double d = 1.0; d <= length; d += 0.6) {
                Vec3 p = start.add(look.scale(d));
                server.sendParticles(red, p.x, p.y, p.z, 3, 0.25, 0.25, 0.25, 0.0);
                server.sendParticles(ParticleTypes.FLAME, p.x, p.y, p.z, 2, 0.3, 0.3, 0.3, 0.02);
            }
            server.sendParticles(ParticleTypes.EXPLOSION_EMITTER, end.x, end.y, end.z, 1, 0, 0, 0, 0);
            server.sendParticles(ParticleTypes.FLAME, end.x, end.y, end.z, 120, blastRadius * 0.4, blastRadius * 0.3, blastRadius * 0.4, 0.15);
            server.sendParticles(ParticleTypes.LAVA, end.x, end.y, end.z, 30, blastRadius * 0.3, 0.3, blastRadius * 0.3, 0.0);
            server.sendParticles(ParticleTypes.SMOKE, end.x, end.y, end.z, 60, blastRadius * 0.4, 0.4, blastRadius * 0.4, 0.05);

            server.playSound(null, entity.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.0f, 0.8f);
            server.playSound(null, net.minecraft.core.BlockPos.containing(end), SoundEvents.DRAGON_FIREBALL_EXPLODE,
                    SoundSource.PLAYERS, 2.0f, 0.7f);
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }
}
