package com.tuusuario.mmo.eldencraft.managers;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.models.ClassType;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import com.tuusuario.mmo.eldencraft.skills.Skill;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CastManager implements Listener {
    private final EldenCraft plugin;

    private static class CastState {
        BossBar bar;
        Skill skill;
        int currentTicks;
        int maxTicks;
        boolean mustCharge;
        boolean sobrecargar;
    }

    private final Map<UUID, CastState> castingPlayers = new HashMap<>();

    public CastManager(EldenCraft plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Map.Entry<UUID, CastState> entry : castingPlayers.entrySet()) {
                Player p = Bukkit.getPlayer(entry.getKey());
                if (p == null || !p.isOnline()) continue;

                CastState state = entry.getValue();
                if (state.currentTicks < state.maxTicks) {
                    state.currentTicks++;
                    double progress = (double) state.currentTicks / state.maxTicks;
                    state.bar.setProgress(Math.min(1.0, progress));
                    
                    if (progress >= 1.0) {
                        state.bar.setTitle(ChatColor.LIGHT_PURPLE + "✦ ¡" + state.skill.getName() + " LISTO! ✦");
                        state.bar.setColor(BarColor.PURPLE);
                        if (state.currentTicks == state.maxTicks) {
                            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 2f);
                        }
                    } else {
                        state.bar.setTitle(ChatColor.LIGHT_PURPLE + "Cargando " + state.skill.getName() + "... (" + (int)(progress*100) + "%)");
                    }

                    // Partículas de casteo
                    PlayerData data = plugin.getPlayerManager().getPlayerData(p.getUniqueId());
                    if (data != null && data.getPlayerClass() != null) {
                        spawnCastParticles(p, data.getPlayerClass(), progress);
                    }
                }
            }
        }, 0L, 1L);
    }

    private void spawnCastParticles(Player p, ClassType type, double progress) {
        org.bukkit.Location loc = p.getLocation().add(0, 1, 0);
        int amount = (int) (progress * 5) + 1;
        switch (type) {
            case MAGE: p.getWorld().spawnParticle(Particle.FLAME, loc, amount, 0.5, 0.5, 0.5, 0.02); break;
            case SORCERER: p.getWorld().spawnParticle(Particle.PORTAL, loc, amount * 2, 0.5, 0.5, 0.5, 0.1); break;
            case WARLOCK: p.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, loc, amount, 0.5, 0.5, 0.5, 0.02); break;
            case DRUID: p.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc, amount, 0.5, 0.5, 0.5, 0); break;
            case CLERIC: p.getWorld().spawnParticle(Particle.END_ROD, loc, amount, 0.5, 0.5, 0.5, 0.02); break;
            case PALADIN: p.getWorld().spawnParticle(Particle.WAX_ON, loc, amount, 0.5, 0.5, 0.5, 0); break;
            case BARD: p.getWorld().spawnParticle(Particle.NOTE, loc, amount, 0.5, 0.5, 0.5, 1); break;
            default: p.getWorld().spawnParticle(Particle.ENCHANT, loc, amount, 0.5, 0.5, 0.5, 0.1); break;
        }
    }

    public void startCasting(Player player, Skill skill, boolean sobrecargar) {
        if (castingPlayers.containsKey(player.getUniqueId())) {
            cancelCast(player, true);
        }

        CastState state = new CastState();
        state.skill = skill;
        state.currentTicks = 0;
        state.maxTicks = skill.getMaxChargeTicks();
        state.mustCharge = skill.getCastType() == Skill.CastType.MUST_CHARGE;
        state.sobrecargar = sobrecargar;
        
        state.bar = Bukkit.createBossBar(ChatColor.LIGHT_PURPLE + "Cargando " + skill.getName() + "...", BarColor.PINK, BarStyle.SEGMENTED_10);
        state.bar.setProgress(0.0);
        state.bar.addPlayer(player);

        castingPlayers.put(player.getUniqueId(), state);
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_AMBIENT, 1f, 1.5f);
        player.sendMessage(ChatColor.GRAY + "Cargando hechizo... Usa Click Izquierdo o Agáchate para lanzar.");
    }

    public void cancelCast(Player player, boolean silent) {
        CastState state = castingPlayers.remove(player.getUniqueId());
        if (state != null) {
            state.bar.removeAll();
            if (!silent) {
                player.sendMessage(ChatColor.RED + "Casteo cancelado.");
                player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.5f, 1f);
            }
        }
    }

    private void finishCast(Player player) {
        CastState state = castingPlayers.remove(player.getUniqueId());
        if (state == null) return;
        
        state.bar.removeAll();
        double progress = (double) state.currentTicks / state.maxTicks;
        if (progress > 1.0) progress = 1.0;

        if (state.mustCharge && progress < 1.0) {
            player.sendMessage(ChatColor.RED + "¡Este hechizo requiere carga completa!");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data != null) {
            state.skill.execute(player, data, progress);
            if (state.sobrecargar) {
                state.skill.execute(player, data, progress);
                player.sendActionBar(ChatColor.LIGHT_PURPLE + "✦ ¡Sobrecarga! " + state.skill.getName() + " x2");
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (castingPlayers.containsKey(event.getPlayer().getUniqueId())) {
                finishCast(event.getPlayer());
            }
        }
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        if (event.isSneaking() && castingPlayers.containsKey(event.getPlayer().getUniqueId())) {
            finishCast(event.getPlayer());
        }
    }
}
