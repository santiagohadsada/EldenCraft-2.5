package com.tuusuario.mmo.eldencraft.skills.monk;

import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Comparator;
import java.util.Optional;

public class PatadaViento implements Skill {
    @Override public String getName() { return "Patada Viento"; }
    @Override public double getCost() { return 25.0; }
    @Override public int getLevelRequired() { return 6; }

    @Override
    public void execute(Player player, PlayerData data) {
        com.tuusuario.mmo.eldencraft.EldenCraft plugin = org.bukkit.plugin.java.JavaPlugin.getPlugin(com.tuusuario.mmo.eldencraft.EldenCraft.class);
        Optional<Entity> opt = player.getNearbyEntities(5, 5, 5).stream()
                .filter(e -> e instanceof LivingEntity && !plugin.getPartyManager().isAlly(player, e))
                .min(Comparator.comparingDouble(e -> e.getLocation().distanceSquared(player.getLocation())));

        if (opt.isEmpty()) {
            player.sendMessage("§c✦ No hay objetivo cercano.");
            return;
        }
        LivingEntity target = (LivingEntity) opt.get();
        Vector dir = target.getLocation().toVector()
                .subtract(player.getLocation().toVector()).normalize();
        dir.setY(0.5);
        target.setVelocity(dir.multiply(2.8));
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 2));
        target.damage(8.0 + data.getDexterity() * 0.5, player);

        target.getWorld().spawnParticle(Particle.SWEEP_ATTACK,
                target.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 1.6f);
        player.sendMessage("§e✦ Patada Viento: §7¡Objetivo lanzado y ralentizado!");
    }
}
