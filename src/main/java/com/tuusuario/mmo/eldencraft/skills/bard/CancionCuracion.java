package com.tuusuario.mmo.eldencraft.skills.bard;

import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.Sound;
import org.bukkit.Particle;

public class CancionCuracion implements Skill {
    @Override
    public String getName() { return "Canción de Curación"; }
    @Override
    public double getCost() { return 20.0; }
    @Override
    public int getLevelRequired() { return 1; }

    @Override
    public void execute(Player player, PlayerData data) {
        double heal = 10.0 + (data.getCharisma() * 0.2);
        double RADIO = 6.0;
        com.tuusuario.mmo.eldencraft.EldenCraft plugin = org.bukkit.plugin.java.JavaPlugin.getPlugin(com.tuusuario.mmo.eldencraft.EldenCraft.class);
        for (Entity e : player.getNearbyEntities(RADIO, RADIO, RADIO)) {
            if (e instanceof Player ally && plugin.getPartyManager().canHeal(player, ally)) {
                ally.setHealth(Math.min(ally.getMaxHealth(), ally.getHealth() + heal));
                ally.spawnParticle(Particle.HEART, ally.getLocation().add(0, 1, 0), 5);
            }
        }
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1f, 1f);
    }
}