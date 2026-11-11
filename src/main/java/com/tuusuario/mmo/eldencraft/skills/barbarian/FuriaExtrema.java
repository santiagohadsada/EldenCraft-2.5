package com.tuusuario.mmo.eldencraft.skills.barbarian;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import com.tuusuario.mmo.eldencraft.util.BalanceState;
import com.tuusuario.mmo.eldencraft.util.GlowEffect;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

// furia extrema – BALANCEADO: - Ya NO es instakill: STRENGTH I + SPEED I + REGEN I.
public class FuriaExtrema implements Skill {
    @Override public String getName() { return "Furia Extrema"; }
    @Override public double getCost() { return 80.0; }
    @Override public int getLevelRequired() { return 20; }

    @Override
    public void execute(Player player, PlayerData data) {
        int duration = 800; // 40s
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH,    duration, 1)); // STR II
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,       duration, 0)); // SPEED I
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 300, 1));
        player.sendMessage("§4§l[!] §c¡Tu furia te ciega! §7(recibes el doble de daño durante 40s)");
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1f, 0.5f);
        player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 0.5f, 0.6f);
        player.playSound(player.getLocation(), Sound.ENTITY_RAVAGER_ROAR, 0.8f, 0.8f);

        EldenCraft plugin = JavaPlugin.getPlugin(EldenCraft.class);
        GlowEffect.apply(plugin, player, ChatColor.DARK_RED, duration);

        // Activar berserk -> recibe x2 daño
        BalanceState.barbarianBerserk.add(player.getUniqueId());
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            BalanceState.barbarianBerserk.remove(player.getUniqueId());
            if (player.isOnline()) {
                player.sendMessage(ChatColor.GRAY + "✦ La furia se desvanece. Tu defensa vuelve a la normalidad.");
            }
        }, duration);

        // le ponemos una re aura roja
        Particle.DustOptions deepRed = new Particle.DustOptions(Color.fromRGB(180, 0, 0), 2.0f);
        final int iters = duration / 10;
        new BukkitRunnable() {
            int n = 0;
            @Override
            public void run() {
                if (n++ >= iters || !player.isOnline()) { cancel(); return; }
                player.getWorld().spawnParticle(Particle.DUST,
                        player.getLocation().add(0, 1, 0), 12, 0.5, 0.9, 0.5, 0, deepRed);
                player.getWorld().spawnParticle(Particle.LAVA,
                        player.getLocation().add(0, 0.2, 0), 2, 0.3, 0.1, 0.3, 0);
                // Ondas de FLAME expansivas periódicas
                if (n % 6 == 0) {
                    player.getWorld().spawnParticle(Particle.FLAME,
                            player.getLocation().add(0, 0.5, 0), 15, 1.5, 0.3, 1.5, 0.05);
                }
            }
        }.runTaskTimer(plugin, 10L, 10L);
    }
}
