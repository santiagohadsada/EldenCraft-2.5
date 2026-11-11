package com.tuusuario.mmo.eldencraft.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class BleedTask {

    public static void applyBleed(LivingEntity victim, Player attacker, int ticks, double damagePerTick, Plugin plugin) {
        new org.bukkit.scheduler.BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count >= ticks || !victim.isValid() || victim.isDead()) {
                    this.cancel();
                    return;
                }
                victim.damage(damagePerTick, attacker);
                victim.getWorld().spawnParticle(Particle.BLOCK, victim.getLocation().add(0, 1, 0), 10, 0.3, 0.5, 0.3, org.bukkit.Material.REDSTONE_BLOCK.createBlockData());
                count++;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }
}
