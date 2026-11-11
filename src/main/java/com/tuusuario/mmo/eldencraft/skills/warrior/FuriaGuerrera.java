package com.tuusuario.mmo.eldencraft.skills.warrior;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import com.tuusuario.mmo.eldencraft.util.BalanceState;
import com.tuusuario.mmo.eldencraft.util.GlowEffect;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

// Slot 5 del Guerrero - "Postura de Combate": Inmunidad absoluta de 8 segundos.
public class FuriaGuerrera implements Skill {
    @Override public String getName() { return "Postura de Combate"; }
    @Override public double getCost() { return 40.0; }
    @Override public int getLevelRequired() { return 12; }

    @Override
    public void execute(Player player, PlayerData data) {
        EldenCraft plugin = JavaPlugin.getPlugin(EldenCraft.class);
        int duration = 160; // 8 segundos

        // Inmunidad absoluta: RESISTANCE V cancela todo el daño
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, duration, 4, false, true, true));
        player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_IRON, 1f, 0.8f);
        player.playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_HURT, 1f, 1.5f);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                player.sendMessage(ChatColor.GRAY + "✦ Tu postura de combate termina.");
            }
        }, duration);

        GlowEffect.apply(plugin, player, ChatColor.WHITE, duration);
        player.getWorld().spawnParticle(Particle.END_ROD,
                player.getLocation().add(0, 1, 0), 40, 0.5, 1.0, 0.5, 0.05);
        player.getWorld().spawnParticle(Particle.CRIT,
                player.getLocation().add(0, 1, 0), 20, 0.4, 0.8, 0.4, 0);
        player.sendMessage("§f§l» §7Postura de §b§lCombate§r§7: §a¡Inmunidad absoluta durante 8s!");
    }
}
