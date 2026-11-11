package com.tuusuario.mmo.eldencraft.listeners;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.models.ClassType;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import com.tuusuario.mmo.eldencraft.util.SorcererState;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

// pasivas de las clases nuevas:
//  - monje: no puede usar espada, con mano vacia pega mas
//  - hechicero: cargas arcanas al recibir daño
//  - brujo: los wither skulls no rompen bloques
public class NewClassesListener implements Listener {

    private final EldenCraft plugin;

    public NewClassesListener(EldenCraft plugin) {
        this.plugin = plugin;
    }

    // === monje: restriccion de armas ===

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMonkAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data == null || data.getPlayerClass() != ClassType.MONK) return;

        Material held = player.getInventory().getItemInMainHand().getType();
        String name = held.name();

        if (name.endsWith("_SWORD")) {
            event.setCancelled(true);
            player.sendActionBar("§c✦ El Monje no puede usar espadas (Camino del Puño).");
        } else if (name.endsWith("_AXE")) {
            event.setDamage(event.getDamage() * 0.15);
        } else if (held == Material.AIR || held == Material.STICK) {
            double bonus = event.getDamage() * 2.0 + data.getDexterity() * 0.4;
            event.setDamage(bonus);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 30, 1, false, false));
            player.getWorld().spawnParticle(Particle.CRIT,
                    event.getEntity().getLocation().add(0, 1, 0), 5, 0.2, 0.2, 0.2, 0.05);
        }
    }

    // === hechicero: cargas arcanas al recibir daño ===──

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSorcererDamaged(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data == null || data.getPlayerClass() != ClassType.SORCERER) return;
        if (event.getFinalDamage() <= 0) return;

        UUID uuid = player.getUniqueId();
        int charges = SorcererState.arcaneCharges.merge(uuid, 1, Integer::sum);

        if (charges >= 5) {
            SorcererState.arcanaReady.add(uuid);
            SorcererState.arcaneCharges.put(uuid, 0);
            player.sendActionBar("§5§l✦ ¡REACCIÓN ARCANA lista! Usa una habilidad.");
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 0.8f, 1.8f);
        } else {
            player.sendActionBar("§5✦ Carga arcana: " + charges + "/5");
        }
    }

    // === brujo: q las calaveras no rompan bloques ===────

    @EventHandler(ignoreCancelled = true)
    public void onWitherSkullExplode(EntityExplodeEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof WitherSkull)) return;
        if (SorcererState.safeWitherSkulls.remove(entity.getUniqueId())) {
            event.blockList().clear();
        }
    }
}
