package com.tuusuario.mmo.eldencraft.skills.gunslinger;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Rafaga implements Skill {
    @Override public String getName() { return "Ráfaga"; }
    @Override public double getCost() { return 30.0; }
    @Override public int getLevelRequired() { return 3; }

    @Override
    public void execute(Player player, PlayerData data) {
        EldenCraft plugin = JavaPlugin.getPlugin(EldenCraft.class);
        player.sendMessage("§e✦ Ráfaga: §7¡3 disparos rápidos!");
        
        for (int i = 0; i < 3; i++) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    Arrow arrow = player.launchProjectile(Arrow.class);
                    arrow.setVelocity(player.getLocation().getDirection().multiply(2.5));
                    arrow.setDamage(arrow.getDamage() + (data.getDexterity() * 0.15));
                    player.playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1f, 1.5f);
                }
            }, i * 5L);
        }
    }
}
