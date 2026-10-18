package com.whimsicalspells.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class TargetUtil {
    private TargetUtil() {}

    /** La criatura (no aliada) a la que apunta el lanzador, o null si no hay ninguna al alcance. */
    public static LivingEntity pickEntity(LivingEntity caster, double range) {
        Vec3 start = caster.getEyePosition();
        Vec3 look = caster.getLookAngle();
        Vec3 end = start.add(look.scale(range));

        BlockHitResult block = caster.level().clip(new ClipContext(start, end,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
        double limitSqr = block.getType() == HitResult.Type.MISS ? range * range
                : block.getLocation().distanceToSqr(start);

        AABB box = caster.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(caster, start, end, box,
                e -> e instanceof LivingEntity && e.isAlive() && !e.isSpectator()
                        && e.isPickable() && !caster.isAlliedTo(e),
                limitSqr);
        return hit == null ? null : (LivingEntity) hit.getEntity();
    }

    /** El punto al que apunta el lanzador: el bloque que golpea o el final del alcance. */
    public static Vec3 pickPoint(LivingEntity caster, double range) {
        Vec3 start = caster.getEyePosition();
        Vec3 end = start.add(caster.getLookAngle().scale(range));
        BlockHitResult block = caster.level().clip(new ClipContext(start, end,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
        return block.getType() == HitResult.Type.MISS ? end : block.getLocation();
    }
}
