package com.tuusuario.mmo.eldencraft.skills.paladin;

import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.Sound;
import org.bukkit.Particle;

// Aura de Curación: - Radio aumentado a 12 bloques.
public class AuraCuracion implements Skill {
    @Override public String getName() { return "Aura de Curación"; }
    @Override public double getCost() { return 50.0; }
    @Override public int getLevelRequired() { return 12; }

    @Override
    public void execute(Player player, PlayerData data) {
        double heal = 10.0 + (data.getCharisma() * 0.3);
        double radius = 12.0;

        // El paladin se cura a si mismo tambien
        sanar(player, heal);

        com.tuusuario.mmo.eldencraft.EldenCraft plugin = org.bukkit.plugin.java.JavaPlugin.getPlugin(com.tuusuario.mmo.eldencraft.EldenCraft.class);
        for (Entity e : player.getNearbyEntities(radius, radius, radius)) {
            if (e instanceof Player ally && plugin.getPartyManager().canHeal(player, ally)) {
                sanar(ally, heal);
            }
        }
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
        player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER,
                player.getLocation().add(0, 1, 0), 40, radius / 2.0, 1.0, radius / 2.0, 0);
    }

    private void sanar(Player ally, double heal) {
        double maxHealth = ally.getAttribute(
                org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getBaseValue();
        ally.setHealth(Math.min(maxHealth, ally.getHealth() + heal));
        ally.getWorld().spawnParticle(Particle.HEART,
                ally.getLocation().add(0, 1.2, 0), 6, 0.5, 0.5, 0.5, 0.05);
    }
}
