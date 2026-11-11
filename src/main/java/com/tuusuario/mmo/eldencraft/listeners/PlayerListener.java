package com.tuusuario.mmo.eldencraft.listeners;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerListener implements Listener {

    private final EldenCraft plugin;

    public PlayerListener(EldenCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // cargamos los datos del jugador (incluye migración F1/F2 si corresponde)
        PlayerData data = plugin.getPlayerManager().loadPlayer(player.getUniqueId());

        // le seteamos la vida maxima si ya tenia personaje
        if (data.getRace() != null) {
            com.tuusuario.mmo.eldencraft.managers.ExperienceManager.applyMaxHealth(player, data);
            try {
                org.bukkit.attribute.AttributeInstance scaleAttr = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_SCALE);
                if (scaleAttr != null) {
                    scaleAttr.setBaseValue(data.getRace().scale);
                }
            } catch (Throwable ignored) {}

            // Si es Aasimar o Aarakocra, habilitamos allowFlight en tierra para el doble salto
            if (data.getRace() == com.tuusuario.mmo.eldencraft.models.RaceType.AASIMAR || 
                data.getRace() == com.tuusuario.mmo.eldencraft.models.RaceType.AARAKOCRA) {
                player.setAllowFlight(true);
                player.setFlying(false);
                // Programar para el siguiente tick para asegurar que los demás jugadores lo reciban correctamente
                Bukkit.getScheduler().runTask(plugin, () -> plugin.getFlyManager().sendFakeElytra(player, false));
            }
        }

        player.sendMessage(ChatColor.GREEN + "¡Bienvenido a EldenCraft, " + player.getName() + "!");

        // Aplicar visibilidad y scoreboards por campaña
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                com.tuusuario.mmo.eldencraft.campaign.CampaignContext.applyVisibilityIsolation(plugin, player);

                // Avisar de invitaciones pendientes a campañas (C3)
                java.util.List<String> pending = plugin.getDataManager().getPendingInvites(player.getUniqueId());
                if (!pending.isEmpty()) {
                    player.sendMessage(ChatColor.GOLD + "✦ ¡Tienes " + pending.size() + " invitación(es) pendiente(s) a campaña(s) D&D! ("
                            + String.join(", ", pending) + ")");
                    player.sendMessage(ChatColor.YELLOW + "Usa " + ChatColor.GREEN + "/campana aceptar <id>" + ChatColor.YELLOW
                            + " o abre " + ChatColor.GOLD + "/campana" + ChatColor.YELLOW + " para revisarlas.");
                }
            }
        }, 5L);

        // si es nuevo le abrimos el menu pa q elija raza
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (data.getRace() == null) {
                new SelectionGUI(plugin).openRaceMenu(player);
                player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD
                        + "⚠ Debes elegir una raza para comenzar tu aventura.");
            } else {
                player.sendMessage(ChatColor.GRAY + "Eres un " + ChatColor.GOLD
                        + data.getRace().displayName + " "
                        + (data.getPlayerClass() != null ? data.getPlayerClass().displayName : "")
                        + ChatColor.GRAY + " (Niv " + data.getLevel() + ").");
            }
        }, 20L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // limpiamos la party y las invitaciones
        if (plugin.getPartyManager() != null) {
            plugin.getPartyManager().handleQuit(event.getPlayer().getUniqueId());
        }
        plugin.getPlayerManager().unloadPlayer(event.getPlayer().getUniqueId());
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            com.tuusuario.mmo.eldencraft.campaign.CampaignContext.updateAllVisibility(plugin);
        }, 2L);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onChat(org.bukkit.event.player.AsyncPlayerChatEvent event) {
        Player sender = event.getPlayer();
        String senderCamp = com.tuusuario.mmo.eldencraft.campaign.CampaignContext.getCampaignId(plugin, sender);

        // Aislamiento de chat por campaña. Admins ven todo.
        event.getRecipients().removeIf(recipient -> {
            if (recipient.isOp() || recipient.hasPermission("eldencraft.admin")) {
                return false;
            }
            String recipientCamp = com.tuusuario.mmo.eldencraft.campaign.CampaignContext.getCampaignId(plugin, recipient);
            return !senderCamp.equalsIgnoreCase(recipientCamp);
        });
    }

    // F1/SPEC 3.5: Intercepción de daño para PG D&D vs corazones
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!plugin.getConfig().getBoolean("hp.dnd-hp-enabled", true)) return;

        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data == null || data.getDndHpMax() <= 0) return;

        int hpPerHeart = plugin.getConfig().getInt("hp.hp-per-heart", 1);
        int dndDamage = Math.max(1, (int) Math.round(event.getFinalDamage() * hpPerHeart / 2.0));

        int newHp = Math.max(0, data.getDndHpCurrent() - dndDamage);
        data.setDndHpCurrent(newHp);

        // Si la barra de corazones es proporcional, ajustarla visualmente
        if (plugin.getConfig().getBoolean("hp.hearts-proportional", true)) {
            double maxHealth = data.getMaxHealth();
            double fraction = (double) newHp / (double) data.getDndHpMax();
            double visualHealth = Math.max(0.5, Math.min(maxHealth, fraction * maxHealth));
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (player.isOnline() && !player.isDead()) {
                    try {
                        player.setHealth(Math.min(player.getMaxHealth(), visualHealth));
                    } catch (Throwable ignored) {}
                }
            });
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data != null && plugin.getConfig().getBoolean("hp.dnd-hp-enabled", true)) {
            // Al reaparecer, recuperar PG máximos
            data.setDndHpCurrent(data.getDndHpMax());
        }
    }
}
