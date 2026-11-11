package com.tuusuario.mmo.eldencraft.skills.barbarian;

import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.Sound;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;

public class GolpeDemoledor implements Skill {
    @Override public String getName() { return "Golpe Demoledor"; }
    @Override public double getCost() { return 50.0; }
    @Override public int getLevelRequired() { return 12; }

    @Override
    public void execute(Player player, PlayerData data) {
        // Daño reducido (antes 15 + STR*0.6)
        double dmg = 11.0 + (data.getStrength() * 0.45);
        for (Entity e : player.getNearbyEntities(4, 4, 4)) {
            if (e instanceof LivingEntity && e != player) {
                LivingEntity target = (LivingEntity) e;
                target.damage(dmg);
                target.getWorld().spawnParticle(Particle.EXPLOSION, target.getLocation(), 1);
            }
        }
        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1f, 0.5f);
    }
}
