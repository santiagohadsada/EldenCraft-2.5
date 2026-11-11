package com.tuusuario.mmo.eldencraft.skills.mage;

import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.Sound;

public class ExplosionMagica implements Skill {
    public String getName() { return "Explosión Mágica"; }
    public double getCost() { return 45.0; }
    public int getLevelRequired() { return 12; }

    public void execute(Player player, PlayerData data) {
        player.getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION_EMITTER, player.getLocation(), 1);
        double dmg = 13.0 + (data.getIntelligence() * 0.6);
        player.getNearbyEntities(6, 6, 6).forEach(e -> {
            if (e instanceof org.bukkit.entity.LivingEntity && e != player) {
                ((org.bukkit.entity.LivingEntity) e).damage(dmg);
            }
        });
        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1f, 1.2f);
    }
}
