package com.whimsicalspells.spells;

import com.whimsicalspells.TaskManager;
import com.whimsicalspells.WhimsicalSpells;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.Tags;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Redirige a mis enemigos: los mobs hostiles de la zona se atacan entre sí. */
public class RedirectEnemiesSpell extends AbstractSpell {
    private final ResourceLocation spellId = WhimsicalSpells.id("redirect_enemies");

    private static final int CAST_TIME_TICKS = 40; // 2 s de conjuración
    private static final int MAX_LEVEL = 3;
    private static final double[] RADIUS = {8.0, 10.0, 12.0};
    private static final int[] SECONDS = {6, 9, 12};
    private static final int RETARGET_EVERY_TICKS = 10;

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.ENDER_RESOURCE)
            .setMaxLevel(MAX_LEVEL)
            .setCooldownSeconds(30)
            .build();

    public RedirectEnemiesSpell() {
        this.baseManaCost = 40;
        this.manaCostPerLevel = 10;
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
            double radius = RADIUS[l - 1];

            // Mobs hostiles de la zona (sin jefes, sin aliados, sin mascotas)
            List<Mob> mobs = server.getEntitiesOfClass(Mob.class, entity.getBoundingBox().inflate(radius),
                    m -> m instanceof Enemy && m.isAlive()
                            && !m.getType().is(Tags.EntityTypes.BOSSES)
                            && !entity.isAlliedTo(m)
                            && m.distanceToSqr(entity) <= radius * radius);

            if (mobs.size() < 2) {
                if (entity instanceof ServerPlayer player) {
                    player.displayClientMessage(Component.translatable("ui.whimsical_spells.redirect_few"), true);
                }
            } else {
                List<UUID> ids = new ArrayList<>();
                for (Mob mob : mobs) {
                    ids.add(mob.getUUID());
                    server.sendParticles(ParticleTypes.ANGRY_VILLAGER,
                            mob.getX(), mob.getEyeY() + 0.4, mob.getZ(), 5, 0.3, 0.2, 0.3, 0);
                }
                server.playSound(null, entity.blockPosition(), SoundEvents.EVOKER_CAST_SPELL,
                        SoundSource.PLAYERS, 1.0f, 0.8f);
                runRedirect(server.dimension(), ids, SECONDS[l - 1] * 20);
            }
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    private static void runRedirect(ResourceKey<Level> dimension, List<UUID> ids, int ticks) {
        int[] age = {0};
        TaskManager.add(server -> {
            ServerLevel level = server.getLevel(dimension);
            if (level == null || age[0] >= ticks) return true;

            if (age[0] % RETARGET_EVERY_TICKS == 0) {
                List<Mob> alive = new ArrayList<>();
                for (UUID id : ids) {
                    Entity e = level.getEntity(id);
                    if (e instanceof Mob mob && mob.isAlive()) alive.add(mob);
                }
                if (alive.size() < 2) return true;

                for (Mob mob : alive) {
                    Mob nearest = null;
                    double best = Double.MAX_VALUE;
                    for (Mob other : alive) {
                        if (other == mob) continue;
                        double d = mob.distanceToSqr(other);
                        if (d < best) { best = d; nearest = other; }
                    }
                    mob.setTarget(nearest);
                }
            }
            age[0]++;
            return false;
        });
    }
}
