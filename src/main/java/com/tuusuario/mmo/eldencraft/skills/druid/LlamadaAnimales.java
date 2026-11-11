package com.tuusuario.mmo.eldencraft.skills.druid;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.plugin.java.JavaPlugin;

public class LlamadaAnimales implements Skill {
    @Override public String getName() { return "Llamada de Animales"; }
    @Override public double getCost() { return 50.0; }
    @Override public int getLevelRequired() { return 12; }
    @Override public int getCooldown() { return 180; } // 3 mins

    @Override
    public void execute(Player player, PlayerData data) {
        EldenCraft plugin = JavaPlugin.getPlugin(EldenCraft.class);

        for (int i = 0; i < 3; i++) {
            Wolf wolf = player.getWorld().spawn(
                    player.getLocation().clone().add((i - 1) * 2.0, 0, 1.0), Wolf.class);
            wolf.setTamed(true);
            wolf.setOwner(player);
            wolf.setCustomName(ChatColor.GREEN + "✦ Lobo Druida");
            wolf.setCustomNameVisible(true);
            wolf.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(30.0);
            wolf.setHealth(30.0);
            wolf.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(
                    4.0 + data.getWisdom() * 0.35);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (wolf.isValid() && !wolf.isDead()) wolf.remove();
            }, 2400L);
        }

        player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER,
                player.getLocation().add(0, 1, 0), 30, 1.5, 0.5, 1.5, 0);
        player.playSound(player.getLocation(), Sound.ENTITY_WOLF_HOWL, 1f, 0.8f);
        player.sendMessage("§2✦ Llamada de Animales: §7¡3 Lobos Druidas te protegen por 2 min!");
    }
}
