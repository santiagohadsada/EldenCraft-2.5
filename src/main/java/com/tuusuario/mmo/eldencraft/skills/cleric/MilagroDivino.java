package com.tuusuario.mmo.eldencraft.skills.cleric;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import com.tuusuario.mmo.eldencraft.util.GlowEffect;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class MilagroDivino implements Skill {
    @Override public String getName() { return "Milagro Divino"; }
    @Override public double getCost() { return 100.0; }
    @Override public int getLevelRequired() { return 20; }

    @Override
    public void execute(Player player, PlayerData data) {
        EldenCraft plugin = JavaPlugin.getPlugin(EldenCraft.class);
        double healAmount = 16.0 + data.getWisdom() * 0.8;
        int healed = 0;

        // Glow blanco celestial
        GlowEffect.apply(plugin, player, ChatColor.WHITE, 100);

        double maxHp = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        player.setHealth(Math.min(maxHp, player.getHealth() + healAmount));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 300, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 200, 2));
        healed++;

        for (Entity e : player.getNearbyEntities(12, 12, 12)) {
            if (!(e instanceof Player ally) || !plugin.getPartyManager().canHeal(player, ally)) continue;
            
            double allyMax = ally.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            ally.setHealth(Math.min(allyMax, ally.getHealth() + healAmount));
            ally.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 300, 1));
            ally.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 200, 1));
            ally.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 60, 0, false, false));
            ally.removePotionEffect(PotionEffectType.POISON);
            ally.removePotionEffect(PotionEffectType.WITHER);

            // Beam celestial en cada aliado
            ally.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, ally.getLocation().add(0, 1, 0), 50, 0.5, 1.0, 0.5, 0.15);
            ally.getWorld().spawnParticle(Particle.END_ROD, ally.getLocation().add(0, 2, 0), 20, 0.2, 1.5, 0.2, 0.05);
            ally.getWorld().spawnParticle(Particle.ENCHANT, ally.getLocation().add(0, 0.5, 0), 30, 0.5, 1.0, 0.5, 0.8);
            ally.sendMessage("§f§l✦ ¡MILAGRO DIVINO! §7" + player.getName() + " te ha curado.");
            healed++;
        }

        // Sonidos celestiales
        player.getWorld().playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.5f, 1.5f);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 0.8f);

        // Columna de luz celestial
        player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation().add(0, 1, 0), 200, 4.0, 2.0, 4.0, 0.25);
        player.getWorld().spawnParticle(Particle.FLASH, player.getLocation().add(0, 2, 0), 3);

        Particle.DustOptions holyGold = new Particle.DustOptions(Color.fromRGB(255, 223, 128), 2.0f);
        for (int y = 0; y < 8; y++) {
            player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0, y, 0), 10, 0.2, 0.1, 0.2, 0.03);
            player.getWorld().spawnParticle(Particle.DUST, player.getLocation().add(0, y, 0), 8, 0.3, 0.1, 0.3, 0, holyGold);
        }
        player.getWorld().spawnParticle(Particle.ENCHANT, player.getLocation().add(0, 1, 0), 100, 3.0, 2.0, 3.0, 1.5);

        player.sendMessage("§f§l✦ ¡MILAGRO DIVINO! §7+" + String.format("%.1f", healAmount)
                + " HP a " + healed + " aliado(s).");
    }
}
