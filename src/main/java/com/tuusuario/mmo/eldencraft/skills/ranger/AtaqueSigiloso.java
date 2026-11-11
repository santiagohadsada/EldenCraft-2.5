package com.tuusuario.mmo.eldencraft.skills.ranger;

import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

// Marca del Cazador: revela a todos los enemigos hostiles cercanos con brillo (GLOWING) durante 30 segundos.
public class AtaqueSigiloso implements Skill {

    private static final int RADIO = 20;
    private static final int DURACION_TICKS = 600;

    @Override
    public String getName() { return "Marca del Cazador"; }
    @Override
    public double getCost() { return 40.0; }
    @Override
    public int getLevelRequired() { return 16; }

    @Override
    public void execute(Player player, PlayerData data) {
        int marcados = 0;
        for (Entity e : player.getNearbyEntities(RADIO, RADIO, RADIO)) {
            if (esHostil(e) && e != player) {
                LivingEntity m = (LivingEntity) e;
                m.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, DURACION_TICKS, 0, false, false));
                m.getWorld().spawnParticle(Particle.END_ROD,
                        m.getLocation().add(0, 1, 0), 8, 0.3, 0.5, 0.3, 0.02);
                marcados++;
            }
        }

        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.6f);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 1.4f);
        player.getWorld().spawnParticle(Particle.WAX_ON,
                player.getLocation().add(0, 1, 0), 30, 0.6, 1.0, 0.6, 0.05);

        if (marcados == 0) {
            player.sendMessage("§a[!] §7No hay enemigos hostiles a tu alrededor.");
        } else {
            player.sendMessage("§a[!] §7Has marcado §e" + marcados + "§7 enemigos. Los verás brillar 30s.");
        }
    }

    private boolean esHostil(Entity e) {
        return e instanceof Monster
                || e instanceof Slime
                || e instanceof Phantom
                || e instanceof Ghast
                || e instanceof EnderDragon
                || e instanceof Wither;
    }
}
