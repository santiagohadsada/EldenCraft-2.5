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

// Ataque Sorpresa (slot 6): Marca el SIGUIENTE ataque normal del pícaro como DOBLE CRÍTICO (x4 de daño).
public class AtaqueSorpresa implements Skill {
    @Override public String getName() { return "Ataque Sorpresa"; }
    @Override public double getCost() { return 45.0; }
    @Override public int getLevelRequired() { return 16; }

    @Override
    public void execute(Player player, PlayerData data) {
        EldenCraft plugin = JavaPlugin.getPlugin(EldenCraft.class);
        BalanceState.rogueCritNext.put(player.getUniqueId(), 4.0);
        player.sendMessage(ChatColor.DARK_RED + "§l» §cTu próximo golpe será un §4§l¡DOBLE CRÍTICO!§r§c (x4).");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 1f, 0.5f);
        player.getWorld().spawnParticle(Particle.LARGE_SMOKE,
                player.getLocation().add(0, 1, 0), 20, 0.5, 0.8, 0.5, 0.05);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Double v = BalanceState.rogueCritNext.get(player.getUniqueId());
            if (v != null && v == 4.0) BalanceState.rogueCritNext.remove(player.getUniqueId());
        }, 200L);
    }
}
