package com.tuusuario.mmo.eldencraft.skills.sorcerer;

import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class DescargaCaotica implements Skill {
    @Override public String getName() { return "Descarga Caótica"; }
    @Override public double getCost() { return 25.0; }
    @Override public int getLevelRequired() { return 3; }

    @Override
    public void execute(Player player, PlayerData data) {
        List<LivingEntity> targets = new ArrayList<>();
        com.tuusuario.mmo.eldencraft.EldenCraft plugin = org.bukkit.plugin.java.JavaPlugin.getPlugin(com.tuusuario.mmo.eldencraft.EldenCraft.class);
        for (Entity e : player.getNearbyEntities(8, 8, 8)) {
            if (e instanceof LivingEntity && !plugin.getPartyManager().isAlly(player, e)) {
                targets.add((LivingEntity) e);
            }
        }
        if (targets.isEmpty()) {
            player.sendMessage("§c✦ No hay enemigos cercanos para la Descarga Caótica.");
            return;
        }
        double dmg = 6.0 + data.getIntelligence() * 0.4;
        LivingEntity prev = null;
        int jumps = Math.min(targets.size(), 4);
        for (int i = 0; i < jumps; i++) {
            LivingEntity target = targets.get(i);
            target.damage(dmg, player);
            Location loc = target.getLocation().add(0, 1, 0);
            player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, loc, 20, 0.3, 0.5, 0.3, 0.1);
            player.getWorld().strikeLightningEffect(loc);
            if (prev != null) {
                player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK,
                        prev.getLocation().add(0, 1, 0).toVector()
                                .midpoint(loc.toVector()).toLocation(player.getWorld()),
                        10, 0.2, 0.3, 0.2, 0.05);
            }
            prev = target;
            dmg *= 0.75;
        }
        player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 1.5f);
        player.sendMessage("§b✦ Descarga Caótica: §7salta entre " + jumps + " enemigos.");
    }
}
