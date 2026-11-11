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

public class TormentaEspinas implements Skill {
    @Override public String getName() { return "Tormenta de Espinas"; }
    @Override public double getCost() { return 40.0; }
    @Override public int getLevelRequired() { return 16; }

    @Override
    public void execute(Player player, PlayerData data) {
        int hit = 0;
        double dmg = 8.0 + data.getWisdom() * 0.5;
        com.tuusuario.mmo.eldencraft.EldenCraft plugin = org.bukkit.plugin.java.JavaPlugin.getPlugin(com.tuusuario.mmo.eldencraft.EldenCraft.class);
        for (Entity e : player.getNearbyEntities(5, 5, 5)) {
            if (!(e instanceof LivingEntity) || plugin.getPartyManager().isAlly(player, e)) continue;
            LivingEntity mob = (LivingEntity) e;
            mob.damage(dmg, player);
            mob.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 1));
            mob.getWorld().spawnParticle(Particle.COMPOSTER,
                    mob.getLocation().add(0, 1, 0), 20, 0.3, 0.5, 0.3, 0.1);
            hit++;
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 200, 2, false, false));
        com.tuusuario.mmo.eldencraft.util.BalanceState.druidThorns.put(player.getUniqueId(), System.currentTimeMillis() + 10000);
        player.getWorld().spawnParticle(Particle.COMPOSTER,
                player.getLocation().add(0, 1, 0), 50, 2.0, 1.0, 2.0, 0.15);
        player.playSound(player.getLocation(), Sound.BLOCK_WOOD_BREAK, 1f, 0.6f);
        player.sendMessage("§2✦ Tormenta de Espinas: §7" + hit + " enemigos dañados. Thorns activo 10s.");
    }
}
