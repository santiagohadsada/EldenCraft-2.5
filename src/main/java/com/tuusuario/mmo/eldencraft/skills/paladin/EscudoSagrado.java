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

public class EscudoSagrado implements Skill {
    @Override
    public String getName() { return "Escudo Sagrado"; }
    @Override
    public double getCost() { return 25.0; }
    @Override
    public int getLevelRequired() { return 3; }

    @Override
    public void execute(Player player, PlayerData data) {
        double shield = 5.0 + (data.getConstitution() * 0.3);
        player.setAbsorptionAmount(shield);
        player.playSound(player.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1f, 0.5f);

        EldenCraft plugin = JavaPlugin.getPlugin(EldenCraft.class);
        GlowEffect.apply(plugin, player, ChatColor.YELLOW, 20 * 30);
        player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER,
                player.getLocation().add(0, 1, 0), 30, 0.6, 1.0, 0.6, 0);
        player.getWorld().spawnParticle(Particle.END_ROD,
                player.getLocation().add(0, 1, 0), 15, 0.4, 0.8, 0.4, 0.05);
    }
}
