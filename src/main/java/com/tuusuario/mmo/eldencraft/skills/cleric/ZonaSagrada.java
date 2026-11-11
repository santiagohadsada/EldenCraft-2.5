package com.tuusuario.mmo.eldencraft.skills.cleric;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ZonaSagrada implements Skill {
    @Override public String getName() { return "Zona Sagrada"; }
    @Override public double getCost() { return 35.0; }
    @Override public int getLevelRequired() { return 6; }

    @Override
    public void execute(Player player, PlayerData data) {
        EldenCraft plugin = JavaPlugin.getPlugin(EldenCraft.class);
        double healPerTick = 2.0 + data.getWisdom() * 0.2;

        Bukkit.getScheduler().runTaskTimer(plugin, task -> {
            if (!player.isOnline()) { task.cancel(); return; }

            for (Entity e : player.getNearbyEntities(6, 4, 6)) {
                if (!(e instanceof Player ally) || !plugin.getPartyManager().canHeal(player, ally)) continue;
                double maxHp = ally.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                double missingHp = maxHp - ally.getHealth();
                double scaledHeal = healPerTick + (missingHp * 0.1);
                ally.setHealth(Math.min(maxHp, ally.getHealth() + scaledHeal));
                ally.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 40, 0, false, false));
                ally.getWorld().spawnParticle(Particle.END_ROD,
                        ally.getLocation().add(0, 1, 0), 3, 0.2, 0.3, 0.2, 0.02);
            }
            double maxHp = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            double missingHp = maxHp - player.getHealth();
            double scaledHeal = healPerTick + (missingHp * 0.1);
            player.setHealth(Math.min(maxHp, player.getHealth() + scaledHeal));
            player.getWorld().spawnParticle(Particle.END_ROD,
                    player.getLocation().add(0, 1, 0), 5, 0.3, 0.4, 0.3, 0.03);

        }, 0L, 20L);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.sendMessage("§f✦ Zona Sagrada terminó.");
        }, 200L);

        player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING,
                player.getLocation().add(0, 1, 0), 40, 2.0, 0.5, 2.0, 0.1);
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1.2f);
        player.sendMessage("§f✦ Zona Sagrada: §7¡Área de curación activa por 10 segundos!");
    }
}
