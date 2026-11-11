package com.tuusuario.mmo.eldencraft.skills.paladin;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import com.tuusuario.mmo.eldencraft.util.BalanceState;
import com.tuusuario.mmo.eldencraft.util.GlowEffect;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

// Slot 7 del Paladín - "Tótem Divino": Al activarla, durante los próximos 2 minutos,
// si el paladín o un aliado dentro de 30 bloques recibe daño letal, lo cancela y lo cura al 50% de HP.
// Cooldown: 1 minuto (configurado en AbilityListener).
public class Resurreccion implements Skill {

    // Duración activa del tótem: 2 minutos
    private static final long DURACION_MS = 5L * 60L * 1000L;

    @Override public String getName() { return "Tótem Divino"; }
    @Override public double getCost() { return 100.0; }
    @Override public int getLevelRequired() { return 20; }

    @Override
    public void execute(Player player, PlayerData data) {
        EldenCraft plugin = JavaPlugin.getPlugin(EldenCraft.class);

        BalanceState.paladinTotem.put(player.getUniqueId(),
                System.currentTimeMillis() + DURACION_MS);

        player.sendMessage(ChatColor.GOLD + "§l» §6Tótem Divino preparado: " + ChatColor.YELLOW
                + "salvará a un aliado en 30 bloques §6del próximo golpe mortal (5 min).");
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1.2f);

        GlowEffect.apply(plugin, player, ChatColor.GOLD, 200);
        player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING,
                player.getLocation().add(0, 1, 0), 100, 0.8, 1.5, 0.8, 0.35);
        player.getWorld().spawnParticle(Particle.END_ROD,
                player.getLocation().add(0, 1, 0), 60, 0.6, 1.2, 0.6, 0.12);
        player.getWorld().spawnParticle(Particle.ENCHANT,
                player.getLocation().add(0, 1, 0), 80, 1.5, 2.0, 1.5, 1.0);
        player.getWorld().spawnParticle(Particle.FLASH,
                player.getLocation().add(0, 2, 0), 2);
        // Columna dorada de luz
        for (int y = 0; y < 6; y++) {
            player.getWorld().spawnParticle(Particle.END_ROD,
                    player.getLocation().add(0, y, 0), 8, 0.15, 0.1, 0.15, 0.03);
        }
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 0.8f);

        Bukkit.getScheduler().runTaskTimer(plugin, new java.util.function.Consumer<org.bukkit.scheduler.BukkitTask>() {
            int ticks = 0;
            @Override
            public void accept(org.bukkit.scheduler.BukkitTask task) {
                if (ticks++ > 6000 || !player.isOnline()) { // 5 mins
                    task.cancel();
                    return;
                }
                if (ticks % 20 == 0) { // every second
                    player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation().add(0, 2, 0), 3, 0.5, 0.5, 0.5, 0.1);
                }
            }
        }, 0L, 1L);
    }
}
