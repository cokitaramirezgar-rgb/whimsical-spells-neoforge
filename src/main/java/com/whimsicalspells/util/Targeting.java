package com.whimsicalspells.util;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/** Utilidades para elegir objetivos: a quién miras, qué bloque miras y quiénes son tus aliados cercanos. */
public final class Targeting {
    private Targeting() {}

    public static Predicate<LivingEntity> nonAlly(LivingEntity caster) {
        return e -> e != caster && !caster.isAlliedTo(e);
    }

    public static Predicate<LivingEntity> ally(LivingEntity caster) {
        return e -> e != caster && caster.isAlliedTo(e);
    }

    /** Primera criatura que miras (antes de cualquier bloque) y que cumpla el filtro; null si no hay. */
    public static LivingEntity livingInSight(LivingEntity caster, double range, Predicate<LivingEntity> filter) {
        Level level = caster.level();
        Vec3 start = caster.getEyePosition();
        Vec3 look = caster.getLookAngle();
        Vec3 end = start.add(look.scale(range));

        BlockHitResult block = level.clip(new ClipContext(start, end,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
        double limitSqr = block.getType() == HitResult.Type.MISS ? range * range
                : block.getLocation().distanceToSqr(start);

        AABB box = caster.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(caster, start, end, box,
                e -> e instanceof LivingEntity le && le.isAlive() && !le.isSpectator()
                        && le.isPickable() && filter.test(le),
                limitSqr);
        return hit == null ? null : (LivingEntity) hit.getEntity();
    }

    /** Bloque que miras (ignora fluidos). Comprueba getType() != MISS. */
    public static BlockHitResult blockInSight(LivingEntity caster, double range) {
        Vec3 start = caster.getEyePosition();
        Vec3 end = start.add(caster.getLookAngle().scale(range));
        return caster.level().clip(new ClipContext(start, end,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
    }

    /** Tú más tus aliados dentro del radio (equipo o mascotas). */
    public static List<LivingEntity> withAllies(LivingEntity caster, double radius) {
        List<LivingEntity> result = new ArrayList<>();
        result.add(caster);
        for (LivingEntity e : caster.level().getEntitiesOfClass(LivingEntity.class,
                caster.getBoundingBox().inflate(radius),
                e -> e.isAlive() && e != caster && caster.isAlliedTo(e) && e.distanceToSqr(caster) <= radius * radius)) {
            result.add(e);
        }
        return result;
    }

    public static void tell(Entity entity, String key, Object... args) {
        if (entity instanceof ServerPlayer player) {
            player.displayClientMessage(Component.translatable(key, args), true);
        }
    }
}
