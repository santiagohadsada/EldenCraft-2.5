package com.tuusuario.mmo.eldencraft.skills.gunslinger;

import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class Rodar implements Skill {
    @Override public String getName() { return "Rodar"; }
    @Override public double getCost() { return 15.0; }
    @Override public int getLevelRequired() { return 5; }

    @Override
    public void execute(Player player, PlayerData data) {
        Vector dir = player.getLocation().getDirection().setY(0).normalize();
        player.setVelocity(dir.multiply(1.5).setY(0.4));
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 20, 0.5, 0.1, 0.5, 0.1);
        player.playSound(player.getLocation(), Sound.ENTITY_HORSE_SADDLE, 1f, 1f);
        player.sendMessage("§e✦ Rodar: §7¡Evasión rápida!");
    }
}
