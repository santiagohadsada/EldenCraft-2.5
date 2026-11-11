package com.tuusuario.mmo.eldencraft.skills.rogue;

import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import com.tuusuario.mmo.eldencraft.util.GlowEffect;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Sound;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.plugin.java.JavaPlugin;
import com.tuusuario.mmo.eldencraft.EldenCraft;

public class Invisibilidad implements Skill {
    @Override
    public String getName() { return "Invisibilidad"; }
    @Override
    public double getCost() { return 60.0; }
    @Override
    public int getLevelRequired() { return 20; }

    @Override
    public void execute(Player player, PlayerData data) {
        EldenCraft plugin = JavaPlugin.getPlugin(EldenCraft.class);

        // Flash de glow gris antes de desaparecer (efecto dramático breve)
        GlowEffect.apply(plugin, player, ChatColor.DARK_GRAY, 20);

        // Invisibilidad absoluta por 10 segundos
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 200, 1));

        // Sonidos dramáticos
        player.playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 1f, 1f);
        player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_EMERGE, 0.4f, 2.0f);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.6f, 0.5f);

        // Bomba de humo épica
        player.getWorld().spawnParticle(Particle.LARGE_SMOKE,
                player.getLocation().add(0, 1, 0), 80, 0.8, 1.2, 0.8, 0.08);
        player.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE,
                player.getLocation().add(0, 0.5, 0), 30, 0.5, 0.3, 0.5, 0.02);
        player.getWorld().spawnParticle(Particle.PORTAL,
                player.getLocation().add(0, 1, 0), 50, 0.6, 1.0, 0.6, 0.8);
        player.getWorld().spawnParticle(Particle.ENCHANT,
                player.getLocation().add(0, 1, 0), 60, 0.8, 1.5, 0.8, 1.0);

        // Anillo oscuro de humo
        Particle.DustOptions shadowDust = new Particle.DustOptions(Color.fromRGB(30, 30, 40), 2.0f);
        for (int i = 0; i < 24; i++) {
            double angle = i * (2 * Math.PI / 24);
            double x = 1.5 * Math.cos(angle);
            double z = 1.5 * Math.sin(angle);
            player.getWorld().spawnParticle(Particle.DUST,
                    player.getLocation().add(x, 0.1, z), 3, 0, 0, 0, 0, shadowDust);
        }

        player.sendMessage("§f§l» §7Te has vuelto completamente invisible.");

        // Trail de sombras sutiles mientras invisible
        Bukkit.getScheduler().runTaskTimer(plugin, new java.util.function.Consumer<org.bukkit.scheduler.BukkitTask>() {
            int ticks = 0;
            @Override
            public void accept(org.bukkit.scheduler.BukkitTask task) {
                if (ticks++ > 200 || !player.isOnline()) {
                    task.cancel();
                    // Efecto al reaparecer
                    if (player.isOnline()) {
                        player.getWorld().spawnParticle(Particle.LARGE_SMOKE,
                                player.getLocation().add(0, 1, 0), 30, 0.4, 0.8, 0.4, 0.03);
                        player.getWorld().spawnParticle(Particle.ENCHANT,
                                player.getLocation().add(0, 1, 0), 40, 0.5, 1.0, 0.5, 0.5);
                        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1.5f);
                    }
                    return;
                }
                if (ticks % 5 == 0) {
                    player.getWorld().spawnParticle(Particle.PORTAL,
                            player.getLocation().add(0, 1, 0), 2, 0.3, 0.8, 0.3, 0.01);
                    player.getWorld().spawnParticle(Particle.ASH,
                            player.getLocation().add(0, 1, 0), 3, 0.3, 0.5, 0.3, 0);
                }
            }
        }, 0L, 1L);
    }
}
