package com.tuusuario.mmo.eldencraft.skills.monk;

import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class GolpeKi implements Skill {
    @Override public String getName() { return "Golpe Ki"; }
    @Override public double getCost() { return 15.0; }
    @Override public int getLevelRequired() { return 1; }

    @Override
    public void execute(Player player, PlayerData data) {
        double dmg = 2.0 + data.getStrength() * 0.1 + data.getDexterity() * 0.05;
        int hit = 0;
        com.tuusuario.mmo.eldencraft.EldenCraft plugin = org.bukkit.plugin.java.JavaPlugin.getPlugin(com.tuusuario.mmo.eldencraft.EldenCraft.class);
        for (Entity e : player.getNearbyEntities(3, 3, 3)) {
            if (!(e instanceof LivingEntity) || plugin.getPartyManager().isAlly(player, e)) continue;
            ((LivingEntity) e).damage(dmg, player);
            hit++;
        }
        player.getWorld().spawnParticle(Particle.CRIT,
                player.getLocation().add(0, 1, 0), 20, 0.5, 0.3, 0.5, 0.15);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1f, 1.4f);
        if (hit == 0)
            player.sendMessage("§e✦ Golpe Ki: §7No hay enemigos cuerpo a cuerpo.");
        else
            player.sendMessage("§e✦ Golpe Ki: §7" + hit + " enemigos golpeados.");
    }
}
