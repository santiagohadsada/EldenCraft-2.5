package com.tuusuario.mmo.eldencraft.skills.ranger;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Wither;
import org.bukkit.entity.Wolf;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

// Compañero Lobo: invoca 2 lobos aliados que pelean contigo.
public class Camuflaje implements Skill {

    private static final int CANT_LOBOS = 2;
    private static final long DURACION_TICKS = 600L; // 30s

    @Override public String getName() { return "Camuflaje"; }
    @Override public double getCost() { return 30.0; }
    @Override public int getLevelRequired() { return 6; }
    @Override public int getCooldown() { return 180; } // 3 mins

    @Override
    public void execute(Player player, PlayerData data) {
        EldenCraft plugin = JavaPlugin.getPlugin(EldenCraft.class);
        Location origen = player.getLocation();

        LivingEntity objetivo = buscarHostilCercano(player, 16);
        final List<Wolf> invocados = new ArrayList<>();

        for (int i = 0; i < CANT_LOBOS; i++) {
            double dx = (i == 0 ? 1.0 : -1.0);
            Location spawn = origen.clone().add(dx, 0.1, 0);
            Wolf w = (Wolf) origen.getWorld().spawnEntity(spawn, EntityType.WOLF);
            w.setOwner(player);
            w.setCollarColor(DyeColor.ORANGE);
            w.setRemoveWhenFarAway(true);
            if (objetivo != null) w.setTarget(objetivo);

            w.getWorld().spawnParticle(Particle.HAPPY_VILLAGER,
                    spawn.clone().add(0, 1, 0), 12, 0.4, 0.4, 0.4, 0);
            invocados.add(w);
        }

        player.playSound(origen, Sound.ENTITY_WOLF_HOWL, 1f, 1f);
        player.sendMessage("§a[!] §7Tus compañeros lobo te apoyan en combate (30s).");

        // Refrescar objetivo cada 2 segundos por si el lobo se queda quieto
        new BukkitRunnable() {
            int it = 0;
            @Override
            public void run() {
                it++;
                if (it > 15 || invocados.stream().noneMatch(Mob::isValid)) {
                    cancel();
                    return;
                }
                LivingEntity nuevo = buscarHostilCercano(player, 16);
                if (nuevo != null) {
                    for (Wolf w : invocados) {
                        if (w.isValid() && (w.getTarget() == null || !w.getTarget().isValid())) {
                            w.setTarget(nuevo);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 40L, 40L);

        // Despawn al expirar
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            for (Wolf w : invocados) {
                if (w.isValid()) {
                    w.getWorld().spawnParticle(Particle.POOF, w.getLocation().add(0, 0.5, 0), 15);
                    w.remove();
                }
            }
        }, DURACION_TICKS);
    }

    private LivingEntity buscarHostilCercano(Player player, double radio) {
        LivingEntity mejor = null;
        double menor = Double.MAX_VALUE;
        for (Entity e : player.getNearbyEntities(radio, radio, radio)) {
            if (esHostil(e)) {
                double d = e.getLocation().distanceSquared(player.getLocation());
                if (d < menor) {
                    menor = d;
                    mejor = (LivingEntity) e;
                }
            }
        }
        return mejor;
    }

    private boolean esHostil(Entity e) {
        return e instanceof Monster || e instanceof Slime || e instanceof Phantom
                || e instanceof Ghast || e instanceof EnderDragon || e instanceof Wither;
    }
}
