package com.tuusuario.mmo.eldencraft.skills.bard;

import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import com.tuusuario.mmo.eldencraft.util.GlowEffect;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Sound;
import org.bukkit.Particle;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.plugin.java.JavaPlugin;
import com.tuusuario.mmo.eldencraft.EldenCraft;
import org.bukkit.entity.Entity;

public class Inspiracion implements Skill {
    @Override
    public String getName() { return "Inspiración"; }
    @Override
    public double getCost() { return 60.0; }
    @Override
    public int getLevelRequired() { return 20; }

    @Override
    public void execute(Player player, PlayerData data) {
        EldenCraft plugin = JavaPlugin.getPlugin(EldenCraft.class);

        // Glow púrpura mágico al bardo
        GlowEffect.apply(plugin, player, ChatColor.LIGHT_PURPLE, 300);

        // Buff a aliados + glow
        for (Entity e : player.getNearbyEntities(8, 8, 8)) {
            if (e instanceof Player ally && plugin.getPartyManager().canHeal(player, ally)) {
                ally.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 300, 1));
                ally.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 300, 1));
                ally.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 60, 0, false, false));
                ally.getWorld().spawnParticle(Particle.END_ROD,
                        ally.getLocation().add(0, 1, 0), 15, 0.3, 0.6, 0.3, 0.05);
                ally.getWorld().spawnParticle(Particle.ENCHANT,
                        ally.getLocation().add(0, 0.5, 0), 30, 0.5, 1.0, 0.5, 0.8);
                ally.sendMessage("§d§l» §f¡Te sientes inspirado por la música!");
            }
        }

        // Sonidos épicos
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1.5f);
        player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.8f, 1.2f);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1f, 1.0f);

        // Explosión musical inicial
        player.getWorld().spawnParticle(Particle.END_ROD,
                player.getLocation().add(0, 1, 0), 60, 1.5, 1.5, 1.5, 0.15);
        player.getWorld().spawnParticle(Particle.ENCHANT,
                player.getLocation().add(0, 1, 0), 80, 2.0, 2.0, 2.0, 1.0);

        // Anillo de notas musicales doradas
        Particle.DustOptions goldDust = new Particle.DustOptions(Color.fromRGB(255, 200, 50), 1.5f);
        for (int i = 0; i < 36; i++) {
            double angle = i * (2 * Math.PI / 36);
            double x = 2.5 * Math.cos(angle);
            double z = 2.5 * Math.sin(angle);
            player.getWorld().spawnParticle(Particle.DUST,
                    player.getLocation().add(x, 1, z), 2, 0, 0, 0, 0, goldDust);
        }

        // Aura musical continua
        Bukkit.getScheduler().runTaskTimer(plugin, new java.util.function.Consumer<org.bukkit.scheduler.BukkitTask>() {
            int ticks = 0;
            @Override
            public void accept(org.bukkit.scheduler.BukkitTask task) {
                if (ticks++ > 300 || !player.isOnline()) {
                    task.cancel();
                    return;
                }
                if (ticks % 4 == 0) {
                    player.getWorld().spawnParticle(Particle.NOTE,
                            player.getLocation().add(0, 2.2, 0), 3, 0.6, 0.3, 0.6, 1);
                }
                if (ticks % 8 == 0) {
                    player.getWorld().spawnParticle(Particle.END_ROD,
                            player.getLocation().add(0, 1.5, 0), 2, 0.5, 0.5, 0.5, 0.03);
                    player.getWorld().spawnParticle(Particle.HEART,
                            player.getLocation().add(0, 1.8, 0), 1, 0.5, 0.3, 0.5, 0);
                }
            }
        }, 0L, 1L);

        player.sendMessage("§d§l♫ ¡INSPIRACIÓN! §7Tu música inspira a todos tus aliados.");
    }
}