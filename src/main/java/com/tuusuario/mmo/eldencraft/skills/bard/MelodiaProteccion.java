package com.tuusuario.mmo.eldencraft.skills.bard;

import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Sound;

public class MelodiaProteccion implements Skill {
    @Override
    public String getName() { return "Melodía de Protección"; }
    @Override
    public double getCost() { return 25.0; }
    @Override
    public int getLevelRequired() { return 3; }

    @Override
    public void execute(Player player, PlayerData data) {
        int power = Math.max(0, data.getCharisma() / 15);
        com.tuusuario.mmo.eldencraft.EldenCraft plugin = org.bukkit.plugin.java.JavaPlugin.getPlugin(com.tuusuario.mmo.eldencraft.EldenCraft.class);
        for (Entity e : player.getNearbyEntities(7, 7, 7)) {
            if (e instanceof Player ally && plugin.getPartyManager().canHeal(player, ally)) {
                ally.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 200, power));
            }
        }
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_GUITAR, 1f, 0.8f);
    }
}