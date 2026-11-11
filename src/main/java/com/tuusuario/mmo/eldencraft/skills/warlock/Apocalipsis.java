package com.tuusuario.mmo.eldencraft.skills.warlock;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import com.tuusuario.mmo.eldencraft.util.GlowEffect;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Apocalipsis implements Skill {
    @Override public String getName() { return "Apocalipsis"; }
    @Override public double getCost() { return 100.0; }
    @Override public int getLevelRequired() { return 20; }

    @Override
    public void execute(Player player, PlayerData data) {
        EldenCraft plugin = JavaPlugin.getPlugin(EldenCraft.class);
        double baseDmg = 15.0 + data.getIntelligence() * 0.7;

        // Glow rojo demoníaco
        GlowEffect.apply(plugin, player, ChatColor.RED, 200);

        // Darkness momentánea al jugador (efecto dramático)
        player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 40, 0, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 200, 2));

        // Sonidos del apocalipsis
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.8f, 0.8f);
        player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 0.6f, 0.5f);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.7f, 0.6f);

        // Explosión oscura inicial
        player.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER,
                player.getLocation().add(0, 1, 0), 10, 2.0, 1.0, 2.0, 0);
        player.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME,
                player.getLocation().add(0, 0.5, 0), 60, 2.0, 0.3, 2.0, 0.08);
        player.getWorld().spawnParticle(Particle.ASH,
                player.getLocation().add(0, 2, 0), 100, 4.0, 3.0, 4.0, 0.1);
        player.getWorld().spawnParticle(Particle.SCULK_SOUL,
                player.getLocation().add(0, 1, 0), 30, 1.5, 1.0, 1.5, 0.1);

        // Anillo de fuego del alma
        Particle.DustOptions bloodRed = new Particle.DustOptions(Color.fromRGB(139, 0, 0), 2.0f);
        for (int i = 0; i < 36; i++) {
            double angle = i * (2 * Math.PI / 36);
            double x = 3.5 * Math.cos(angle);
            double z = 3.5 * Math.sin(angle);
            player.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME,
                    player.getLocation().add(x, 0.2, z), 2, 0, 0, 0, 0);
            player.getWorld().spawnParticle(Particle.DUST,
                    player.getLocation().add(x, 0.5, z), 2, 0, 0, 0, 0, bloodRed);
        }

        player.sendMessage("§4§l✦ ¡APOCALIPSIS! §7¡El caos se desata!");

        // 3 oleadas con efectos crecientes
        for (int wave = 0; wave < 3; wave++) {
            final int w = wave;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Location loc = player.getLocation();

                // Sonido de oleada
                player.getWorld().playSound(loc, Sound.ENTITY_WARDEN_SONIC_BOOM, 0.4f, 0.6f + w * 0.3f);

                for (Entity e : player.getNearbyEntities(10 + w * 2, 10, 10 + w * 2)) {
                    if (!(e instanceof LivingEntity) || plugin.getPartyManager().isAlly(player, e)) continue;
                    LivingEntity mob = (LivingEntity) e;
                    mob.damage(baseDmg * (1.0 - w * 0.15), player);
                    mob.getWorld().strikeLightningEffect(mob.getLocation());
                    // Partículas en cada mob golpeado
                    mob.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME,
                            mob.getLocation().add(0, 1, 0), 15, 0.3, 0.5, 0.3, 0.05);
                    mob.getWorld().spawnParticle(Particle.ASH,
                            mob.getLocation().add(0, 1, 0), 20, 0.5, 1.0, 0.5, 0.1);
                }

                // Onda de fuego del alma expandiéndose
                player.getWorld().spawnParticle(Particle.FLAME, loc.clone().add(0, 1, 0),
                        60 + w * 30, 3.0 + w, 1.0, 3.0 + w, 0.1);
                player.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, loc.clone().add(0, 0.5, 0),
                        40 + w * 20, 2.0 + w, 0.5, 2.0 + w, 0.05);
            }, w * 15L);
        }

        // Aura oscura residual
        Bukkit.getScheduler().runTaskTimer(plugin, new java.util.function.Consumer<org.bukkit.scheduler.BukkitTask>() {
            int ticks = 0;
            @Override
            public void accept(org.bukkit.scheduler.BukkitTask task) {
                if (ticks++ > 200 || !player.isOnline()) {
                    task.cancel();
                    return;
                }
                if (ticks % 5 == 0) {
                    player.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME,
                            player.getLocation().add(0, 0.5, 0), 3, 0.5, 0.3, 0.5, 0.02);
                    player.getWorld().spawnParticle(Particle.ASH,
                            player.getLocation().add(0, 1, 0), 5, 0.8, 1.0, 0.8, 0);
                }
            }
        }, 0L, 1L);
    }
}
