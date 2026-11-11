package com.tuusuario.mmo.eldencraft.skills.warlock;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.plugin.java.JavaPlugin;

public class InvocacionOscura implements Skill {
    @Override public String getName() { return "Invocación Oscura"; }
    @Override public double getCost() { return 60.0; }
    @Override public int getLevelRequired() { return 9; }
    @Override public int getCooldown() { return 180; } // 3 mins

    @Override
    public void execute(Player player, PlayerData data) {
        EldenCraft plugin = JavaPlugin.getPlugin(EldenCraft.class);

        for (int i = 0; i < 2; i++) {
            org.bukkit.entity.WitherSkeleton minion = player.getWorld().spawn(
                    player.getLocation().clone().add((i == 0 ? 1.5 : -1.5), 0, 0), org.bukkit.entity.WitherSkeleton.class);
            minion.setMetadata("summoner_uuid", new org.bukkit.metadata.FixedMetadataValue(plugin, player.getUniqueId().toString()));
            minion.setCustomName(ChatColor.DARK_GRAY + "☠ Espíritu Oscuro");
            minion.setCustomNameVisible(true);
            minion.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(40.0);
            minion.setHealth(40.0);
            minion.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(
                    6.0 + data.getIntelligence() * 0.4);

            player.getWorld().spawnParticle(Particle.ASH,
                    minion.getLocation().add(0, 1, 0), 30, 0.3, 0.5, 0.3, 0.05);

            // Wolf AI task
            Bukkit.getScheduler().runTaskTimer(plugin, task -> {
                if (!minion.isValid() || minion.isDead() || !player.isOnline()) {
                    if (minion.isValid()) minion.remove();
                    task.cancel();
                    return;
                }
                
                // Attack player's target or enemies targeting player
                org.bukkit.entity.LivingEntity target = null;
                if (player.getTargetEntity(15) instanceof org.bukkit.entity.LivingEntity le && !plugin.getPartyManager().isAlly(player, le)) {
                    target = le;
                }
                if (target == null) {
                    for (org.bukkit.entity.Entity e : player.getNearbyEntities(10, 10, 10)) {
                        if (e instanceof org.bukkit.entity.Mob mob && mob.getTarget() != null && mob.getTarget().equals(player)) {
                            target = mob;
                            break;
                        }
                    }
                }
                
                if (target != null) {
                    minion.setTarget(target);
                } else {
                    if (minion.getTarget() != null) {
                        if (minion.getTarget().equals(player) || minion.getTarget().hasMetadata("summoner_uuid")) {
                            minion.setTarget(null);
                        }
                    }
                    if (minion.getTarget() == null && minion.getLocation().distance(player.getLocation()) > 4.0) {
                        if (minion.getLocation().distance(player.getLocation()) > 20.0) {
                            minion.teleport(player.getLocation());
                        } else {
                            minion.getPathfinder().moveTo(player.getLocation(), 1.2);
                        }
                    }
                }
            }, 0L, 20L);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (minion.isValid() && !minion.isDead()) {
                    // Efecto de desaparición
                    minion.getWorld().spawnParticle(Particle.SOUL,
                            minion.getLocation().add(0, 1, 0), 25, 0.3, 0.6, 0.3, 0.05);
                    minion.getWorld().spawnParticle(Particle.LARGE_SMOKE,
                            minion.getLocation().add(0, 1, 0), 20, 0.4, 0.8, 0.4, 0.02);
                    minion.getWorld().spawnParticle(Particle.ASH,
                            minion.getLocation().add(0, 1, 0), 30, 0.5, 1.0, 0.5, 0.1);
                    minion.getWorld().playSound(minion.getLocation(),
                            Sound.ENTITY_WITHER_HURT, 0.6f, 0.5f);
                    minion.remove();
                }
            }, 2400L); // 2 minutes
        }

        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.5f, 1.5f);
        player.sendMessage("§8✦ Invocación Oscura: §7¡2 Espíritus Oscuros te sirven por 2 min!");
    }
}
