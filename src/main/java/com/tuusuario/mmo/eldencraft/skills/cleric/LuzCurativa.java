package com.tuusuario.mmo.eldencraft.skills.cleric;

import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

public class LuzCurativa implements Skill {
    @Override public String getName() { return "Luz Curativa"; }
    @Override public double getCost() { return 20.0; }
    @Override public int getLevelRequired() { return 1; }

    @Override
    public void execute(Player player, PlayerData data) {
        double maxHp = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        double missingHp = maxHp - player.getHealth();
        double healAmount = 8.0 + data.getWisdom() * 0.5 + missingHp * 0.2;
        player.setHealth(Math.min(maxHp, player.getHealth() + healAmount));

        player.getWorld().spawnParticle(Particle.END_ROD,
                player.getLocation().add(0, 1, 0), 20, 0.3, 0.5, 0.3, 0.05);
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_AMBIENT, 1f, 1.5f);
        player.sendMessage("§f✦ Luz Curativa: §7+" + String.format("%.1f", healAmount) + " HP restaurados.");
    }
}
