package com.tuusuario.mmo.eldencraft.skills.monk;

import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class ExplosionKi implements Skill {
    @Override public String getName() { return "Explosión Ki"; }
    @Override public double getCost() { return 45.0; }
    @Override public int getLevelRequired() { return 16; }

    @Override
    public void execute(Player player, PlayerData data) {
        double dmg = 10.0 + data.getStrength() * 0.5 + data.getDexterity() * 0.4;
        int hit = 0;
        com.tuusuario.mmo.eldencraft.EldenCraft plugin = org.bukkit.plugin.java.JavaPlugin.getPlugin(com.tuusuario.mmo.eldencraft.EldenCraft.class);
        for (Entity e : player.getNearbyEntities(5, 5, 5)) {
            if (!(e instanceof LivingEntity) || plugin.getPartyManager().isAlly(player, e)) continue;
            LivingEntity mob = (LivingEntity) e;
            mob.damage(dmg, player);
            Vector dir = mob.getLocation().toVector()
                    .subtract(player.getLocation().toVector()).normalize();
            dir.setY(0.4);
            mob.setVelocity(dir.multiply(2.0));
            hit++;
        }
        player.getWorld().spawnParticle(Particle.CRIT,
                player.getLocation().add(0, 1, 0), 60, 2.0, 1.0, 2.0, 0.2);
        player.getWorld().spawnParticle(Particle.SWEEP_ATTACK,
                player.getLocation().add(0, 1, 0), 15, 1.5, 0.5, 1.5, 0);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1f, 0.7f);
        player.sendMessage("§e✦ Explosión Ki: §7" + hit + " enemigos golpeados con la onda.");
    }
}
