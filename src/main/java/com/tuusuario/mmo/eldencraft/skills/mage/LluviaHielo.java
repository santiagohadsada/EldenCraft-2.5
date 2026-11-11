package com.tuusuario.mmo.eldencraft.skills.mage;

import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.Sound;

public class LluviaHielo implements Skill {
    public String getName() { return "Lluvia de Hielo"; }
    public double getCost() { return 30.0; }
    public int getLevelRequired() { return 9; }

    public void execute(Player player, PlayerData data) {
        double dmg = 5.0 + (data.getIntelligence() * 0.4);
        player.getNearbyEntities(5, 5, 5).forEach(e -> {
            if (e instanceof org.bukkit.entity.LivingEntity && e != player) {
                ((org.bukkit.entity.LivingEntity) e).damage(dmg);
                ((org.bukkit.entity.LivingEntity) e).addPotionEffect(
                        new org.bukkit.potion.PotionEffect(
                                org.bukkit.potion.PotionEffectType.SLOWNESS, 80, 2));
            }
        });
        player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1f, 0.5f);
    }
}
