package com.tuusuario.mmo.eldencraft.skills.rogue;

import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Sound;

public class EscapeRapidoRogue implements Skill {
    @Override
    public String getName() { return "Escape Rápido"; }
    @Override
    public double getCost() { return 25.0; }
    @Override
    public int getLevelRequired() { return 12; }

    @Override
    public void execute(Player player, PlayerData data) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 300, 2));
        player.playSound(player.getLocation(), Sound.ENTITY_RABBIT_JUMP, 1f, 1.5f);
    }
}