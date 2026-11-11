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

// slot 7 del guerrero - "Contraataque": Habilidad técnica (no burst).
public class RabiaCiega implements Skill {
    @Override public String getName() { return "Contraataque"; }
    @Override public double getCost() { return 60.0; }
    @Override public int getLevelRequired() { return 20; }

    @Override
    public void execute(Player player, PlayerData data) {
        EldenCraft plugin = JavaPlugin.getPlugin(EldenCraft.class);
        int duration = 240; // 12s

        BalanceState.warriorParry.add(player.getUniqueId());
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            BalanceState.warriorParry.remove(player.getUniqueId());
            if (player.isOnline()) {
                player.sendMessage(ChatColor.GRAY + "✦ Tu postura de contraataque termina.");
            }
        }, duration);

        GlowEffect.apply(plugin, player, ChatColor.WHITE, duration);
        player.getWorld().spawnParticle(Particle.SWEEP_ATTACK,
                player.getLocation().add(0, 1, 0), 15, 0.6, 0.8, 0.6, 0);
        player.getWorld().spawnParticle(Particle.END_ROD,
                player.getLocation().add(0, 1, 0), 40, 0.5, 1.0, 0.5, 0.08);
        player.getWorld().spawnParticle(Particle.ENCHANT,
                player.getLocation().add(0, 1, 0), 60, 1.0, 1.5, 1.0, 1.0);
        player.getWorld().spawnParticle(Particle.FLASH,
                player.getLocation().add(0, 1, 0), 1);
        player.playSound(player.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1f, 0.8f);
        player.playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_REPAIR, 0.8f, 1.2f);
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.5f, 1.5f);
        player.sendMessage("§f§l» §7Postura de §b§lContraataque§r§7: §areflejas el 50% del daño durante 12s.");

        Bukkit.getScheduler().runTaskTimer(plugin, new java.util.function.Consumer<org.bukkit.scheduler.BukkitTask>() {
            int ticks = 0;
            @Override
            public void accept(org.bukkit.scheduler.BukkitTask task) {
                if (ticks++ > duration || !player.isOnline()) {
                    task.cancel();
                    return;
                }
                if (ticks % 8 == 0) {
                    player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, player.getLocation().add(0, 1, 0), 2, 0.5, 0.5, 0.5, 0);
                    player.getWorld().spawnParticle(Particle.ENCHANT, player.getLocation().add(0, 0.5, 0), 8, 0.5, 0.8, 0.5, 0.5);
                }
            }
        }, 0L, 1L);
    }
}
