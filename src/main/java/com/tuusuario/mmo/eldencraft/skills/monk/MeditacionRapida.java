package com.tuusuario.mmo.eldencraft.skills.monk;

import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class MeditacionRapida implements Skill {
    @Override public String getName() { return "Meditación Rápida"; }
    @Override public double getCost() { return 20.0; }
    @Override public int getLevelRequired() { return 3; }

    @Override
    public void execute(Player player, PlayerData data) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1));
        player.getWorld().spawnParticle(Particle.END_ROD,
                player.getLocation().add(0, 1, 0), 30, 0.3, 0.5, 0.3, 0.05);
        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_CLUSTER_BREAK, 1f, 1.5f);
        player.sendMessage("§e✦ Meditación Rápida: §7Ki fluye: velocidad, resistencia y regeneración.");
    }
}
