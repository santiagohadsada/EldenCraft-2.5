package com.tuusuario.mmo.eldencraft.skills.bard;

import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Sound;

public class RitmoVelocidad implements Skill {
    @Override
    public String getName() { return "Ritmo de Velocidad"; }
    @Override
    public double getCost() { return 20.0; }
    @Override
    public int getLevelRequired() { return 6; }

    @Override
    public void execute(Player player, PlayerData data) {
        com.tuusuario.mmo.eldencraft.EldenCraft plugin = org.bukkit.plugin.java.JavaPlugin.getPlugin(com.tuusuario.mmo.eldencraft.EldenCraft.class);
        for (Entity e : player.getNearbyEntities(6, 6, 6)) {
            if (e instanceof Player ally && plugin.getPartyManager().canHeal(player, ally)) {
                ally.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1));
            }
        }
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_FLUTE, 1f, 1.2f);
    }
}