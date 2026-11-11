package com.tuusuario.mmo.eldencraft.skills.barbarian;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

// Carga Salvaje (salto del bárbaro): - Impulso hacia delante y arriba.
public class CargaSalvaje implements Skill {
    @Override public String getName() { return "Carga Salvaje"; }
    @Override public double getCost() { return 25.0; }
    @Override public int getLevelRequired() { return 6; }

    @Override
    public void execute(Player player, PlayerData data) {
        EldenCraft plugin = JavaPlugin.getPlugin(EldenCraft.class);
        player.setVelocity(player.getLocation().getDirection().multiply(1.6).setY(0.65));
        player.playSound(player.getLocation(), Sound.ENTITY_RAVAGER_ROAR, 1f, 0.7f);

        final double aoeDmg = 8.0 + (data.getStrength() * 0.4);

        new BukkitRunnable() {
            int ticks = 0;
            boolean dejoSuelo = false;
            @Override
            public void run() {
                ticks++;
                if (ticks > 100) { cancel(); return; }
                if (!dejoSuelo) {
                    if (!player.isOnGround()) dejoSuelo = true;
                    return;
                }
                if (player.isOnGround()) {
                    for (Entity e : player.getNearbyEntities(4, 3, 4)) {
                        if (e instanceof LivingEntity && e != player) {
                            ((LivingEntity) e).damage(aoeDmg, player);
                        }
                    }
                    player.getWorld().spawnParticle(Particle.EXPLOSION, player.getLocation(), 1);
                    player.getWorld().spawnParticle(Particle.CLOUD,
                            player.getLocation(), 30, 1.0, 0.1, 1.0, 0.1);
                    player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1f, 0.7f);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 2L, 2L);
    }
}
