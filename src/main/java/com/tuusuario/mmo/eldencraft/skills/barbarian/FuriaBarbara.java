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

public class FuriaBarbara implements Skill {
    @Override public String getName() { return "Furia Bárbara"; }
    @Override public double getCost() { return 30.0; }
    @Override public int getLevelRequired() { return 3; }

    @Override
    public void execute(Player player, PlayerData data) {
        // Reducido: máximo STR I (antes podía dar STR II por escalado)
        int duration = 240; // 12s
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, duration, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, duration, 0));
        player.playSound(player.getLocation(), Sound.ENTITY_WOLF_GROWL, 1f, 0.5f);

        EldenCraft plugin = JavaPlugin.getPlugin(EldenCraft.class);
        GlowEffect.apply(plugin, player, ChatColor.RED, duration);

        Particle.DustOptions red = new Particle.DustOptions(Color.RED, 1.5f);
        player.getWorld().spawnParticle(Particle.DUST,
                player.getLocation().add(0, 1, 0), 40, 0.6, 1.0, 0.6, 0, red);
        player.getWorld().spawnParticle(Particle.ANGRY_VILLAGER,
                player.getLocation().add(0, 2, 0), 5);

        player.sendMessage(ChatColor.RED + "§l[!] §c¡La furia bárbara te invade!");
    }
}
