package com.tuusuario.mmo.eldencraft.skills.druid;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import com.tuusuario.mmo.eldencraft.util.GlowEffect;
import org.bukkit.ChatColor;
import org.bukkit.Color;

public class AvatarPrimigenio implements Skill {
    @Override public String getName() { return "Avatar Primigenio"; }
    @Override public double getCost() { return 100.0; }
    @Override public int getLevelRequired() { return 20; }
    @Override public int getCooldown() { return 210; } // 3:30 mins

    private static final java.util.Map<java.util.UUID, Long> beamCooldowns = new java.util.HashMap<>();
    private static final java.util.Map<java.util.UUID, org.bukkit.boss.BossBar> wardenBars = new java.util.HashMap<>();

    @Override
    public void execute(Player player, PlayerData data) {
        EldenCraft plugin = JavaPlugin.getPlugin(EldenCraft.class);

        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 3000, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 3000, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 3000, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 3000, 0));

        // brillo verde del warden
        GlowEffect.apply(plugin, player, ChatColor.DARK_GREEN, 3000);

        player.getWorld().spawnParticle(Particle.SCULK_SOUL,
                player.getLocation().add(0, 1, 0), 100, 1.2, 1.8, 1.2, 0.15);
        player.getWorld().spawnParticle(Particle.WARPED_SPORE,
                player.getLocation().add(0, 1, 0), 80, 2.0, 2.0, 2.0, 0.1);
        player.getWorld().spawnParticle(Particle.FLASH,
                player.getLocation().add(0, 1, 0), 2);
        player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_ROAR, 1f, 0.7f);
        player.playSound(player.getLocation(), Sound.ENTITY_RAVAGER_ROAR, 0.7f, 0.6f);
        player.playSound(player.getLocation(), Sound.BLOCK_SCULK_CATALYST_BLOOM, 1f, 0.8f);
        player.sendMessage("§2§l✦ ¡AVATAR PRIMIGENIO! §7Agáchate y haz Click Izquierdo para disparar el Rayo.");

        player.setMetadata("warden_form", new org.bukkit.metadata.FixedMetadataValue(plugin, data.getWisdom()));

        org.bukkit.boss.BossBar bar = Bukkit.createBossBar("§2§lRayo del Warden: ¡LISTO!", org.bukkit.boss.BarColor.GREEN, org.bukkit.boss.BarStyle.SOLID);
        bar.addPlayer(player);
        wardenBars.put(player.getUniqueId(), bar);
        beamCooldowns.put(player.getUniqueId(), 0L);

        Bukkit.getScheduler().runTaskTimer(plugin, new java.util.function.Consumer<BukkitTask>() {
            int ticks = 0;
            @Override
            public void accept(BukkitTask task) {
                if (!player.isOnline() || ticks > 3000 || !player.hasMetadata("warden_form")) {
                    task.cancel();
                    if (player.isOnline()) {
                        player.removePotionEffect(PotionEffectType.STRENGTH);
                        player.removePotionEffect(PotionEffectType.RESISTANCE);
                        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
                        player.removePotionEffect(PotionEffectType.SLOWNESS);
                        player.getWorld().spawnParticle(Particle.SCULK_SOUL,
                                player.getLocation().add(0, 1, 0), 30, 0.5, 1.0, 0.5, 0.1);
                        player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_DIG, 0.8f, 1.0f);
                        player.sendMessage("§7✦ El Avatar Primigenio se disipa. Vuelves a tu forma.");
                        player.removeMetadata("warden_form", plugin);
                    }
                    org.bukkit.boss.BossBar b = wardenBars.remove(player.getUniqueId());
                    if (b != null) b.removeAll();
                    return;
                }
                
                // Actualizar BossBar
                long lastBeam = beamCooldowns.getOrDefault(player.getUniqueId(), 0L);
                long elapsed = System.currentTimeMillis() - lastBeam;
                if (elapsed < 10000) {
                    double progress = elapsed / 10000.0;
                    bar.setProgress(Math.min(1.0, progress));
                    bar.setTitle("§cRayo del Warden en recarga... (" + (10 - (elapsed / 1000)) + "s)");
                    bar.setColor(org.bukkit.boss.BarColor.RED);
                } else {
                    if (bar.getColor() == org.bukkit.boss.BarColor.RED) {
                        bar.setProgress(1.0);
                        bar.setTitle("§2§lRayo del Warden: ¡LISTO! (Shift + Click Izq)");
                        bar.setColor(org.bukkit.boss.BarColor.GREEN);
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
                    }
                }

                if (ticks % 8 == 0) {
                    player.getWorld().spawnParticle(Particle.SCULK_SOUL,
                            player.getLocation().add(0, 1, 0), 5, 0.5, 0.8, 0.5, 0.05);
                    player.getWorld().spawnParticle(Particle.WARPED_SPORE,
                            player.getLocation().add(0, 1, 0), 3, 0.4, 0.6, 0.4, 0.02);
                }
                ticks++;
            }
        }, 0L, 1L);
    }

    public static void shootBeam(Player player) {
        long lastBeam = beamCooldowns.getOrDefault(player.getUniqueId(), 0L);
        if (System.currentTimeMillis() - lastBeam < 10000) return;

        beamCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        EldenCraft plugin = JavaPlugin.getPlugin(EldenCraft.class);
        double wisdom = player.getMetadata("warden_form").get(0).asDouble();
        double sonicDmg = 12.0 + wisdom * 0.6;

        org.bukkit.util.Vector dir = player.getLocation().getDirection().normalize();
        org.bukkit.Location start = player.getLocation().add(0, 1.5, 0);

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1.0f, 1.0f);

        // Raycast
        for (int i = 0; i < 20; i++) {
            org.bukkit.Location point = start.clone().add(dir.clone().multiply(i));
            player.getWorld().spawnParticle(Particle.SONIC_BOOM, point, 1, 0, 0, 0, 0);
            
            for (Entity e : point.getWorld().getNearbyEntities(point, 1.5, 1.5, 1.5)) {
                if (e instanceof LivingEntity && !e.equals(player) && !plugin.getPartyManager().isAlly(player, e)) {
                    ((LivingEntity) e).damage(sonicDmg, player);
                }
            }
        }
    }
}
