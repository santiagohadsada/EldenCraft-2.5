package com.tuusuario.mmo.eldencraft.skills.gunslinger;

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

public class ModoDuelo implements Skill {
    @Override public String getName() { return "Modo Duelo"; }
    @Override public double getCost() { return 80.0; }
    @Override public int getLevelRequired() { return 18; }

    @Override
    public void execute(Player player, PlayerData data) {
        EldenCraft plugin = JavaPlugin.getPlugin(EldenCraft.class);

        // Glow dorado
        GlowEffect.apply(plugin, player, ChatColor.GOLD, 300);

        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 300, 1, true, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 300, 1, true, false));
        com.tuusuario.mmo.eldencraft.util.BalanceState.gunslingerDuelMode.put(
                player.getUniqueId(), System.currentTimeMillis() + 15000);

        // Sonidos dramáticos de duelo
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 2f);
        player.playSound(player.getLocation(), Sound.ITEM_CROSSBOW_SHOOT, 1f, 0.1f);
        player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.8f, 0.5f);

        // Flash inicial
        player.getWorld().spawnParticle(Particle.FLASH, player.getLocation().add(0, 1, 0), 2);
        player.getWorld().spawnParticle(Particle.FLAME, player.getLocation().add(0, 1, 0), 50, 0.6, 1.0, 0.6, 0.12);
        player.getWorld().spawnParticle(Particle.CRIT, player.getLocation().add(0, 1, 0), 40, 0.8, 1.2, 0.8, 0.15);
        player.getWorld().spawnParticle(Particle.LAVA, player.getLocation().add(0, 0.5, 0), 10, 0.3, 0.1, 0.3, 0);

        // Anillo de fuego
        Particle.DustOptions orangeDust = new Particle.DustOptions(Color.fromRGB(255, 140, 0), 1.5f);
        for (int i = 0; i < 24; i++) {
            double angle = i * (2 * Math.PI / 24);
            double x = 2.0 * Math.cos(angle);
            double z = 2.0 * Math.sin(angle);
            player.getWorld().spawnParticle(Particle.FLAME, player.getLocation().add(x, 0.2, z), 2, 0, 0, 0, 0);
            player.getWorld().spawnParticle(Particle.DUST, player.getLocation().add(x, 0.5, z), 2, 0, 0, 0, 0, orangeDust);
        }

        // Aura continua de duelo
        Bukkit.getScheduler().runTaskTimer(plugin, new java.util.function.Consumer<org.bukkit.scheduler.BukkitTask>() {
            int ticks = 0;
            @Override
            public void accept(org.bukkit.scheduler.BukkitTask task) {
                if (ticks++ > 300 || !player.isOnline()) {
                    task.cancel();
                    return;
                }
                if (ticks % 3 == 0) {
                    double angle = ticks * 0.4;
                    double x = Math.cos(angle) * 1.2;
                    double z = Math.sin(angle) * 1.2;
                    player.getWorld().spawnParticle(Particle.FLAME, player.getLocation().add(x, 0.5, z), 1, 0, 0, 0, 0);
                    player.getWorld().spawnParticle(Particle.LARGE_SMOKE, player.getLocation().add(-x, 0.5, -z), 1, 0, 0, 0, 0);
                }
                if (ticks % 10 == 0) {
                    player.getWorld().spawnParticle(Particle.CRIT, player.getLocation().add(0, 1, 0), 3, 0.3, 0.5, 0.3, 0.05);
                    player.getWorld().spawnParticle(Particle.LAVA, player.getLocation().add(0, 0.2, 0), 1, 0.2, 0.1, 0.2, 0);
                }
            }
        }, 0L, 1L);

        player.sendMessage("§c§l⚔ Modo Duelo: §7¡Daño y velocidad al máximo, pero no puedes curarte por 15s!");
    }
}
