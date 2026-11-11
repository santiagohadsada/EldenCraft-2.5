package com.tuusuario.mmo.eldencraft.skills.druid;

import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class EnredaRaices implements Skill {
    @Override public String getName() { return "Enreda Raíces"; }
    @Override public double getCost() { return 20.0; }
    @Override public int getLevelRequired() { return 1; }

    @Override
    public void execute(Player player, PlayerData data) {
        int enredados = 0;
        com.tuusuario.mmo.eldencraft.EldenCraft plugin = org.bukkit.plugin.java.JavaPlugin.getPlugin(com.tuusuario.mmo.eldencraft.EldenCraft.class);
        for (Entity e : player.getNearbyEntities(7, 7, 7)) {
            if (!(e instanceof LivingEntity) || plugin.getPartyManager().isAlly(player, e)) continue;
            LivingEntity mob = (LivingEntity) e;
            mob.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 5, false, false));
            mob.damage(3.0 + data.getWisdom() * 0.3, player);
            mob.getWorld().spawnParticle(Particle.COMPOSTER,
                    mob.getLocation().add(0, 0.5, 0), 15, 0.3, 0.3, 0.3, 0.05);
            enredados++;
        }
        player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER,
                player.getLocation().add(0, 1, 0), 20, 2.0, 0.5, 2.0, 0);
        player.playSound(player.getLocation(), Sound.BLOCK_GRASS_BREAK, 1f, 0.7f);
        player.sendMessage("§2✦ Enreda Raíces: §7" + enredados + " enemigos enraizados.");
    }
}
