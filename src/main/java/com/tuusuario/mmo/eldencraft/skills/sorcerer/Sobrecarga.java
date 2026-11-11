package com.tuusuario.mmo.eldencraft.skills.sorcerer;

import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import com.tuusuario.mmo.eldencraft.util.SorcererState;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class Sobrecarga implements Skill {
    @Override public String getName() { return "Sobrecarga"; }
    @Override public double getCost() { return 40.0; }
    @Override public int getLevelRequired() { return 12; }

    @Override
    public void execute(Player player, PlayerData data) {
        SorcererState.sobrecargaActiva.add(player.getUniqueId());
        player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK,
                player.getLocation().add(0, 1, 0), 50, 0.5, 0.8, 0.5, 0.15);
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1f, 2.0f);
        player.sendMessage("§e✦ Sobrecarga: §7¡Tu próxima habilidad se ejecutará dos veces!");
    }
}
