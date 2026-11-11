package com.tuusuario.mmo.eldencraft.skills.ranger;

import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.Sound;

public class FlechaPrecisa implements Skill {
    @Override
    public String getName() { return "Flecha Precisa"; }
    @Override
    public double getCost() { return 20.0; }
    @Override
    public int getLevelRequired() { return 3; }

    @Override
    public void execute(Player player, PlayerData data) {
        Arrow arrow = player.launchProjectile(Arrow.class);
        // Daño de la flecha nerfeado para que no mate zombies nivel 3 de un golpe
        double extraDamage = (data.getDexterity() * 0.3) + (data.getWisdom() * 0.1);
        arrow.setDamage(2.0 + extraDamage);
        arrow.setCritical(true);
        player.playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1f, 1.5f);
    }
}