package com.tuusuario.mmo.eldencraft.skills.cleric;

import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class Purificacion implements Skill {
    @Override public String getName() { return "Purificación"; }
    @Override public double getCost() { return 15.0; }
    @Override public int getLevelRequired() { return 3; }

    @Override
    public void execute(Player player, PlayerData data) {
        PotionEffectType[] negativos = {
            PotionEffectType.POISON, PotionEffectType.WITHER, PotionEffectType.SLOWNESS,
            PotionEffectType.WEAKNESS, PotionEffectType.BLINDNESS, PotionEffectType.NAUSEA,
            PotionEffectType.HUNGER, PotionEffectType.LEVITATION, PotionEffectType.BAD_OMEN
        };
        int removed = 0;
        for (PotionEffectType type : negativos) {
            if (player.hasPotionEffect(type)) {
                player.removePotionEffect(type);
                removed++;
            }
        }
        player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING,
                player.getLocation().add(0, 1, 0), 30, 0.3, 0.6, 0.3, 0.1);
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1.5f);
        player.sendMessage("§f✦ Purificación: §7" + removed + " efecto(s) negativos eliminados.");
    }
}
