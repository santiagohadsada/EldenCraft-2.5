package com.tuusuario.mmo.eldencraft.skills.barbarian;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import com.tuusuario.mmo.eldencraft.util.GlowEffect;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class RabiaCiegaBarbarian implements Skill {
    @Override public String getName() { return "Rabia Ciega"; }
    @Override public double getCost() { return 40.0; }
    @Override public int getLevelRequired() { return 9; }

    @Override
    public void execute(Player player, PlayerData data) {
        int duration = 300; // 15s
        // ahora da fuerza 1, lo otro estaba muy op
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, duration, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, 0));
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 0.5f);

        EldenCraft plugin = JavaPlugin.getPlugin(EldenCraft.class);
        GlowEffect.apply(plugin, player, ChatColor.RED, duration);

        Particle.DustOptions red = new Particle.DustOptions(Color.RED, 1.8f);
        player.getWorld().spawnParticle(Particle.DUST,
                player.getLocation().add(0, 1, 0), 60, 0.8, 1.2, 0.8, 0, red);
        player.sendMessage(ChatColor.DARK_RED + "§l[!] §cTu vista se enrojece de furia.");
    }
}
