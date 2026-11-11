package com.tuusuario.mmo.eldencraft.skills.sorcerer;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class NucleoInestable implements Skill {
    @Override public String getName() { return "Núcleo Inestable"; }
    @Override public double getCost() { return 30.0; }
    @Override public int getLevelRequired() { return 6; }

    @Override
    public void execute(Player player, PlayerData data) {
        EldenCraft plugin = JavaPlugin.getPlugin(EldenCraft.class);
        Location loc = player.getLocation().clone();
        double dmg = 12.0 + data.getIntelligence() * 0.6;

        player.getWorld().spawnParticle(Particle.PORTAL, loc.add(0, 1, 0), 40, 0.5, 0.5, 0.5, 0.3);
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1f, 0.5f);
        player.sendMessage("§5✦ Núcleo Inestable: §7¡Explota en 3 segundos!");

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            player.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, loc, 5, 1.0, 0.5, 1.0, 0);
            player.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.2f, 1.0f);
            for (Entity e : loc.getWorld().getNearbyEntities(loc, 5, 5, 5)) {
                if (e instanceof LivingEntity && !plugin.getPartyManager().isAlly(player, e)) {
                    ((LivingEntity) e).damage(dmg, player);
                }
            }
        }, 60L);
    }
}
