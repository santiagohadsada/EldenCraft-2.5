package com.tuusuario.mmo.eldencraft.skills.barbarian;

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

// Protección Bárbara (slot 6): - Resistencia fuerte (mín.
public class ProteccionBarbara implements Skill {
    @Override public String getName() { return "Protección Bárbara"; }
    @Override public double getCost() { return 45.0; }
    @Override public int getLevelRequired() { return 16; }

    @Override
    public void execute(Player player, PlayerData data) {
        int res = Math.min(3, 2 + (data.getConstitution() / 25)); // mínimo II, máx IV
        int duration = 500; // 25s
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE,    duration, res));
        player.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST,  duration, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, duration, 0));
        player.setHealth(Math.min(player.getAttribute(
                org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue(),
                player.getHealth() + 4.0));
        player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 1f, 0.6f);
        player.sendMessage("§e§l» §6Tu piel se endurece como roca. §7(Resistencia + vida extra)");

        EldenCraft plugin = JavaPlugin.getPlugin(EldenCraft.class);
        GlowEffect.apply(plugin, player, ChatColor.GOLD, duration);
        player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER,
                player.getLocation().add(0, 1, 0), 25, 0.6, 1.0, 0.6, 0);
    }
}
