package com.tuusuario.mmo.eldencraft.skills.paladin;

import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import com.tuusuario.mmo.eldencraft.util.UndeadCheck;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.Sound;
import org.bukkit.Particle;

// Golpe Sagrado: - Daño aumentado.
public class GolpeSagrado implements Skill {
    @Override public String getName() { return "Golpe Sagrado"; }
    @Override public double getCost() { return 30.0; }
    @Override public int getLevelRequired() { return 6; }

    @Override
    public void execute(Player player, PlayerData data) {
        double base = 8.0 + (data.getCharisma() * 0.5);
        player.getNearbyEntities(4, 4, 4).forEach(e -> {
            if (e instanceof LivingEntity && e != player) {
                double dmg = base * (UndeadCheck.isUndead(e) ? 2.0 : 1.0);
                ((LivingEntity) e).damage(dmg, player);
                e.getWorld().spawnParticle(Particle.FIREWORK, e.getLocation().add(0, 1, 0), 8);
                if (UndeadCheck.isUndead(e)) {
                    e.getWorld().spawnParticle(Particle.END_ROD,
                            e.getLocation().add(0, 1, 0), 12, 0.3, 0.6, 0.3, 0.05);
                }
            }
        });
        player.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 1f, 1.2f);
    }
}
