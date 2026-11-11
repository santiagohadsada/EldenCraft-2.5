package com.tuusuario.mmo.eldencraft.skills.ranger;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Wither;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

// Trampa Natural: - Coloca una trampa real durante 30s.
public class TrampaCaza implements Skill {

    private static final double RADIO_TRIGGER = 2.5;
    private static final int DURACION_SEG = 30;

    @Override public String getName() { return "Trampa Natural"; }
    @Override public double getCost() { return 30.0; }
    @Override public int getLevelRequired() { return 9; }

    @Override
    public void execute(Player player, PlayerData data) {
        EldenCraft plugin = JavaPlugin.getPlugin(EldenCraft.class);
        final Location trampa = player.getLocation().clone().add(0, 0.05, 0);
        final long expira = System.currentTimeMillis() + (DURACION_SEG * 1000L);
        final double bonus = data.getDexterity() * 0.4;

        player.playSound(trampa, Sound.BLOCK_TRIPWIRE_ATTACH, 1f, 1.2f);
        player.sendMessage("§a[!] §7Has colocado una §2trampa natural§7 (30s).");
        Particle.DustOptions verde = new Particle.DustOptions(Color.fromRGB(80, 180, 60), 1.4f);

        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                ticks++;
                if (System.currentTimeMillis() > expira) {
                    trampa.getWorld().spawnParticle(Particle.SMOKE,
                            trampa.clone().add(0, 0.2, 0), 10);
                    cancel();
                    return;
                }
                if (ticks % 2 == 0) {
                    for (int i = 0; i < 8; i++) {
                        double ang = 2 * Math.PI * i / 8.0;
                        double x = Math.cos(ang) * 1.2;
                        double z = Math.sin(ang) * 1.2;
                        trampa.getWorld().spawnParticle(Particle.DUST,
                                trampa.clone().add(x, 0.05, z), 1, 0, 0, 0, 0, verde);
                    }
                }
                for (Entity e : trampa.getWorld().getNearbyEntities(
                        trampa, RADIO_TRIGGER, 2.0, RADIO_TRIGGER)) {
                    if (e == player) continue;
                    if (esHostil(e)) {
                        LivingEntity v = (LivingEntity) e;
                        v.damage(5.0 + bonus, player);
                        // Lentitud III + Debilidad I (5s)
                        v.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 2));
                        v.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 0));
                        v.getWorld().spawnParticle(Particle.CRIT,
                                v.getLocation().add(0, 1, 0), 30, 0.4, 0.6, 0.4, 0.05);
                        v.getWorld().playSound(v.getLocation(),
                                Sound.BLOCK_TRIPWIRE_CLICK_ON, 1f, 0.7f);
                        if (player.isOnline()) {
                            player.sendMessage("§a[!] §7Tu trampa atrapó a §e" + v.getName() + "§7.");
                        }
                        cancel();
                        return;
                    }
                }
            }
        }.runTaskTimer(plugin, 5L, 5L);
    }

    private boolean esHostil(Entity e) {
        return e instanceof Monster || e instanceof Slime || e instanceof Phantom
                || e instanceof Ghast || e instanceof EnderDragon || e instanceof Wither;
    }
}
