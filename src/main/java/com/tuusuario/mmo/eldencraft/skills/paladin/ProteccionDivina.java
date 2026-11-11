package com.tuusuario.mmo.eldencraft.skills.paladin;

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

public class ProteccionDivina implements Skill {
    @Override
    public String getName() { return "Protección Divina"; }
    @Override
    public double getCost() { return 35.0; }
    @Override
    public int getLevelRequired() { return 9; }

    @Override
    public void execute(Player player, PlayerData data) {
        int resistance = Math.max(0, (data.getConstitution() / 15));
        int duration = 200;
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, duration, resistance));
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1.5f);

        EldenCraft plugin = JavaPlugin.getPlugin(EldenCraft.class);
        GlowEffect.apply(plugin, player, ChatColor.YELLOW, duration);
        player.getWorld().spawnParticle(Particle.END_ROD,
                player.getLocation().add(0, 1, 0), 30, 0.5, 1.0, 0.5, 0.05);
    }
}
