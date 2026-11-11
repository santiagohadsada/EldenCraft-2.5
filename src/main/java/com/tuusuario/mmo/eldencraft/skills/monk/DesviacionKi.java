package com.tuusuario.mmo.eldencraft.skills.monk;

import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class DesviacionKi implements Skill {
    @Override public String getName() { return "Desviación Ki"; }
    @Override public double getCost() { return 35.0; }
    @Override public int getLevelRequired() { return 12; }

    @Override
    public void execute(Player player, PlayerData data) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 60, 3, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 60, 1, false, false));
        player.getWorld().spawnParticle(Particle.ENCHANTED_HIT,
                player.getLocation().add(0, 1, 0), 30, 0.4, 0.6, 0.4, 0.1);
        player.playSound(player.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1f, 1.5f);
        player.sendMessage("§e✦ Desviación Ki: §7¡Escudo de Ki activo por 3 segundos!");
    }
}
