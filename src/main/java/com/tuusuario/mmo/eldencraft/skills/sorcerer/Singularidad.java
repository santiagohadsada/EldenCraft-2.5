package com.tuusuario.mmo.eldencraft.skills.sorcerer;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import com.tuusuario.mmo.eldencraft.util.GlowEffect;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.bukkit.plugin.java.JavaPlugin;

public class Singularidad implements Skill {
    @Override public String getName() { return "Singularidad"; }
    @Override public double getCost() { return 80.0; }
    @Override public int getLevelRequired() { return 20; }

    @Override
    public void execute(Player player, PlayerData data) {
        EldenCraft plugin = JavaPlugin.getPlugin(EldenCraft.class);

        // Glow púrpura oscuro
        GlowEffect.apply(plugin, player, ChatColor.DARK_PURPLE, 60);

        int pulled = 0;
        for (Entity e : player.getNearbyEntities(12, 12, 12)) {
            if (!(e instanceof LivingEntity) || plugin.getPartyManager().isAlly(player, e)) continue;
            Vector dir = player.getLocation().toVector()
                    .subtract(e.getLocation().toVector()).normalize();
            dir.setY(Math.max(0.2, dir.getY()));
            e.setVelocity(dir.multiply(2.0));
            ((LivingEntity) e).damage(8.0 + data.getIntelligence() * 0.5, player);
            pulled++;
        }

        org.bukkit.Location center = player.getLocation().add(0, 1, 0);

        // Ítem visual flotante inagarrable
        org.bukkit.entity.ItemDisplay enderPearl = player.getWorld().spawn(center, org.bukkit.entity.ItemDisplay.class);
        enderPearl.setItemStack(new org.bukkit.inventory.ItemStack(org.bukkit.Material.ENDER_PEARL));
        enderPearl.setBillboard(org.bukkit.entity.Display.Billboard.CENTER);
        enderPearl.setGlowing(true);
        enderPearl.setGlowColorOverride(Color.PURPLE);

        // Sonidos épicos
        player.playSound(center, Sound.ENTITY_WARDEN_SONIC_BOOM, 0.6f, 0.4f);
        player.playSound(center, Sound.ENTITY_ENDER_DRAGON_GROWL, 0.8f, 1.2f);
        player.playSound(center, Sound.BLOCK_BEACON_ACTIVATE, 1.5f, 0.5f);

        // Explosión inicial de partículas de agujero negro
        player.getWorld().spawnParticle(Particle.REVERSE_PORTAL, center, 200, 4.0, 2.0, 4.0, 0.3);
        player.getWorld().spawnParticle(Particle.DRAGON_BREATH, center, 80, 3.0, 1.5, 3.0, 0.1);
        player.getWorld().spawnParticle(Particle.SCULK_SOUL, center, 40, 2.0, 1.0, 2.0, 0.1);

        // Vórtice continuo con efecto blackhole
        Particle.DustOptions voidPurple = new Particle.DustOptions(Color.fromRGB(60, 0, 120), 2.5f);
        Particle.DustOptions voidBlack = new Particle.DustOptions(Color.fromRGB(10, 0, 20), 3.0f);

        Bukkit.getScheduler().runTaskTimer(plugin, new java.util.function.Consumer<org.bukkit.scheduler.BukkitTask>() {
            int ticks = 0;
            @Override
            public void accept(org.bukkit.scheduler.BukkitTask task) {
                if (ticks++ > 60 || !player.isOnline()) {
                    task.cancel();
                    enderPearl.remove();
                    // Implosión final
                    player.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, center, 3);
                    player.getWorld().spawnParticle(Particle.FLASH, center, 2);
                    player.getWorld().spawnParticle(Particle.SCULK_SOUL, center, 60, 2.0, 1.5, 2.0, 0.2);
                    player.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1f, 0.8f);
                    player.playSound(center, Sound.ENTITY_WARDEN_SONIC_BOOM, 0.5f, 0.3f);
                    return;
                }

                // Transformación progresiva para que cierre exactamente en el centro al terminar
                double radius = 4.0 - (ticks * (4.0 / 60.0));
                for (int s = 0; s < 2; s++) {
                    double angle = ticks * 0.5 + s * Math.PI;
                    double x = radius * Math.cos(angle);
                    double z = radius * Math.sin(angle);
                    player.getWorld().spawnParticle(Particle.PORTAL,
                            center.clone().add(x, 0, z), 5, 0, 0, 0, 0);
                    player.getWorld().spawnParticle(Particle.WITCH,
                            center.clone().add(-x, 0, -z), 5, 0, 0, 0, 0);
                    player.getWorld().spawnParticle(Particle.DUST,
                            center.clone().add(x * 0.5, 0.5, z * 0.5), 2, 0, 0, 0, 0, voidPurple);
                }

                // Partículas de DRAGON_BREATH + SCULK_SOUL en el centro
                if (ticks % 5 == 0) {
                    player.getWorld().spawnParticle(Particle.DRAGON_BREATH, center, 10, 1.0, 0.5, 1.0, 0.05);
                    player.getWorld().spawnParticle(Particle.SCULK_SOUL, center, 5, 0.5, 0.3, 0.5, 0.03);
                    player.getWorld().spawnParticle(Particle.DUST, center, 8, 1.5, 1.0, 1.5, 0, voidBlack);
                }

                // Atraer enemigos
                for (Entity e : player.getNearbyEntities(8, 8, 8)) {
                    if (!(e instanceof LivingEntity) || plugin.getPartyManager().isAlly(player, e)) continue;
                    Vector dir = center.toVector().subtract(e.getLocation().toVector()).normalize();
                    e.setVelocity(dir.multiply(0.4));
                }
            }
        }, 0L, 1L);

        player.sendMessage("§5§l✦ ¡SINGULARIDAD! §7" + pulled + " enemigos atrapados en el vórtice!");
    }
}
