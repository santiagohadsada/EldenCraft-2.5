package com.tuusuario.mmo.eldencraft.skills.paladin;

import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.Sound;
import org.bukkit.Particle;

public class Curacion implements Skill {
    @Override
    public String getName() { return "Curación"; }
    @Override
    public double getCost() { return 20.0; } // Gasta Maná
    @Override
    public int getLevelRequired() { return 1; }

    @Override
    public void execute(Player player, PlayerData data) {
        // Sana 10 puntos (5 corazones) + escalado por Carisma
        double healing = 10.0 + (data.getCharisma() * 0.2);
        player.setHealth(Math.min(player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue(), player.getHealth() + healing));
        player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 0.8f);
    }
}