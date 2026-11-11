package com.tuusuario.mmo.eldencraft.skills.rogue;

import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Slot 4 del Pícaro - "Toxina Corrosiva":
 * Lanza una toxina al enemigo más cercano en 8 bloques.
 *
 * Contra enemigos NORMALES:
 *   - Veneno II (5s)   — no funciona en no-muertos, se sustituye abajo
 *   - Wither I (3s)    — daño verdadero que ignora absorción
 *   - Lentitud II (4s)
 *
 * Contra NO-MUERTOS (inmunes al Veneno):
 *   - Daño directo equivalente a Veneno II durante 5s  (daño inmediato)
 *   - Wither II (3s)   — versión más fuerte para compensar
 *   - Lentitud II (4s)
 *
 * Si el objetivo ya está envenenado/marchitado: +daño extra según Agilidad.
 * Costo: 30 energía | CD: 15s | Nivel 9
 */
public class Robo implements Skill {

    private static final double RANGO = 8.0;

    // Tipos de no-muertos en 1.21
    private static final java.util.Set<org.bukkit.entity.EntityType> UNDEAD =
        java.util.EnumSet.of(
            org.bukkit.entity.EntityType.ZOMBIE,
            org.bukkit.entity.EntityType.ZOMBIE_VILLAGER,
            org.bukkit.entity.EntityType.ZOMBIE_HORSE,
            org.bukkit.entity.EntityType.ZOMBIFIED_PIGLIN,
            org.bukkit.entity.EntityType.HUSK,
            org.bukkit.entity.EntityType.DROWNED,
            org.bukkit.entity.EntityType.SKELETON,
            org.bukkit.entity.EntityType.STRAY,
            org.bukkit.entity.EntityType.WITHER_SKELETON,
            org.bukkit.entity.EntityType.WITHER,
            org.bukkit.entity.EntityType.PHANTOM
        );

    @Override public String getName() { return "Toxina Corrosiva"; }
    @Override public double getCost() { return 30.0; }
    @Override public int getLevelRequired() { return 9; }

    @Override
    public void execute(Player player, PlayerData data) {

        // Buscar objetivo hostil más cercano
        LivingEntity objetivo = null;
        double distMin = Double.MAX_VALUE;
        com.tuusuario.mmo.eldencraft.EldenCraft plugin = org.bukkit.plugin.java.JavaPlugin.getPlugin(com.tuusuario.mmo.eldencraft.EldenCraft.class);
        for (Entity e : player.getNearbyEntities(3, 3, 3)) {
            if (!(e instanceof LivingEntity) || plugin.getPartyManager().isAlly(player, e)) continue;
            double d = e.getLocation().distanceSquared(player.getLocation());
            if (d < distMin) { distMin = d; objetivo = (LivingEntity) e; }
        }

        if (objetivo == null) {
            player.sendMessage("§c✖ No hay enemigos cerca para lanzar la toxina.");
            return;
        }

        final LivingEntity target = objetivo;
        boolean esNoMuerto = UNDEAD.contains(target.getType());

        // Bonus si ya está debilitado
        boolean yaDebilitado = target.hasPotionEffect(PotionEffectType.POISON)
                            || target.hasPotionEffect(PotionEffectType.WITHER);
        if (yaDebilitado) {
            double extraDmg = 6.0 + (data.getDexterity() * 0.3);
            target.damage(extraDmg, player);
            player.sendActionBar("§4§l¡TOXINA POTENCIADA! §r§7+" + (int) extraDmg + " dmg");
        }

        if (esNoMuerto) {
            // No-muertos son inmunes al Veneno: compensar con daño directo + Wither II
            double dmgVenenoEquivalente = 4.0; // ~Veneno II durante 5s simplificado
            target.damage(dmgVenenoEquivalente, player);
            target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER,   60, 1)); // Wither II, 3s
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 1)); // Lentitud II, 4s
            player.sendMessage("§2§l» §7Toxina adaptada a §cno-muerto§7: daño+Wither II+Lentitud.");
        } else {
            target.addPotionEffect(new PotionEffect(PotionEffectType.POISON,   100, 1)); // Veneno II, 5s
            target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER,    60, 0)); // Wither I, 3s
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,  80, 1)); // Lentitud II, 4s
            player.sendMessage("§2§l» §7Toxina Corrosiva: Veneno II + Wither I + Lentitud II.");
        }

        // Nube visual
        Location loc = target.getLocation().add(0, 0.1, 0);
        AreaEffectCloud nube = loc.getWorld().spawn(loc, AreaEffectCloud.class);
        nube.setRadius(1.5f);
        nube.setDuration(40);
        nube.setWaitTime(0);
        nube.setRadiusOnUse(0f);
        nube.setRadiusPerTick(-0.02f);
        nube.setColor(esNoMuerto ? Color.fromRGB(180, 50, 50) : Color.fromRGB(50, 200, 50));
        nube.clearCustomEffects();

        // Partículas y sonido
        target.getWorld().spawnParticle(Particle.WITCH,
                target.getLocation().add(0, target.getHeight() / 2.0, 0),
                30, 0.4, 0.6, 0.4, 0.1);
        player.playSound(player.getLocation(), Sound.ENTITY_WITCH_DRINK, 1f, 1.8f);
        player.playSound(player.getLocation(), Sound.ENTITY_SPLASH_POTION_BREAK, 0.8f, 0.6f);
    }
}
