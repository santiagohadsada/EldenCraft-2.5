package com.tuusuario.mmo.eldencraft.skills.rogue;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import com.tuusuario.mmo.eldencraft.util.BalanceState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

// Golpe Sigiloso (slot 1): Marca el SIGUIENTE ataque normal del pícaro como CRÍTICO (x2 de daño).
public class GolpeSigiloso implements Skill {
    @Override public String getName() { return "Golpe Sigiloso"; }
    @Override public double getCost() { return 15.0; }
    @Override public int getLevelRequired() { return 1; }

    @Override
    public void execute(Player player, PlayerData data) {
        EldenCraft plugin = JavaPlugin.getPlugin(EldenCraft.class);
        BalanceState.rogueCritNext.put(player.getUniqueId(), 2.0);
        player.sendMessage(ChatColor.GRAY + "§l» §7Tu próximo golpe será un §e§lCRÍTICO§r§7 (x2).");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 1.5f);
        player.getWorld().spawnParticle(Particle.CRIT,
                player.getLocation().add(0, 1, 0), 15, 0.4, 0.8, 0.4, 0);

        // Caduca a los 10s si no se usó
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Double v = BalanceState.rogueCritNext.get(player.getUniqueId());
            if (v != null && v == 2.0) BalanceState.rogueCritNext.remove(player.getUniqueId());
        }, 200L);
    }
}
