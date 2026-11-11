package com.tuusuario.mmo.eldencraft.skills.mage;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import com.tuusuario.mmo.eldencraft.util.GlowEffect;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class TormentaElectrica implements Skill {
    public String getName() { return "Tormenta Eléctrica"; }
    public double getCost() { return 70.0; }
    public int getLevelRequired() { return 20; }

    public void execute(Player player, PlayerData data) {
        EldenCraft plugin = JavaPlugin.getPlugin(EldenCraft.class);
        double dmg = 20.0 + (data.getIntelligence() * 0.8);

        // Glow eléctrico
        GlowEffect.apply(plugin, player, ChatColor.AQUA, 60);

        // Sonido épico de apertura
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1.5f);
        player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.5f, 0.6f);

        // Aura eléctrica pre-cast
        player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK,
                player.getLocation().add(0, 1, 0), 80, 1.5, 1.5, 1.5, 0.3);
        player.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME,
                player.getLocation().add(0, 0.5, 0), 40, 2.0, 0.2, 2.0, 0.05);

        // Anillo eléctrico circular
        Particle.DustOptions electricBlue = new Particle.DustOptions(Color.fromRGB(0, 200, 255), 1.5f);
        for (int i = 0; i < 36; i++) {
            double angle = i * (2 * Math.PI / 36);
            double x = 3.0 * Math.cos(angle);
            double z = 3.0 * Math.sin(angle);
            player.getWorld().spawnParticle(Particle.DUST,
                    player.getLocation().add(x, 0.5, z), 3, 0, 0, 0, 0, electricBlue);
        }

        // Rayos con delay dramático
        player.getNearbyEntities(10, 10, 10).forEach(e -> {
            if (e instanceof LivingEntity && e != player) {
                LivingEntity target = (LivingEntity) e;
                player.getWorld().strikeLightningEffect(e.getLocation());
                target.damage(dmg, player);
                target.getWorld().spawnParticle(Particle.ELECTRIC_SPARK,
                        target.getLocation().add(0, 1, 0), 20, 0.3, 0.5, 0.3, 0.1);
            }
        });

        // Post-cast: onda expansiva de partículas
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.getWorld().spawnParticle(Particle.FLASH,
                    player.getLocation().add(0, 1, 0), 2);
            player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK,
                    player.getLocation().add(0, 1, 0), 100, 5.0, 2.0, 5.0, 0.5);
            player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.5f, 0.8f);
        }, 5L);

        player.sendMessage("§b§l⚡ ¡TORMENTA ELÉCTRICA! §7¡Rayos devastadores sobre tus enemigos!");
    }
}
