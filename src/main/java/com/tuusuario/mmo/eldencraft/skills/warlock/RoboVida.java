package com.tuusuario.mmo.eldencraft.skills.warlock;

import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class RoboVida implements Skill {
    @Override public String getName() { return "Robo de Vida"; }
    @Override public double getCost() { return 25.0; }
    @Override public int getLevelRequired() { return 3; }

    @Override
    public void execute(Player player, PlayerData data) {
        double totalHealed = 0;
        double dmgPerMob = 5.0 + data.getIntelligence() * 0.3;

        com.tuusuario.mmo.eldencraft.EldenCraft plugin = org.bukkit.plugin.java.JavaPlugin.getPlugin(com.tuusuario.mmo.eldencraft.EldenCraft.class);
        for (Entity e : player.getNearbyEntities(6, 6, 6)) {
            if (!(e instanceof LivingEntity) || plugin.getPartyManager().isAlly(player, e)) continue;
            LivingEntity mob = (LivingEntity) e;
            mob.damage(dmgPerMob, player);
            totalHealed += dmgPerMob * 0.5;
            mob.getWorld().spawnParticle(Particle.HEART,
                    mob.getLocation().add(0, 1.5, 0), 3, 0.2, 0.2, 0.2, 0);
        }

        if (totalHealed > 0) {
            double maxHp = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            player.setHealth(Math.min(maxHp, player.getHealth() + totalHealed));
            player.getWorld().spawnParticle(Particle.HEART,
                    player.getLocation().add(0, 2, 0), 10, 0.4, 0.4, 0.4, 0);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 0.7f);
            player.sendMessage("§c✦ Robo de Vida: §7+" + String.format("%.1f", totalHealed) + " HP robados.");
        } else {
            player.sendMessage("§c✦ No hay enemigos cercanos para robar vida.");
        }
    }
}
