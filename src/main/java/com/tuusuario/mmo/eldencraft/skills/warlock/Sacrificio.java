package com.tuusuario.mmo.eldencraft.skills.warlock;

import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Sacrificio implements Skill {
    @Override public String getName() { return "Sacrificio"; }
    @Override public double getCost() { return 0.0; }
    @Override public int getLevelRequired() { return 12; }

    @Override
    public void execute(Player player, PlayerData data) {
        if (player.getHealth() <= 20.0) {
            player.sendMessage("§c✦ Necesitas más de 10 corazones de vida para el Sacrificio.");
            return;
        }
        player.damage(20.0);
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 200, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 200, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1));
        player.getWorld().spawnParticle(Particle.ASH,
                player.getLocation().add(0, 1, 0), 60, 0.5, 0.8, 0.5, 0.1);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, 1f, 0.5f);
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 0.6f, 1.3f);
        player.sendMessage("§4✦ Sacrificio: §7Ofrendas tu sangre por poder oscuro (10s).");
    }
}
