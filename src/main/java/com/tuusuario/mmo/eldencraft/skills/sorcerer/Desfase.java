package com.tuusuario.mmo.eldencraft.skills.sorcerer;

import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Desfase implements Skill {
    @Override public String getName() { return "Desfase"; }
    @Override public double getCost() { return 35.0; }
    @Override public int getLevelRequired() { return 9; }

    @Override
    public void execute(Player player, PlayerData data) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 1, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 60, 0, false, false));
        com.tuusuario.mmo.eldencraft.util.SorcererState.desfaseInmune.put(player.getUniqueId(), System.currentTimeMillis() + 3000);
        player.getWorld().spawnParticle(Particle.PORTAL,
                player.getLocation().add(0, 1, 0), 60, 0.3, 0.8, 0.3, 0.2);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1.3f);
        player.sendMessage("§b✦ Desfase: §7¡Intangibilidad durante 3 segundos!");
    }
}
