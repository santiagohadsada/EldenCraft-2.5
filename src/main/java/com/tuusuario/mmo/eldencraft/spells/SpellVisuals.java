package com.tuusuario.mmo.eldencraft.spells;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * Efectos visuales y sonoros por escuela de magia (SPEC 5.2 / F3).
 */
public class SpellVisuals {

    private SpellVisuals() {}

    /**
     * Reproduce efectos de lanzamiento en la ubicación del lanzador o del objetivo.
     */
    public static void playSchoolEffect(Player caster, Location targetLoc, String school) {
        Location loc = targetLoc != null ? targetLoc : caster.getLocation();
        String s = school != null ? school.toLowerCase() : "evocacion";

        switch (s) {
            case "evocacion" -> {
                caster.getWorld().spawnParticle(Particle.FLAME, loc.clone().add(0, 1, 0), 20, 0.4, 0.4, 0.4, 0.05);
                caster.getWorld().playSound(loc, Sound.ENTITY_BLAZE_SHOOT, 1.0f, 1.0f);
            }
            case "conjuracion" -> {
                caster.getWorld().spawnParticle(Particle.PORTAL, loc.clone().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.1);
                caster.getWorld().playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.2f);
            }
            case "abjuracion" -> {
                caster.getWorld().spawnParticle(Particle.CRIT, loc.clone().add(0, 1, 0), 25, 0.5, 0.5, 0.5, 0.1);
                caster.getWorld().playSound(loc, Sound.BLOCK_ANVIL_LAND, 0.6f, 1.8f);
            }
            case "transmutacion" -> {
                caster.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc.clone().add(0, 1, 0), 15, 0.4, 0.4, 0.4, 0.02);
                caster.getWorld().playSound(loc, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.2f);
            }
            case "necromancia" -> {
                caster.getWorld().spawnParticle(Particle.SMOKE, loc.clone().add(0, 1, 0), 25, 0.4, 0.4, 0.4, 0.05);
                caster.getWorld().playSound(loc, Sound.ENTITY_WITHER_AMBIENT, 0.8f, 1.4f);
            }
            case "ilusion" -> {
                caster.getWorld().spawnParticle(Particle.WITCH, loc.clone().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.05);
                caster.getWorld().playSound(loc, Sound.ENTITY_ENDERMAN_STARE, 0.7f, 1.5f);
            }
            case "encantamiento" -> {
                caster.getWorld().spawnParticle(Particle.HEART, loc.clone().add(0, 1, 0), 10, 0.4, 0.4, 0.4, 0.02);
                caster.getWorld().playSound(loc, Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.4f);
            }
            case "adivinacion" -> {
                caster.getWorld().spawnParticle(Particle.ENCHANT, loc.clone().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.1);
                caster.getWorld().playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.0f);
            }
            default -> {
                caster.getWorld().spawnParticle(Particle.ENCHANT, loc.clone().add(0, 1, 0), 15, 0.3, 0.3, 0.3, 0.05);
                caster.getWorld().playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 2.0f);
            }
        }
    }
}
