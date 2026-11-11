package com.tuusuario.mmo.eldencraft.skills.sorcerer;

import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class PulsoExpansivo implements Skill {
    @Override public String getName() { return "Pulso Expansivo"; }
    @Override public double getCost() { return 20.0; }
    @Override public int getLevelRequired() { return 1; }

    @Override public CastType getCastType() { return CastType.CHARGEABLE; }
    @Override public int getMaxChargeTicks() { return 30; } // 1.5 segundos

    @Override
    public void execute(Player player, PlayerData data) {
        execute(player, data, 1.0);
    }

    @Override
    public void execute(Player player, PlayerData data, double charge) {
        int pushed = 0;
        com.tuusuario.mmo.eldencraft.EldenCraft plugin = org.bukkit.plugin.java.JavaPlugin.getPlugin(com.tuusuario.mmo.eldencraft.EldenCraft.class);
        double chargeMult = 0.5 + (charge * 1.5); // 50% a 200%
        for (Entity e : player.getNearbyEntities(6 * chargeMult, 6, 6 * chargeMult)) {
            if (!(e instanceof LivingEntity) || plugin.getPartyManager().isAlly(player, e)) continue;
            Vector dir = e.getLocation().toVector()
                    .subtract(player.getLocation().toVector()).normalize();
            dir.setY(0.35);
            e.setVelocity(dir.multiply((2.5 + data.getIntelligence() * 0.05) * chargeMult));
            ((LivingEntity) e).damage((4.0 + data.getIntelligence() * 0.3) * chargeMult, player);
            pushed++;
        }
        player.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER,
                player.getLocation().add(0, 1, 0), (int)(3 * chargeMult), 1.5, 0.5, 1.5, 0);
        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.6f, 1.8f);
        player.sendMessage("§b✦ Pulso Expansivo: §7" + pushed + " enemigos repelidos. (Carga: " + (int)(charge*100) + "%)");
    }
}
