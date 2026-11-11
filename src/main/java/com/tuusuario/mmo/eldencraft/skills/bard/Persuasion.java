package com.tuusuario.mmo.eldencraft.skills.bard;

import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Boss;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

// Persuasion: encanta a los enemigos cercanos (debilidad + lentitud).
public class Persuasion implements Skill {

    private static final double RADIO = 8.0;

    @Override
    public String getName() { return "Persuasión"; }
    @Override
    public double getCost() { return 35.0; }
    @Override
    public int getLevelRequired() { return 9; }

    @Override
    public void execute(Player player, PlayerData data) {
        int total = 0;
        int jefes = 0;

        com.tuusuario.mmo.eldencraft.EldenCraft plugin = org.bukkit.plugin.java.JavaPlugin.getPlugin(com.tuusuario.mmo.eldencraft.EldenCraft.class);
        for (Entity e : player.getNearbyEntities(6, 6, 6)) {
            if (!(e instanceof LivingEntity) || plugin.getPartyManager().isAlly(player, e)) continue;
            LivingEntity v = (LivingEntity) e;
            double altura = v.getHeight();

            if (v instanceof Boss) {
                // Wither, Ender Dragon, Elder Guardian, Warden... inmunes a pociones.
                // Persuasion forzada: daño directo escalado con carisma.
                double dmg = 6.0 + data.getCharisma() * 0.4;
                v.damage(dmg, player);
                v.getWorld().spawnParticle(Particle.NOTE,
                        v.getLocation().add(0, altura + 0.5, 0), 14, 0.6, 0.4, 0.6, 1);
                v.getWorld().spawnParticle(Particle.ANGRY_VILLAGER,
                        v.getLocation().add(0, altura + 0.5, 0), 6, 0.4, 0.3, 0.4, 0);
                v.getWorld().playSound(v.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 0.7f);
                jefes++;
            } else {
                v.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 140, 2));
                v.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 140, 2));
                v.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 140, 1));
                v.getWorld().spawnParticle(Particle.HEART,
                        v.getLocation().add(0, altura + 0.3, 0), 5, 0.4, 0.3, 0.4, 0);
                v.getWorld().spawnParticle(Particle.NOTE,
                        v.getLocation().add(0, altura + 0.3, 0), 6, 0.5, 0.3, 0.5, 1);
            }
            total++;
        }

        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1f, 1f);
        if (total == 0) {
            player.sendMessage("§d[!] §7No hay nadie a quien persuadir.");
        } else if (jefes > 0) {
            player.sendMessage("§d[!] §7Persuades a §e" + total + "§7 enemigos §8(" + jefes + " jefe(s) reciben tu carisma a golpe de nota).");
        } else {
            player.sendMessage("§d[!] §7Persuades a §e" + total + "§7 enemigos.");
        }
    }
}
