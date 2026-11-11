package com.tuusuario.mmo.eldencraft.commands;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.campaign.Campaign;
import com.tuusuario.mmo.eldencraft.campaign.CampaignMember;
import com.tuusuario.mmo.eldencraft.campaign.CampaignRole;
import com.tuusuario.mmo.eldencraft.campaign.CampaignStatus;
import com.tuusuario.mmo.eldencraft.campaign.gui.CampaignEditorGUI;
import com.tuusuario.mmo.eldencraft.campaign.gui.CampaignSelectorGUI;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

/**
 * Comando /campana <crear|editar|abrir|cerrar|archivar|borrar|invitar|aceptar|rechazar|vetar|transferir|unirse|lista|info|mundo|rol|expulsar> (SPEC 15.4 / C3).
 */
public class CampanaCommand implements CommandExecutor {

    private final EldenCraft plugin;

    public CampanaCommand(EldenCraft plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Este comando solo puede ser ejecutado por un jugador.");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("menu")) {
            new CampaignSelectorGUI(plugin, player).open();
            return true;
        }

        String sub = args[0].toLowerCase();
        var campMgr = plugin.getCampaignManager();
        PlayerData pData = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        String defaultCamp = (pData != null && pData.getCampaignId() != null) ? pData.getCampaignId() : "principal";

        switch (sub) {
            case "crear" -> {
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Uso: /campana crear <id> <nombre>");
                    return true;
                }
                String id = args[1].toLowerCase();
                String name = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
                try {
                    Campaign c = campMgr.createCampaign(player.getUniqueId(), id, name, "ec_camp_" + id);
                    player.sendMessage(ChatColor.GREEN + "¡Campaña " + ChatColor.GOLD + c.getName()
                            + ChatColor.GREEN + " creada con éxito! Código: " + ChatColor.YELLOW + c.getJoinCode());
                } catch (IllegalArgumentException e) {
                    player.sendMessage(ChatColor.RED + e.getMessage());
                }
            }
            case "editar" -> {
                String id = args.length >= 2 ? args[1] : defaultCamp;
                if (!campMgr.isDm(id, player.getUniqueId()) && !player.isOp()) {
                    player.sendMessage(ChatColor.RED + "No tienes permisos de DM en la campaña '" + id + "'.");
                    return true;
                }
                Campaign c = campMgr.getCampaign(id);
                if (c == null) {
                    player.sendMessage(ChatColor.RED + "Campaña no encontrada.");
                    return true;
                }

                if (args.length < 4) {
                    // Abrir GUI de edición
                    new CampaignEditorGUI(plugin, player, c).open();
                    return true;
                }

                String key = args[2].toLowerCase();
                String val = String.join(" ", java.util.Arrays.copyOfRange(args, 3, args.length));

                switch (key) {
                    case "nombre", "name" -> c.setName(val);
                    case "descripcion", "desc" -> c.setDescription(val);
                    case "icono", "icon" -> c.setIcon(val.toUpperCase());
                    case "estado", "status" -> c.setStatus(CampaignStatus.fromString(val));
                    case "maxplayers", "limite" -> {
                        try { c.setMaxPlayers(Integer.parseInt(val)); } catch (NumberFormatException ignored) {}
                    }
                    case "minlevel" -> {
                        try { c.setMinLevel(Integer.parseInt(val)); } catch (NumberFormatException ignored) {}
                    }
                    case "maxlevel" -> {
                        try { c.setMaxLevel(Integer.parseInt(val)); } catch (NumberFormatException ignored) {}
                    }
                    default -> plugin.getDataManager().setCampaignSetting(id, key, val);
                }
                plugin.getDataManager().saveCampaign(c);
                player.sendMessage(ChatColor.GREEN + "Campaña " + c.getName() + " actualizada: " + key + " = " + val);
            }
            case "abrir" -> {
                String id = args.length >= 2 ? args[1] : defaultCamp;
                if (!campMgr.isDm(id, player.getUniqueId()) && !player.isOp()) {
                    player.sendMessage(ChatColor.RED + "No tienes permisos de DM en la campaña especificada.");
                    return true;
                }
                Campaign c = campMgr.getCampaign(id);
                if (c != null) {
                    c.setStatus(CampaignStatus.ABIERTA);
                    plugin.getDataManager().saveCampaign(c);
                    player.sendMessage(ChatColor.GREEN + "La campaña " + c.getName() + " ahora está ABIERTA.");
                }
            }
            case "cerrar" -> {
                String id = args.length >= 2 ? args[1] : defaultCamp;
                if (!campMgr.isDm(id, player.getUniqueId()) && !player.isOp()) {
                    player.sendMessage(ChatColor.RED + "No tienes permisos de DM en la campaña especificada.");
                    return true;
                }
                Campaign c = campMgr.getCampaign(id);
                if (c != null) {
                    c.setStatus(CampaignStatus.CERRADA);
                    plugin.getDataManager().saveCampaign(c);
                    player.sendMessage(ChatColor.YELLOW + "La campaña " + c.getName() + " ahora está CERRADA.");
                }
            }
            case "archivar" -> {
                String id = args.length >= 2 ? args[1] : defaultCamp;
                if (!campMgr.isDm(id, player.getUniqueId()) && !player.isOp()) {
                    player.sendMessage(ChatColor.RED + "No tienes permisos de DM en la campaña especificada.");
                    return true;
                }
                Campaign c = campMgr.getCampaign(id);
                if (c != null) {
                    c.setStatus(CampaignStatus.ARCHIVADA);
                    plugin.getDataManager().saveCampaign(c);
                    player.sendMessage(ChatColor.YELLOW + "La campaña " + c.getName() + " ahora está ARCHIVADA (solo lectura).");
                }
            }
            case "borrar" -> {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Uso: /campana borrar <id>");
                    return true;
                }
                String id = args[1];
                if (!campMgr.isOwner(id, player.getUniqueId()) && !player.isOp()) {
                    player.sendMessage(ChatColor.RED + "Solo el DM Principal o un administrador puede borrar la campaña.");
                    return true;
                }
                Campaign c = campMgr.getCampaign(id);
                if (c != null) {
                    campMgr.unloadCampaignWorld(c, false);
                    plugin.getDataManager().deleteCampaign(id);
                    player.sendMessage(ChatColor.RED + "La campaña '" + id + "' ha sido eliminada permanentemente.");
                }
            }
            case "invitar" -> {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Uso: /campana invitar <jugador> [id_campaña]");
                    return true;
                }
                String targetName = args[1];
                String campId = args.length >= 3 ? args[2] : defaultCamp;
                campMgr.invitePlayer(player, targetName, campId);
            }
            case "aceptar" -> {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Uso: /campana aceptar <id_campaña>");
                    return true;
                }
                campMgr.acceptInvite(player, args[1]);
            }
            case "rechazar" -> {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Uso: /campana rechazar <id_campaña>");
                    return true;
                }
                campMgr.rejectInvite(player, args[1]);
            }
            case "vetar" -> {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Uso: /campana vetar <jugador> [id_campaña]");
                    return true;
                }
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                String campId = args.length >= 3 ? args[2] : defaultCamp;
                campMgr.banPlayer(player, target.getUniqueId(), campId);
            }
            case "transferir" -> {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Uso: /campana transferir <nuevo_dm> [id_campaña]");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage(ChatColor.RED + "El jugador objetivo debe estar conectado para transferirle la campaña.");
                    return true;
                }
                String campId = args.length >= 3 ? args[2] : defaultCamp;
                campMgr.transferOwnership(player, target.getUniqueId(), campId);
            }
            case "unirse" -> {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Uso: /campana unirse <codigo|id_campaña>");
                    return true;
                }
                campMgr.joinCampaign(player, args[1]);
            }
            case "lista" -> {
                List<Campaign> list = campMgr.getAllCampaigns();
                player.sendMessage(ChatColor.GOLD + "=== Campañas Disponibles (" + list.size() + ") ===");
                for (Campaign c : list) {
                    CampaignRole r = campMgr.getRole(c.getId(), player.getUniqueId());
                    String roleStr = r != null ? (" [" + r.displayName + "]") : "";
                    player.sendMessage(ChatColor.YELLOW + "• " + c.getName() + ChatColor.GRAY + " (" + c.getId() + ")"
                            + ChatColor.AQUA + " - " + c.getStatus().displayName + roleStr);
                }
            }
            case "info" -> {
                String id = args.length >= 2 ? args[1] : defaultCamp;
                Campaign c = campMgr.getCampaign(id);
                if (c == null) {
                    player.sendMessage(ChatColor.RED + "Campaña no encontrada.");
                    return true;
                }
                List<CampaignMember> members = campMgr.getMembers(c.getId());
                player.sendMessage(ChatColor.GOLD + "=== Campaña: " + c.getName() + " ===");
                player.sendMessage(ChatColor.WHITE + "ID: " + ChatColor.GRAY + c.getId());
                player.sendMessage(ChatColor.WHITE + "Estado: " + ChatColor.AQUA + c.getStatus().displayName);
                player.sendMessage(ChatColor.WHITE + "Mundo: " + ChatColor.YELLOW + c.getWorldName());
                player.sendMessage(ChatColor.WHITE + "Niveles: " + ChatColor.GREEN + c.getMinLevel() + " - " + c.getMaxLevel());
                player.sendMessage(ChatColor.WHITE + "Miembros (" + members.size() + "/" + c.getMaxPlayers() + "):");
                for (CampaignMember m : members) {
                    String pName = Bukkit.getOfflinePlayer(m.getPlayerUuid()).getName();
                    player.sendMessage(ChatColor.GRAY + "  - " + (pName != null ? pName : m.getPlayerUuid().toString().substring(0, 8))
                            + " (" + m.getRole().displayName + ")");
                }
                if (campMgr.isDm(c.getId(), player.getUniqueId())) {
                    player.sendMessage(ChatColor.YELLOW + "Código de Invitación: " + ChatColor.GOLD + c.getJoinCode());
                }
            }
            case "mundo" -> {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Uso: /campana mundo <cargar|descargar|tp> [id]");
                    return true;
                }
                String action = args[1].toLowerCase();
                String id = args.length >= 3 ? args[2] : defaultCamp;
                Campaign c = campMgr.getCampaign(id);
                if (c == null) {
                    player.sendMessage(ChatColor.RED + "Campaña no encontrada.");
                    return true;
                }

                if (!campMgr.isDm(id, player.getUniqueId()) && !action.equals("tp") && !player.isOp()) {
                    player.sendMessage(ChatColor.RED + "Solo un DM puede gestionar el mundo de la campaña.");
                    return true;
                }

                switch (action) {
                    case "cargar" -> {
                        campMgr.loadCampaignWorld(c);
                        player.sendMessage(ChatColor.GREEN + "Mundo " + c.getWorldName() + " cargado.");
                    }
                    case "descargar" -> {
                        campMgr.unloadCampaignWorld(c, true);
                        player.sendMessage(ChatColor.YELLOW + "Mundo " + c.getWorldName() + " descargado.");
                    }
                    case "tp" -> {
                        campMgr.teleportToCampaignSpawn(player, c);
                    }
                }
            }
            case "rol" -> {
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Uso: /campana rol <jugador> <DM_PRINCIPAL|CO_DM|JUGADOR|OBSERVADOR> [id]");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage(ChatColor.RED + "Jugador no encontrado.");
                    return true;
                }
                CampaignRole role = CampaignRole.fromString(args[2]);
                String id = args.length >= 4 ? args[3] : defaultCamp;
                campMgr.setRole(player, target.getUniqueId(), id, role);
            }
            case "expulsar" -> {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Uso: /campana expulsar <jugador> [id]");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                UUID targetUuid = target != null ? target.getUniqueId() : Bukkit.getOfflinePlayer(args[1]).getUniqueId();
                String id = args.length >= 3 ? args[2] : defaultCamp;
                campMgr.kickPlayer(player, targetUuid, id);
            }
            default -> {
                player.sendMessage(ChatColor.GOLD + "=== Comandos de /campana ===");
                player.sendMessage(ChatColor.YELLOW + "  /campana menu" + ChatColor.GRAY + " - Abre el selector gráfico");
                player.sendMessage(ChatColor.YELLOW + "  /campana lista" + ChatColor.GRAY + " - Lista campañas disponibles");
                player.sendMessage(ChatColor.YELLOW + "  /campana info [id]" + ChatColor.GRAY + " - Información de campaña");
                player.sendMessage(ChatColor.YELLOW + "  /campana unirse <codigo|id>" + ChatColor.GRAY + " - Unirse a campaña");
                player.sendMessage(ChatColor.YELLOW + "  /campana invitar <jugador>" + ChatColor.GRAY + " - Invitar por nombre");
                player.sendMessage(ChatColor.YELLOW + "  /campana aceptar|rechazar <id>" + ChatColor.GRAY + " - Responder invitación");
                player.sendMessage(ChatColor.YELLOW + "  /campana crear <id> <nombre>" + ChatColor.GRAY + " - Crear nueva campaña");
                player.sendMessage(ChatColor.YELLOW + "  /campana editar <id> [clave] [val]" + ChatColor.GRAY + " - Editar campaña / GUI");
                player.sendMessage(ChatColor.YELLOW + "  /campana abrir|cerrar|archivar [id]" + ChatColor.GRAY + " - Cambiar estado");
                player.sendMessage(ChatColor.YELLOW + "  /campana borrar <id>" + ChatColor.GRAY + " - Eliminar campaña");
                player.sendMessage(ChatColor.YELLOW + "  /campana vetar <jugador>" + ChatColor.GRAY + " - Vetar jugador");
                player.sendMessage(ChatColor.YELLOW + "  /campana transferir <nuevo_dm>" + ChatColor.GRAY + " - Transferir DM principal");
                player.sendMessage(ChatColor.YELLOW + "  /campana mundo <cargar|descargar|tp>" + ChatColor.GRAY + " - Control del mundo");
            }
        }
        return true;
    }
}
