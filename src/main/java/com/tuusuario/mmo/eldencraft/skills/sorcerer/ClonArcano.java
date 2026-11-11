package com.tuusuario.mmo.eldencraft.skills.sorcerer;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ClonArcano implements Skill {
    @Override public String getName() { return "Clon Arcano"; }
    @Override public double getCost() { return 80.0; }
    @Override public int getLevelRequired() { return 16; }
    @Override public int getCooldown() { return 180; } // 3 mins

    @Override
    public void execute(Player player, PlayerData data) {
        EldenCraft plugin = JavaPlugin.getPlugin(EldenCraft.class);
        ArmorStand clone = player.getWorld().spawn(player.getLocation(), ArmorStand.class);
        clone.setVisible(false);
        clone.setBasePlate(false);
        clone.setGravity(false);
        clone.setCustomName(ChatColor.DARK_PURPLE + "✦ Clon Arcano");
        clone.setCustomNameVisible(true);
        if (player.getInventory().getHelmet() != null) clone.getEquipment().setHelmet(player.getInventory().getHelmet());
        else clone.getEquipment().setHelmet(new org.bukkit.inventory.ItemStack(org.bukkit.Material.AMETHYST_BLOCK));
        clone.getEquipment().setChestplate(player.getInventory().getChestplate());
        clone.getEquipment().setLeggings(player.getInventory().getLeggings());
        clone.getEquipment().setBoots(player.getInventory().getBoots());
        clone.getEquipment().setItemInMainHand(player.getInventory().getItemInMainHand());

        // Bloquear todos los slots para que no se puedan quitar los ítems
        clone.addEquipmentLock(org.bukkit.inventory.EquipmentSlot.HEAD, ArmorStand.LockType.REMOVING_OR_CHANGING);
        clone.addEquipmentLock(org.bukkit.inventory.EquipmentSlot.CHEST, ArmorStand.LockType.REMOVING_OR_CHANGING);
        clone.addEquipmentLock(org.bukkit.inventory.EquipmentSlot.LEGS, ArmorStand.LockType.REMOVING_OR_CHANGING);
        clone.addEquipmentLock(org.bukkit.inventory.EquipmentSlot.FEET, ArmorStand.LockType.REMOVING_OR_CHANGING);
        clone.addEquipmentLock(org.bukkit.inventory.EquipmentSlot.HAND, ArmorStand.LockType.REMOVING_OR_CHANGING);
        clone.addEquipmentLock(org.bukkit.inventory.EquipmentSlot.OFF_HAND, ArmorStand.LockType.REMOVING_OR_CHANGING);

        player.getWorld().spawnParticle(Particle.PORTAL,
                clone.getLocation().add(0, 1, 0), 40, 0.3, 0.6, 0.3, 0.2);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1.5f);
        player.sendMessage("§5✦ Clon Arcano: §7¡Tu clon te protege durante el combate!");

        Bukkit.getScheduler().runTaskTimer(plugin, task -> {
            if (!clone.isValid() || clone.isDead() || !player.isOnline()) {
                task.cancel();
                if (clone.isValid()) clone.remove();
                return;
            }
            
            LivingEntity target = null;
            Entity playerTarget = player.getTargetEntity(15);
            if (playerTarget instanceof LivingEntity && !plugin.getPartyManager().isAlly(player, playerTarget)) {
                target = (LivingEntity) playerTarget;
            }
            if (target == null) {
                for (Entity e : player.getNearbyEntities(10, 10, 10)) {
                    if (e instanceof org.bukkit.entity.Mob mob && mob.getTarget() != null && mob.getTarget().equals(player)) {
                        target = mob;
                        break;
                    }
                }
            }

            if (target != null) {
                org.bukkit.util.Vector dir = target.getLocation().add(0, 1, 0).toVector().subtract(clone.getLocation().add(0, 1.5, 0).toVector()).normalize();
                org.bukkit.entity.Snowball snowball = clone.getWorld().spawn(clone.getLocation().add(0, 1.5, 0).add(dir), org.bukkit.entity.Snowball.class);
                snowball.setVelocity(dir.multiply(1.5));
                snowball.setShooter(player);
                snowball.setMetadata("arcane_mote", new org.bukkit.metadata.FixedMetadataValue(plugin, data.getIntelligence()));
                clone.getWorld().playSound(clone.getLocation(), Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1f, 1.5f);
                clone.getWorld().spawnParticle(Particle.WITCH, clone.getLocation().add(0, 1.5, 0), 10, 0.2, 0.2, 0.2, 0.05);
            }
        }, 0L, 20L);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (clone.isValid() && !clone.isDead()) {
                clone.getWorld().spawnParticle(Particle.PORTAL,
                        clone.getLocation().add(0, 1, 0), 20, 0.2, 0.4, 0.2, 0.1);
                clone.remove();
            }
        }, 600L); // 30 seconds
    }
}
