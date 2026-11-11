package com.tuusuario.mmo.eldencraft.skills.barbarian;

import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.Sound;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;

public class GolpeBrutal implements Skill {
    @Override public String getName() { return "Golpe Brutal"; }
    @Override public double getCost() { return 20.0; }
    @Override public int getLevelRequired() { return 1; }

    @Override
    public void execute(Player player, PlayerData data) {
        boolean hit = false;
        for (Entity e : player.getNearbyEntities(3, 3, 3)) {
            if (e instanceof LivingEntity && e != player) {
                LivingEntity target = (LivingEntity) e;
                double damage = 7.0 + (data.getStrength() * 0.4);
                target.damage(damage, player); // fuente atribuida al jugador
                target.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, target.getLocation(), 5);
                hit = true;
                break;
            }
        }
        player.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, 1f, 0.5f);
        if (!hit) {
            player.sendActionBar("§c✖ Golpe Brutal falló — ningún enemigo en rango.");
        }
    }
}
