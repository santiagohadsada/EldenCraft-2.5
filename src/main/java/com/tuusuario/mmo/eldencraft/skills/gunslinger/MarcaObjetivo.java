package com.tuusuario.mmo.eldencraft.skills.gunslinger;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class MarcaObjetivo implements Skill {
    @Override public String getName() { return "Marca de Objetivo"; }
    @Override public double getCost() { return 35.0; }
    @Override public int getLevelRequired() { return 11; }

    @Override
    public void execute(Player player, PlayerData data) {
        EldenCraft plugin = JavaPlugin.getPlugin(EldenCraft.class);
        LivingEntity target = null;
        
        // Find target in line of sight
        for (org.bukkit.entity.Entity e : player.getNearbyEntities(20, 20, 20)) {
            if (e instanceof LivingEntity && !plugin.getPartyManager().isAlly(player, e)) {
                org.bukkit.util.Vector dir = e.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
                if (player.getLocation().getDirection().dot(dir) > 0.70) {
                    target = (LivingEntity) e;
                    break;
                }
            }
        }
        
        if (target != null) {
            target.setMetadata("gunslinger_mark", new org.bukkit.metadata.FixedMetadataValue(plugin, System.currentTimeMillis() + 10000));
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1f, 2f);
            player.sendMessage("§e✦ Marca de Objetivo: §7¡" + target.getName() + " recibirá más daño por 10s!");
            
            LivingEntity finalTarget = target;
            Bukkit.getScheduler().runTaskTimer(plugin, task -> {
                if (!finalTarget.isValid() || finalTarget.isDead() || !finalTarget.hasMetadata("gunslinger_mark")) {
                    task.cancel();
                    return;
                }
                long expiry = finalTarget.getMetadata("gunslinger_mark").get(0).asLong();
                if (System.currentTimeMillis() > expiry) {
                    finalTarget.removeMetadata("gunslinger_mark", plugin);
                    task.cancel();
                    return;
                }
                finalTarget.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, finalTarget.getLocation().add(0, 2, 0), 1);
            }, 0L, 10L);
        } else {
            player.sendMessage("§cNo hay ningún objetivo en la mira.");
        }
    }
}
