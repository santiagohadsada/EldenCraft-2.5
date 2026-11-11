package com.tuusuario.mmo.eldencraft.character;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.character.gui.CharacterSelectorGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Controla la seguridad y el flujo en el mundo de Lobby (SPEC 15.3 / F3B-1).
 */
public class LobbyListener implements Listener {

    private final EldenCraft plugin;
    private final Set<String> allowedCommands = new HashSet<>(Arrays.asList(
            "personajes", "menu", "personaje", "lobby", "ayuda", "help", "login", "register"
    ));

    public LobbyListener(EldenCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        var charMgr = plugin.getCharacterManager();
        var lobbyMgr = plugin.getLobbyManager();

        boolean rejoinAuto = lobbyMgr.isRejoinAuto();
        String activeId = charMgr.getActiveCharacterId(player.getUniqueId());

        if (rejoinAuto && activeId != null) {
            // Reentrada automática: restaurar el mismo personaje y campaña
            boolean ok = charMgr.selectCharacter(player, activeId);
            if (ok) {
                var pData = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
                String cName = (pData != null && pData.getCampaignId() != null) ? pData.getCampaignId() : "principal";
                player.sendMessage(ChatColor.GREEN + "✦ Reentrada automática activada: has regresado a tu campaña "
                        + ChatColor.GOLD + cName + ChatColor.GREEN + " con tu personaje activo.");
                player.sendMessage(ChatColor.GRAY + "Escribe " + ChatColor.YELLOW + "/personajes" + ChatColor.GRAY + " para abrir el selector o ir al lobby.");
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline()) {
                        com.tuusuario.mmo.eldencraft.campaign.CampaignContext.applyVisibilityIsolation(plugin, player);
                    }
                }, 2L);
                return;
            }
        }

        // Si no hay personaje activo o rejoin.auto está desactivado o falló:
        lobbyMgr.sendToLobby(player);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                new CharacterSelectorGUI(plugin, player).open();
            }
        }, 5L);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;
        if (!plugin.getLobbyManager().isInLobby(player)) return;

        e.setCancelled(true);
        if (e.getCause() == EntityDamageEvent.DamageCause.VOID) {
            plugin.getLobbyManager().sendToLobby(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFood(FoodLevelChangeEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;
        if (plugin.getLobbyManager().isInLobby(player)) {
            e.setCancelled(true);
            player.setFoodLevel(20);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        if (plugin.getLobbyManager().isInLobby(p) && !(p.isOp() && p.getGameMode() == GameMode.CREATIVE)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        if (plugin.getLobbyManager().isInLobby(p) && !(p.isOp() && p.getGameMode() == GameMode.CREATIVE)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent e) {
        if (plugin.getLobbyManager().isInLobby(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent e) {
        if (e.getEntity() instanceof Player player && plugin.getLobbyManager().isInLobby(player)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getWhoClicked() instanceof Player player && plugin.getLobbyManager().isInLobby(player)) {
            // Si está en el lobby sin personaje activo y toca su inventario, cancelar
            if (plugin.getCharacterManager().getActiveCharacterId(player.getUniqueId()) == null) {
                if (e.getClickedInventory() == player.getInventory()) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (plugin.getLobbyManager().isInLobby(p)) {
            if (plugin.getCharacterManager().getActiveCharacterId(p.getUniqueId()) == null) {
                // Bloquea interacción con el mundo si no hay personaje activo
                if (e.getClickedBlock() != null) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        if (!plugin.getLobbyManager().isInLobby(p)) return;
        if (p.isOp()) return;

        if (plugin.getCharacterManager().getActiveCharacterId(p.getUniqueId()) == null) {
            String msg = e.getMessage().trim().toLowerCase();
            if (msg.startsWith("/")) msg = msg.substring(1);
            String[] parts = msg.split(" ");
            String baseCmd = parts[0].contains(":") ? parts[0].split(":")[1] : parts[0];

            if (!allowedCommands.contains(baseCmd)) {
                e.setCancelled(true);
                p.sendMessage(ChatColor.RED + "¡Debes seleccionar un personaje primero! Usa "
                        + ChatColor.GOLD + "/personajes" + ChatColor.RED + " para abrir el menú.");
            }
        }
    }
}
