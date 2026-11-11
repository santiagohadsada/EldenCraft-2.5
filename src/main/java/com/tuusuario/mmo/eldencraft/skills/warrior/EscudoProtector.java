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

public class EscudoProtector implements Skill {
    public String getName() { return "Escudo Protector"; }
    public double getCost() { return 30.0; }
    public int getLevelRequired() { return 9; }

    public void execute(Player player, PlayerData data) {
        double amount = 5.0 + (data.getStrength() / 5.0);
        player.setAbsorptionAmount(amount);
        player.playSound(player.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1f, 1f);

        EldenCraft plugin = JavaPlugin.getPlugin(EldenCraft.class);
        GlowEffect.apply(plugin, player, ChatColor.WHITE, 20 * 30);
        player.getWorld().spawnParticle(Particle.CRIT,
                player.getLocation().add(0, 1, 0), 30, 0.6, 1.0, 0.6, 0);
    }
}
