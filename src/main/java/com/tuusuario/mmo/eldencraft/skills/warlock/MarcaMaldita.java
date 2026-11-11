package com.tuusuario.mmo.eldencraft.skills.warlock;

import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Comparator;
import java.util.Optional;

public class MarcaMaldita implements Skill {
    @Override public String getName() { return "Marca Maldita"; }
    @Override public double getCost() { return 20.0; }
    @Override public int getLevelRequired() { return 1; }

    @Override
    public void execute(Player player, PlayerData data) {
        com.tuusuario.mmo.eldencraft.EldenCraft plugin = org.bukkit.plugin.java.JavaPlugin.getPlugin(com.tuusuario.mmo.eldencraft.EldenCraft.class);
        Optional<Entity> target = player.getNearbyEntities(6, 6, 6).stream()
                .filter(e -> e instanceof LivingEntity && !plugin.getPartyManager().isAlly(player, e))
                .min(Comparator.comparingDouble(e -> e.getLocation().distanceSquared(player.getLocation())));

        if (target.isEmpty()) {
            player.sendMessage("§c✦ No hay enemigo cercano para marcar.");
            return;
        }

        LivingEntity mob = (LivingEntity) target.get();
        mob.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 1));
        mob.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 120, 1));
        mob.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 0));
        mob.damage(6.0 + data.getIntelligence() * 0.4, player);

        mob.getWorld().spawnParticle(Particle.ASH,
                mob.getLocation().add(0, 1, 0), 40, 0.4, 0.6, 0.4, 0.05);
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SHOOT, 0.8f, 1.3f);
        player.sendMessage("§8✦ Marca Maldita: §7¡" + mob.getType().name() + " marcado!");
    }
}
