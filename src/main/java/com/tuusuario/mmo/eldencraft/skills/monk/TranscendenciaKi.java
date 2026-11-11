package com.tuusuario.mmo.eldencraft.skills.monk;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import com.tuusuario.mmo.eldencraft.util.GlowEffect;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class TranscendenciaKi implements Skill {
    @Override public String getName() { return "Transcendencia Ki"; }
    @Override public double getCost() { return 70.0; }
    @Override public int getLevelRequired() { return 20; }

    @Override
    public void execute(Player player, PlayerData data) {
        EldenCraft plugin = JavaPlugin.getPlugin(EldenCraft.class);
        GlowEffect.apply(plugin, player, ChatColor.YELLOW, 300);

        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 300, 3));
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 300, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 300, 3));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 300, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 1));

        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1.2f);
        player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 0.5f, 2.0f);
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1.5f);

        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0, 1, 0), 100, 0.8, 1.5, 0.8, 0.3);
        player.getWorld().spawnParticle(Particle.CRIT, player.getLocation().add(0, 1, 0), 80, 1.0, 1.2, 1.0, 0.2);
        player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, player.getLocation().add(0, 1, 0), 60, 1.5, 1.5, 1.5, 0.3);
        player.getWorld().spawnParticle(Particle.FLASH, player.getLocation().add(0, 1, 0), 2);

        Particle.DustOptions goldenKi = new Particle.DustOptions(Color.fromRGB(255, 215, 0), 2.0f);
        for (int y = 0; y < 5; y++) {
            player.getWorld().spawnParticle(Particle.DUST, player.getLocation().add(0, y, 0), 15, 0.3, 0.1, 0.3, 0, goldenKi);
            player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0, y, 0), 8, 0.2, 0.1, 0.2, 0.05);
        }

        player.sendMessage("§e§l✦ ¡TRANSCENDENCIA KI! §7¡Tu Ki alcanza el límite durante 15s!");

        Bukkit.getScheduler().runTaskTimer(plugin, new java.util.function.Consumer<org.bukkit.scheduler.BukkitTask>() {
            int ticks = 0;
            @Override
            public void accept(org.bukkit.scheduler.BukkitTask task) {
                if (ticks++ > 300 || !player.isOnline()) {
                    task.cancel();
                    if (player.isOnline()) {
                        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0, 1, 0), 30, 0.5, 1.0, 0.5, 0.1);
                        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 0.8f, 1.0f);
                    }
                    return;
                }
                if (ticks % 2 == 0) {
                    double angle = ticks * 0.3;
                    double x = 0.8 * Math.cos(angle);
                    double z = 0.8 * Math.sin(angle);
                    double y = (ticks % 40) * 0.1;
                    player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(x, y, z), 1, 0, 0, 0, 0);
                    player.getWorld().spawnParticle(Particle.DUST, player.getLocation().add(-x, y, -z), 1, 0, 0, 0, 0, goldenKi);
                }
                if (ticks % 6 == 0) {
                    player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, player.getLocation().add(0, 1, 0), 3, 0.5, 0.8, 0.5, 0.1);
                }
            }
        }, 0L, 1L);
    }
}
