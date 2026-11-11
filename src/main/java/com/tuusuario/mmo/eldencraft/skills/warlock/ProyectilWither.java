package com.tuusuario.mmo.eldencraft.skills.warlock;

import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import com.tuusuario.mmo.eldencraft.util.SorcererState;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkull;

public class ProyectilWither implements Skill {
    @Override public String getName() { return "Proyectil de Wither"; }
    @Override public double getCost() { return 30.0; }
    @Override public int getLevelRequired() { return 6; }

    @Override
    public void execute(Player player, PlayerData data) {
        WitherSkull skull = player.launchProjectile(WitherSkull.class);
        skull.setCharged(false);
        skull.setYield(0f);
        SorcererState.safeWitherSkulls.add(skull.getUniqueId());

        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SHOOT, 1f, 1f);
        player.sendMessage("§8✦ Proyectil de Wither: §7¡Cabeza lanzada!");
    }
}
