package com.tuusuario.mmo.eldencraft.skills.mage;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import com.tuusuario.mmo.eldencraft.util.GlowEffect;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class EscudoMagico implements Skill {
    public String getName() { return "Escudo Mágico"; }
    public double getCost() { return 20.0; }
    public int getLevelRequired() { return 3; }

    public void execute(Player player, PlayerData data) {
        // Absorbe daño escalando con Inteligencia
        double shield = 5.0 + (data.getIntelligence() / 10.0);
        player.setAbsorptionAmount(shield);
        player.playSound(player.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1f, 1.5f);

        EldenCraft plugin = JavaPlugin.getPlugin(EldenCraft.class);

        // Brillo AZUL (blue contour) durante 10 segundos
        int duration = 10 * 20;
        GlowEffect.apply(plugin, player, ChatColor.BLUE, duration);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline() && player.getAbsorptionAmount() > 0) {
                player.setAbsorptionAmount(0);
                player.sendMessage(ChatColor.GRAY + "✦ Tu Escudo Mágico se ha disipado.");
            }
        }, duration);

        // Burst inicial de particulas tipo encantamiento + portal (azul)
        player.getWorld().spawnParticle(Particle.ENCHANT, player.getLocation().add(0, 1, 0), 50, 0.6, 1.2, 0.6, 0.5);
        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0, 1, 0), 20, 0.4, 0.8, 0.4, 0.05);
        player.sendMessage(ChatColor.AQUA + "✦ §lEscudo Mágico activado §7(brillo azul)");

        // Anillo de particulas alrededor del jugador, refrescado cada 0.5s mientras dure
        final int totalIters = duration / 10;
        new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count++ >= totalIters || !player.isOnline() || player.getAbsorptionAmount() <= 0) {
                    cancel();
                    return;
                }
                double radius = 1.2;
                for (int i = 0; i < 12; i++) {
                    double angle = 2 * Math.PI * i / 12 + (count * 0.2);
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    player.getWorld().spawnParticle(Particle.END_ROD,
                            player.getLocation().add(x, 1.0, z), 1, 0, 0, 0, 0);
                }
            }
        }.runTaskTimer(plugin, 10L, 10L);
    }
}
