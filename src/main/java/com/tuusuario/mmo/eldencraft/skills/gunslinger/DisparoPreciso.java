package com.tuusuario.mmo.eldencraft.skills.gunslinger;

import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;

public class DisparoPreciso implements Skill {
    @Override public String getName() { return "Disparo Preciso"; }
    @Override public double getCost() { return 20.0; }
    @Override public int getLevelRequired() { return 1; }

    @Override
    public void execute(Player player, PlayerData data) {
        Arrow arrow = player.launchProjectile(Arrow.class);
        arrow.setVelocity(player.getLocation().getDirection().multiply(4.0));
        arrow.setDamage(arrow.getDamage() + 4.0 + (data.getDexterity() * 0.3));
        arrow.setCritical(true);
        player.playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1f, 2f);
        player.sendMessage("§e✦ Disparo Preciso.");
    }
}
