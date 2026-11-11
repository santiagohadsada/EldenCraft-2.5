package com.tuusuario.mmo.eldencraft.skills.warrior;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import com.tuusuario.mmo.eldencraft.util.GlowEffect;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class DefensaSolida implements Skill {
    public String getName() { return "Defensa Sólida"; }
    public double getCost() { return 20.0; }
    public int getLevelRequired() { return 3; }

    public void execute(Player player, PlayerData data) {
        int resistance = Math.max(0, (data.getStrength() / 15));
        int duration = 100;
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, duration, resistance));
        player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_IRON, 1f, 1f);

        EldenCraft plugin = JavaPlugin.getPlugin(EldenCraft.class);
        GlowEffect.apply(plugin, player, ChatColor.WHITE, duration);
        player.getWorld().spawnParticle(Particle.CRIT,
                player.getLocation().add(0, 1, 0), 15, 0.4, 0.8, 0.4, 0);
    }
}
