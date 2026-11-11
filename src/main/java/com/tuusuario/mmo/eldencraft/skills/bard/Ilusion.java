package com.tuusuario.mmo.eldencraft.skills.bard;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Slot 5 del Bardo - "Ilusión":
 * Crea 3 clones del jugador (armor stands con su skin y equipo).
 * Los mobs hostiles cercanos redirigen su ataque hacia los clones.
 * Los clones son INVULNERABLES pero desaparecen al cabo de 8s.
 * No sueltan ningún ítem al desaparecer o al recibir daño.
 * Costo: 40 energía | Nivel 12
 */
public class Ilusion implements Skill {

    private static final double RADIO_SPAWN  = 2.2;
    private static final double RADIO_TARGET = 20.0;
    private static final int    DURACION     = 160;  // 8 segundos en ticks
    private static final int    CLONES       = 3;

    @Override public String getName()       { return "Ilusión"; }
    @Override public double getCost()       { return 40.0; }
    @Override public int getLevelRequired() { return 12; }
    @Override public int getCooldown()      { return 180; } // 3 mins

    @Override
    public void execute(Player player, PlayerData data) {
        EldenCraft plugin = JavaPlugin.getPlugin(EldenCraft.class);

        // le ponemos la cabeza con tu skin
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) { meta.setOwningPlayer(player); head.setItemMeta(meta); }

        final Set<UUID>       cloneIds = new HashSet<>();
        final List<ArmorStand> clones  = new ArrayList<>();
        Location centro = player.getLocation();

        for (int i = 0; i < CLONES; i++) {
            double ang = (2 * Math.PI * i / CLONES) + (Math.random() * 0.4);
            double r   = RADIO_SPAWN + Math.random() * 0.5;
            Location loc = centro.clone().add(Math.cos(ang) * r, 0, Math.sin(ang) * r);
            loc.setYaw((float) Math.toDegrees(-ang) + 90f);

            ArmorStand stand = centro.getWorld().spawn(loc, ArmorStand.class, as -> {
                as.setVisible(true);
                as.setArms(true);
                as.setBasePlate(false);
                as.setGravity(false);
                as.setSmall(false);
                as.setInvulnerable(true);      // No reciben daño de ningún tipo
                as.setCanPickupItems(false);
                as.setCustomName("§d" + player.getName());
                as.setCustomNameVisible(true);

                EntityEquipment eq = as.getEquipment();
                if (eq != null) {
                    eq.setHelmet(head.clone());
                    ItemStack chest = player.getInventory().getChestplate();
                    ItemStack legs  = player.getInventory().getLeggings();
                    ItemStack boots = player.getInventory().getBoots();
                    ItemStack hand  = player.getInventory().getItemInMainHand();
                    eq.setChestplate(chest != null ? chest.clone() : null);
                    eq.setLeggings  (legs  != null ? legs.clone()  : null);
                    eq.setBoots     (boots != null ? boots.clone() : null);
                    eq.setItemInMainHand(hand != null ? hand.clone() : null);
                    // ArmorStand no es Mob → setDropChance() lanza IllegalArgumentException.
                    // Los drops ya están bloqueados por setInvulnerable(true) + el listener de EntityDeathEvent.
                }
            });

            clones.add(stand);
            cloneIds.add(stand.getUniqueId());

            stand.getWorld().spawnParticle(Particle.PORTAL,
                    loc.clone().add(0, 1, 0), 35, 0.3, 0.8, 0.3, 0.6);
            stand.getWorld().spawnParticle(Particle.WITCH,
                    loc.clone().add(0, 1, 0), 15, 0.3, 0.7, 0.3, 0.05);
        }

        // Listener temporal: bloquea daño y drops por si acaso (doble seguro)
        Listener guard = new Listener() {
            @EventHandler(priority = EventPriority.HIGHEST)
            public void onDeath(EntityDeathEvent e) {
                if (cloneIds.contains(e.getEntity().getUniqueId())) {
                    e.getDrops().clear();
                    e.setDroppedExp(0);
                }
            }
            @EventHandler(priority = EventPriority.HIGHEST)
            public void onDamage(EntityDamageByEntityEvent e) {
                if (cloneIds.contains(e.getEntity().getUniqueId()))
                    e.setCancelled(true);
            }
        };
        plugin.getServer().getPluginManager().registerEvents(guard, plugin);

        player.playSound(centro, Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 1f, 1f);
        player.playSound(centro, Sound.ENTITY_ILLUSIONER_CAST_SPELL, 0.7f, 1.2f);
        player.sendMessage("§d§l[!] §r§7Tres ilusiones tuyas confunden a los enemigos §d(8s)§7.");

        // hacemos q los mobs lo miren cada 5 ticks
        new BukkitRunnable() {
            int ticks = 0;
            @Override public void run() {
                Iterator<ArmorStand> it = clones.iterator();
                while (it.hasNext()) {
                    ArmorStand s = it.next();
                    if (!s.isValid()) { cloneIds.remove(s.getUniqueId()); it.remove(); }
                }
                if (clones.isEmpty() || ticks >= DURACION) { cancel(); return; }
                ticks += 5;

                for (Entity e : player.getWorld().getNearbyEntities(
                        player.getLocation(), RADIO_TARGET, RADIO_TARGET, RADIO_TARGET)) {
                    if (!(e instanceof Mob mob)) continue;
                    if (!(mob.getTarget() instanceof ArmorStand)) {
                        ArmorStand pick = clones.get((int)(Math.random() * clones.size()));
                        if (pick.isValid()) try { mob.setTarget(pick); } catch (Throwable ignored) {}
                    }
                }
            }
        }.runTaskTimer(plugin, 5L, 5L);

        // Despawn al terminar
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            HandlerList.unregisterAll(guard);
            for (ArmorStand s : clones) {
                if (s.isValid()) {
                    s.getWorld().spawnParticle(Particle.POOF, s.getLocation().add(0, 1, 0), 25, 0.3, 0.8, 0.3, 0.05);
                    s.getWorld().playSound(s.getLocation(), Sound.ENTITY_ILLUSIONER_DEATH, 0.6f, 1.4f);
                    s.remove();
                }
            }
            cloneIds.clear();
            if (player.isOnline()) player.sendMessage("§7§o[Las ilusiones se disipan...]");
        }, DURACION);
    }
}
