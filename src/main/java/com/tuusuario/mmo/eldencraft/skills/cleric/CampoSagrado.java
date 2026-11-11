package com.tuusuario.mmo.eldencraft.skills.cleric;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class CampoSagrado implements Skill {
    @Override public String getName() { return "Campo Sagrado"; }
    @Override public double getCost() { return 60.0; }
    @Override public int getLevelRequired() { return 16; }
    @Override public int getCooldown() { return 60; } // 1 minuto

    @Override
    public void execute(Player player, PlayerData data) {
        EldenCraft plugin = JavaPlugin.getPlugin(EldenCraft.class);
        double healPerTick = 3.0 + data.getWisdom() * 0.25;
        double dmgUndead   = 5.0 + data.getWisdom() * 0.3;

        Bukkit.getScheduler().runTaskTimer(plugin, new java.util.function.Consumer<org.bukkit.scheduler.BukkitTask>() {
            int ticks = 0;
            @Override
            public void accept(org.bukkit.scheduler.BukkitTask task) {
                if (!player.isOnline() || ++ticks > 20) {
                    task.cancel();
                    if (player.isOnline()) player.sendMessage("§f✦ Campo Sagrado terminó.");
                    return;
                }

                for (Entity e : player.getWorld().getNearbyEntities(player.getLocation(), 5, 5, 5)) {
                    if (e instanceof Player ally) {
                        if (plugin.getPartyManager().canHeal(player, ally)) {
                            double maxHp = ally.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                            ally.setHealth(Math.min(maxHp, ally.getHealth() + healPerTick));
                            ally.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 40, 0, false, false));
                        }
                    } else if (e instanceof LivingEntity && !plugin.getPartyManager().isAlly(player, e)) {
                        ((LivingEntity) e).damage(dmgUndead, player);
                    }
                }
                double maxHp = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                player.setHealth(Math.min(maxHp, player.getHealth() + healPerTick));
                player.getWorld().spawnParticle(Particle.END_ROD,
                        player.getLocation().add(0, 0.5, 0), 5, 3.0, 0.2, 3.0, 0.02);
                
                // Círculo en el piso
                for (int i = 0; i < 360; i += 12) {
                    double angle = i * Math.PI / 180;
                    double x = 10 * Math.cos(angle);
                    double z = 10 * Math.sin(angle);
                    player.getWorld().spawnParticle(Particle.END_ROD,
                            player.getLocation().add(x, 0.1, z), 1, 0, 0, 0, 0);
                }
            }
        }, 0L, 20L);

        player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING,
                player.getLocation().add(0, 1, 0), 60, 3.0, 1.0, 3.0, 0.1);
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 0.9f);
        player.sendMessage("§f✦ Campo Sagrado: §7Aura de curación y daño a no-muertos (20s).");
    }
}
