package com.tuusuario.mmo.eldencraft.skills.rogue;

import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.Sound;
import org.bukkit.Particle;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

// Trampa Mortal: - Daño equivalente a Daño Instantáneo I (6 puntos = 3 corazones) + bonus por DEX.
public class TrampaMortal implements Skill {
    @Override public String getName() { return "Trampa Mortal"; }
    @Override public double getCost() { return 35.0; }
    @Override public int getLevelRequired() { return 6; }

    @Override
    public void execute(Player player, PlayerData data) {
        double damage = 6.0 + (data.getDexterity() * 0.4); // ~Instant Damage I
        final int[] hits = {0};
        player.getNearbyEntities(4, 4, 4).forEach(e -> {
            if (e instanceof LivingEntity && e != player) {
                LivingEntity v = (LivingEntity) e;
                v.damage(damage, player);
                v.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,  100, 2)); // Lentitud III, 5s
                v.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS,  100, 1)); // Debilidad II, 5s
                e.getWorld().spawnParticle(Particle.SQUID_INK, e.getLocation().add(0, 1, 0), 20);
                hits[0]++;
            }
        });
        player.playSound(player.getLocation(), Sound.BLOCK_TRIPWIRE_CLICK_ON, 1f, 0.5f);
        if (hits[0] > 0) {
            player.sendActionBar("§6⚙ Trampa activada §7— " + hits[0] + " enemigo(s) afectado(s).");
        } else {
            player.sendActionBar("§c✖ Trampa Mortal falló — ningún enemigo en rango (4 bloques).");
        }
    }
}
