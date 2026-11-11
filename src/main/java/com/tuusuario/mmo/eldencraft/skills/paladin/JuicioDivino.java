package com.tuusuario.mmo.eldencraft.skills.paladin;

import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import com.tuusuario.mmo.eldencraft.util.UndeadCheck;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.Sound;

// Juicio Divino: - Rayos sobre los enemigos cercanos.
public class JuicioDivino implements Skill {
    @Override public String getName() { return "Juicio Divino"; }
    @Override public double getCost() { return 60.0; }
    @Override public int getLevelRequired() { return 16; }

    @Override
    public void execute(Player player, PlayerData data) {
        double base = 14.0 + (data.getCharisma() * 0.6);
        player.getNearbyEntities(5, 5, 5).forEach(e -> {
            if (e instanceof LivingEntity && e != player) {
                double dmg = base * (UndeadCheck.isUndead(e) ? 2.0 : 1.0);
                ((LivingEntity) e).damage(dmg, player);
                player.getWorld().strikeLightningEffect(e.getLocation());
            }
        });
        player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1f, 1f);
    }
}
