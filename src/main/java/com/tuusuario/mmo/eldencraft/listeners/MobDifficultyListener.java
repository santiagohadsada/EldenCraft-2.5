package com.tuusuario.mmo.eldencraft.listeners;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

/**
 * Sistema de dificultad dinámica de mobs (nivel máximo del jugador: 20).
 *
 *   Niv 1-5   → sin buff (mobs normales)
 *   Niv 6-10  → Tier I:  +30% HP, +10% daño
 *   Niv 11-15 → Tier II: +70% HP, +25% daño, +8% velocidad
 *   Niv 16-20 → Tier III: +120% HP, +45% daño, +15% velocidad
 */
public class MobDifficultyListener implements Listener {

    private static final double RADIO_BUSQUEDA = 64.0;

    private final EldenCraft plugin;

    public MobDifficultyListener(EldenCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Mob)) return;
        Mob mob = (Mob) entity;

        // No escalar jefes (tienen nombre custom)
        if (mob.getCustomName() != null) return;
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG) return;

        // Buscar jugador más cercano
        Player nearest = null;
        double minDistSq = RADIO_BUSQUEDA * RADIO_BUSQUEDA;
        for (Player p : event.getLocation().getWorld().getPlayers()) {
            double d = p.getLocation().distanceSquared(event.getLocation());
            if (d < minDistSq) { minDistSq = d; nearest = p; }
        }
        if (nearest == null) return;

        PlayerData data = plugin.getPlayerManager().getPlayerData(nearest.getUniqueId());
        if (data == null) return;

        int nivel = data.getLevel();
        if (nivel <= 5) return;

        double hpMult, dmgMult, speedAdd;
        String tierNombre;

        if (nivel <= 10) {
            hpMult = 1.30; dmgMult = 1.10; speedAdd = 0.0;
            tierNombre = "§7[I] ";
        } else if (nivel <= 15) {
            hpMult = 1.70; dmgMult = 1.25; speedAdd = 0.08;
            tierNombre = "§a[II] ";
        } else {
            hpMult = 2.20; dmgMult = 1.45; speedAdd = 0.15;
            tierNombre = "§e[III] ";
        }

        // HP
        AttributeInstance maxHp = mob.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHp != null) {
            double newHp = maxHp.getBaseValue() * hpMult;
            maxHp.setBaseValue(newHp);
            mob.setHealth(Math.min(newHp, maxHp.getValue()));
        }
        // Daño
        AttributeInstance atk = mob.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        if (atk != null) atk.setBaseValue(atk.getBaseValue() * dmgMult);
        // Velocidad
        if (speedAdd > 0) {
            AttributeInstance spd = mob.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
            if (spd != null) spd.setBaseValue(Math.min(0.6, spd.getBaseValue() * (1.0 + speedAdd)));
        }

        mob.setCustomName(tierNombre + ChatColor.RESET + formatNombre(mob));
        mob.setCustomNameVisible(false);
    }

    private String formatNombre(Mob mob) {
        String raw = mob.getType().name().replace("_", " ");
        StringBuilder sb = new StringBuilder();
        for (String w : raw.split(" ")) {
            if (!w.isEmpty())
                sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1).toLowerCase()).append(" ");
        }
        return sb.toString().trim();
    }
}
