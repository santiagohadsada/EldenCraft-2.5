package com.tuusuario.mmo.eldencraft.listeners;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import com.tuusuario.mmo.eldencraft.models.RaceType;
import com.tuusuario.mmo.eldencraft.managers.FlyManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;

public class FlyListener implements Listener {

    private final EldenCraft plugin;

    public FlyListener(EldenCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == org.bukkit.GameMode.CREATIVE || player.getGameMode() == org.bukkit.GameMode.SPECTATOR) {
            return;
        }

        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data == null || data.getRace() == null) return;

        RaceType race = data.getRace();
        if (race == RaceType.AASIMAR || race == RaceType.AARAKOCRA) {
            event.setCancelled(true);
            
            // Si ya está volando, lo desactivamos al presionar espacio de nuevo (doble salto)
            if (plugin.getFlyManager().isFlying(player)) {
                plugin.getFlyManager().deactivateFlight(player, true);
            } else {
                // Si no, intentamos alzar el vuelo
                plugin.getFlyManager().tryActivateFlight(player, data);
            }
        }
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;

        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data == null || data.getRace() == null) return;

        RaceType race = data.getRace();
        if (race == RaceType.AASIMAR || race == RaceType.AARAKOCRA) {
            FlyManager.FlyState state = plugin.getFlyManager().getFlyState(player.getUniqueId());
            if (state != null && state.cancelFallTicks > 0) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onToggleGlide(EntityToggleGlideEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.getGameMode() == org.bukkit.GameMode.CREATIVE || player.getGameMode() == org.bukkit.GameMode.SPECTATOR) {
            return;
        }

        boolean isFlyingActive = plugin.getFlyManager().isFlying(player);
        if (isFlyingActive) {
            // Cancelamos el evento si intenta desactivar el planeo (para evadir la restricción del servidor sin elytra real)
            if (!event.isGliding()) {
                event.setCancelled(true);
            }
        } else {
            // Si NO está volando (ej: en cooldown o sin recursos), bloqueamos el intento del cliente de entrar en glide al presionar espacio
            if (event.isGliding()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onArmorChange(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data == null || data.getRace() == null) return;
        RaceType race = data.getRace();
        if (race == RaceType.AASIMAR || race == RaceType.AARAKOCRA) {
            // Sincronizamos la elytra falsa en el siguiente tick para dejar que se procese el click
            Bukkit.getScheduler().runTask(plugin, () -> plugin.getFlyManager().sendFakeElytra(player, false));
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data == null || data.getRace() == null) return;
        RaceType race = data.getRace();
        if (race == RaceType.AASIMAR || race == RaceType.AARAKOCRA) {
            // Sincronizamos en el siguiente tick si equipa dándole click derecho en la barra
            Bukkit.getScheduler().runTask(plugin, () -> plugin.getFlyManager().sendFakeElytra(player, false));
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.getFlyManager().syncFakeEquipmentForJoined(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getFlyManager().clearPlayer(event.getPlayer().getUniqueId());
    }
}
