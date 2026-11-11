package com.tuusuario.mmo.eldencraft.skills.druid;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Bee;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.metadata.FixedMetadataValue;

public class FrenesiNatural implements Skill {
    @Override public String getName() { return "Tótem de Vida"; }
    @Override public double getCost() { return 35.0; }
    @Override public int getLevelRequired() { return 6; }
    @Override public int getCooldown() { return 60; } // 1 minuto

    @Override
    public void execute(Player player, PlayerData data) {
        EldenCraft plugin = JavaPlugin.getPlugin(EldenCraft.class);

        // creamos el totem (un armorstand invisible con glowstone en la cabeza) (ArmorStand invisible flotando con un bloque brillante en la cabeza)
        org.bukkit.Location loc = player.getLocation().add(0, 1.5, 0);
        org.bukkit.entity.ArmorStand totem = player.getWorld().spawn(loc, org.bukkit.entity.ArmorStand.class);
        totem.setVisible(false);
        totem.setGravity(false);
        totem.setInvulnerable(true);
        totem.setSmall(true);
        totem.setCustomName(ChatColor.GREEN + "✦ Tótem de Vida");
        totem.setCustomNameVisible(true);
        totem.getEquipment().setHelmet(new org.bukkit.inventory.ItemStack(org.bukkit.Material.GLOWSTONE));

        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1.2f);
        player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, loc, 50, 0.5, 0.5, 0.5, 0.1);
        player.sendMessage("§2✦ Tótem de Vida: §7Has invocado un tótem curativo por 15 segundos.");

        double healAmount = 2.0 + (data.getWisdom() * 0.2);

        Bukkit.getScheduler().runTaskTimer(plugin, task -> {
            if (!totem.isValid() || totem.isDead() || totem.getTicksLived() > 300) { // 15s * 20 = 300 ticks
                task.cancel();
                if (totem.isValid()) {
                    totem.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, totem.getLocation(), 20, 0.5, 0.5, 0.5, 0);
                    totem.getWorld().playSound(totem.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1f, 1f);
                    totem.remove();
                }
                return;
            }

            // lo hacemos rotar de a poquito
            org.bukkit.Location tLoc = totem.getLocation();
            tLoc.setYaw(tLoc.getYaw() + 10);
            totem.teleport(tLoc);

            // particulas cada un rato
            if (totem.getTicksLived() % 10 == 0) {
                totem.getWorld().spawnParticle(Particle.COMPOSTER, tLoc.add(0, 1, 0), 10, 1.5, 0.5, 1.5, 0);
            }

            // cura cada segundo (20 ticks = 1 seg)
            if (totem.getTicksLived() % 20 == 0) {
                totem.getWorld().playSound(tLoc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.5f);
                for (org.bukkit.entity.Entity e : totem.getNearbyEntities(8, 8, 8)) {
                    if (e instanceof Player aliado) {
                        // chequeamos si es aliado o el mismo jugador
                        boolean canHeal = plugin.getPartyManager().canHeal(player, aliado);
                        if (canHeal) {
                            double maxHp = aliado.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
                            aliado.setHealth(Math.min(maxHp, aliado.getHealth() + healAmount));
                            aliado.getWorld().spawnParticle(Particle.HEART, aliado.getLocation().add(0, 2, 0), 1);
                        }
                    }
                }
            }
        }, 0L, 1L);
    }
}
