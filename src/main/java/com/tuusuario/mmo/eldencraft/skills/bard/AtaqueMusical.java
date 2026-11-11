package com.tuusuario.mmo.eldencraft.skills.bard;

import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.Sound;
import org.bukkit.Particle;

public class AtaqueMusical implements Skill {
    @Override public String getName() { return "Ataque Musical"; }
    @Override public double getCost() { return 45.0; }
    @Override public int getLevelRequired() { return 16; }

    @Override public CastType getCastType() { return CastType.CHARGEABLE; }
    @Override public int getMaxChargeTicks() { return 40; }

    @Override
    public void execute(Player player, PlayerData data) {
        execute(player, data, 1.0);
    }

    @Override
    public void execute(Player player, PlayerData data, double charge) {
        double chargeMult = 0.3 + (charge * 1.7); // 30% a 200%
        // Daño base y escalado nerfeados
        double damage = (6.0 + (data.getCharisma() * 0.3)) * chargeMult;
        player.getNearbyEntities(7 * chargeMult, 7, 7 * chargeMult).forEach(e -> {
            if (e instanceof LivingEntity target && e != player) {
                target.damage(damage, player);
                target.getWorld().spawnParticle(Particle.NOTE,
                        target.getLocation().add(0, 1, 0), (int)(8 * chargeMult));
            }
        });
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 1f, 0.5f);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, (float)(2.0 - charge));
        player.sendMessage("§d♫ Ataque Musical (Carga: " + (int)(charge*100) + "%)");
    }
}
