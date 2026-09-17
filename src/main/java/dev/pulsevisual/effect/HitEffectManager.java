package dev.pulsevisual.effect;

import dev.pulsevisual.config.ConfigManager;
import dev.pulsevisual.config.VisualConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public final class HitEffectManager {
    private static final Random RANDOM = new Random();
    private static long lastHitMillis;
    private static boolean lastCritical;

    private HitEffectManager() { }

    public static void onAttack(Entity target, boolean critical) {
        Minecraft client = Minecraft.getInstance();
        if (!(target instanceof LivingEntity) || client.level == null) return;
        VisualConfig config = ConfigManager.get();
        lastHitMillis = System.currentTimeMillis();
        lastCritical = critical;

        if (critical ? !config.criticalHitParticles : !config.normalHitParticles) return;
        int count = Math.round(config.particleCount * config.intensity);
        var settings=config.hit;
        var particle=switch(settings.particle){
            case "Spark"->ParticleTypes.ELECTRIC_SPARK;
            case "Crit"->ParticleTypes.CRIT;
            case "End Rod"->ParticleTypes.END_ROD;
            case "Heart"->ParticleTypes.HEART;
            case "Flame"->ParticleTypes.FLAME;
            default->critical?ParticleTypes.END_ROD:ParticleTypes.WAX_ON;
        };
        Vec3 center = target.position().add(0, target.getBbHeight() * settings.height, 0);
        for (int i = 0; i < count; i++) {
            double angle = (settings.shape.equals("Ring") ? i/(double)Math.max(1,count) : RANDOM.nextDouble()) * Math.PI * 2;
            double radius = (settings.shape.equals("Ring")?.4:.12 + RANDOM.nextDouble() * .32)*settings.spread;
            double px = center.x + Math.cos(angle) * radius;
            double py = center.y + (settings.shape.equals("Ring")?0:(RANDOM.nextDouble() - 0.5) * 0.7*settings.spread);
            double pz = center.z + Math.sin(angle) * radius;
            double speed = (critical ? 0.11 : 0.07) * config.intensity*settings.speed;
            double vx = Math.cos(angle) * speed;
            double vy = (RANDOM.nextDouble() - 0.15) * speed;
            double vz = Math.sin(angle) * speed;
            if(settings.shape.equals("Upward")){vx*=.25;vz*=.25;vy=Math.abs(vy)+speed;}
            if(settings.shape.equals("Ring"))vy=0;
            client.level.addParticle(particle,
                    px, py, pz, vx, vy, vz);
            if (settings.extraSparks && critical && i % 3 == 0) {
                client.level.addParticle(ParticleTypes.ELECTRIC_SPARK, px, py, pz, vx * 0.6, vy * 0.6, vz * 0.6);
            } else if (settings.extraSparks && !critical && i % 5 == 0) {
                client.level.addParticle(ParticleTypes.GLOW, px, py, pz, 0, 0.015, 0);
            }
        }
    }

    public static float flashProgress() {
        return Math.max(0, 1f - (System.currentTimeMillis() - lastHitMillis) / (ConfigManager.get().hit.flashDuration*1000));
    }

    public static boolean wasCritical() { return lastCritical; }
}
