package com.tuusuario.mmo.eldencraft.skills.monk;

import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class FlujoKi implements Skill {
    @Override public String getName() { return "Flujo Ki"; }
    @Override public double getCost() { return 30.0; }
    @Override public int getLevelRequired() { return 9; }

    @Override
    public void execute(Player player, PlayerData data) {
        player.removePotionEffect(PotionEffectType.POISON);
        player.removePotionEffect(PotionEffectType.WITHER);
        player.removePotionEffect(PotionEffectType.SLOWNESS);
        player.removePotionEffect(PotionEffectType.WEAKNESS);

        double heal = 6.0 + data.getConstitution() * 0.5 + data.getWisdom() * 0.3;
        double maxHp = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        player.setHealth(Math.min(maxHp, player.getHealth() + heal));

        player.getWorld().spawnParticle(Particle.HEART,
                player.getLocation().add(0, 1.5, 0), 10, 0.3, 0.3, 0.3, 0);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.6f, 1.2f);
        player.sendMessage("§e✦ Flujo Ki: §7+" + String.format("%.1f", heal) + " HP y efectos negativos removidos.");
    }
}
