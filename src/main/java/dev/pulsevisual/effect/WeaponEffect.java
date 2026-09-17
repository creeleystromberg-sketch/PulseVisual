package dev.pulsevisual.effect;

import dev.pulsevisual.config.ConfigManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public final class WeaponEffect {
    private static final Random RANDOM = new Random();
    private static int ticks;

    private WeaponEffect() { }

    public static boolean holdingWeapon(ItemStack stack) {
        return stack.is(ItemTags.SWORDS) || stack.is(ItemTags.AXES);
    }

    public static boolean holdingItem(ItemStack stack) { return !stack.isEmpty(); }

    public static void tick(Minecraft client) {
        if (client.player == null || client.level == null || !ConfigManager.get().weaponEffects) return;
        ItemStack stack = client.player.getMainHandItem();
        if (!holdingItem(stack)) return;
        boolean weapon = holdingWeapon(stack);
        int interval = Math.max(3, Math.round((weapon ? 6f : 17f) / ConfigManager.get().intensity));
        if (++ticks % interval != 0) return;
        Vec3 eye = client.player.getEyePosition();
        Vec3 look = client.player.getLookAngle();
        Vec3 right = look.cross(new Vec3(0, 1, 0)).normalize();
        Vec3 point = eye.add(look.scale(0.65)).add(right.scale(0.28)).add(0, -0.24, 0);
        client.level.addParticle(weapon ? ParticleTypes.END_ROD : ParticleTypes.WAX_ON,
                point.x + (RANDOM.nextDouble() - 0.5) * 0.08,
                point.y + (RANDOM.nextDouble() - 0.5) * 0.12,
                point.z + (RANDOM.nextDouble() - 0.5) * 0.08,
                0, 0.012, 0);
    }
}
