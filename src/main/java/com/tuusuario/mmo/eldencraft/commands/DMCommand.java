package com.tuusuario.mmo.eldencraft.commands;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.campaign.Campaign;
import com.tuusuario.mmo.eldencraft.campaign.CampaignContext;
import com.tuusuario.mmo.eldencraft.campaign.CampaignMember;
import com.tuusuario.mmo.eldencraft.character.CharacterProfile;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import com.tuusuario.mmo.eldencraft.rules.AttributeCalculator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

/**
 * Panel del Dungeon Master de campaña (/dm) (SPEC 15.5 / F3B-3).
 */
public class DMCommand implements CommandExecutor {

    private final EldenCraft plugin;

    public DMCommand(EldenCraft plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Este comando solo puede ser ejecutado por un jugador DM.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String sub = args[0].toLowerCase();
        PlayerData dmData = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        String defaultCamp = dmData != null ? dmData.getCampaignId() : "principal";
        var campMgr = plugin.getCampaignManager();

        switch (sub) {
            case "fichas" -> {
                String campId = args.length >= 2 ? args[1] : defaultCamp;
                if (!player.isOp() && !campMgr.isDm(campId, player.getUniqueId())) {
                    player.sendMessage(ChatColor.RED + "No tienes permisos de DM en la campaña '" + campId + "'.");
                    return true;
                }

                List<CampaignMember> members = campMgr.getMembers(campId);
                player.sendMessage(ChatColor.GOLD + "=== Fichas de la Campaña: " + campId + " (" + members.size() + ") ===");
                for (CampaignMember m : members) {
                    Player online = Bukkit.getPlayer(m.getPlayerUuid());
                    String status = (online != null) ? (ChatColor.GREEN + "[Online]") : (ChatColor.GRAY + "[Offline]");
                    String pName = online != null ? online.getName() : Bukkit.getOfflinePlayer(m.getPlayerUuid()).getName();

                    // Buscar perfil de su personaje activo
                    CharacterProfile prof = null;
                    if (m.getCharacterId() != null) {
                        prof = plugin.getDataManager().getCharacterProfile(m.getCharacterId());
                    }
                    if (prof == null) {
                        List<CharacterProfile> list = plugin.getDataManager().getCharacterProfiles(m.getPlayerUuid());
                        if (!list.isEmpty()) prof = list.get(0);
                    }

                    if (prof != null) {
                        player.sendMessage(ChatColor.YELLOW + "• " + pName + " " + status + ChatColor.WHITE + ": "
                                + prof.getName() + " - " + prof.getPlayerClass().displayName + " Niv " + prof.getLevel()
                                + ChatColor.RED + " (" + prof.getHpCurrent() + "/" + prof.getHpMax() + " PG)");
                    } else {
                        player.sendMessage(ChatColor.YELLOW + "• " + pName + " " + status + ChatColor.GRAY + " (Sin personaje)");
                    }
                }
                player.sendMessage(ChatColor.GRAY + "Usa /dm ficha <jugador> para ver detalles completos.");
            }
            case "ficha" -> {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Uso: /dm ficha <jugador> [campana]");
                    return true;
                }
                String targetName = args[1];
                Player target = Bukkit.getPlayer(targetName);
                UUID targetUuid = (target != null) ? target.getUniqueId() : Bukkit.getOfflinePlayer(targetName).getUniqueId();
                String campId = args.length >= 3 ? args[2] : defaultCamp;

                if (!player.isOp() && !campMgr.isDm(campId, player.getUniqueId())) {
                    player.sendMessage(ChatColor.RED + "No tienes permisos de DM en la campaña '" + campId + "'.");
                    return true;
                }

                PlayerData data = (target != null) ? plugin.getPlayerManager().getPlayerData(targetUuid) : plugin.getDataManager().load(targetUuid);
                if (data == null || data.getPlayerClass() == null) {
                    player.sendMessage(ChatColor.RED + "No se encontraron datos de ficha para " + targetName + ".");
                    return true;
                }

                player.sendMessage(ChatColor.GOLD + "========== FICHA DE " + targetName.toUpperCase() + " ==========");
                player.sendMessage(ChatColor.WHITE + "Personaje: " + ChatColor.YELLOW + data.getCharacterName()
                        + ChatColor.GRAY + " (Slot " + data.getSlot() + " | Campaña: " + data.getCampaignId() + ")");
                player.sendMessage(ChatColor.WHITE + "Raza: " + ChatColor.GREEN + (data.getRace() != null ? data.getRace().displayName : "N/A")
                        + ChatColor.WHITE + " | Clase: " + ChatColor.AQUA + data.getPlayerClass().displayName + " Niv " + data.getLevel());
                player.sendMessage(ChatColor.WHITE + "Alineamiento: " + ChatColor.LIGHT_PURPLE + data.getAlignment()
                        + ChatColor.WHITE + " | Trasfondo: " + ChatColor.GRAY + data.getBackground());
                player.sendMessage(ChatColor.WHITE + "PG: " + ChatColor.RED + data.getDndHpCurrent() + "/" + data.getDndHpMax()
                        + ChatColor.WHITE + " | Oro: " + ChatColor.GOLD + data.getGold() + " | EXP: " + data.getXp());
                player.sendMessage(ChatColor.GOLD + "--- Atributos D&D 5e ---");
                player.sendMessage(ChatColor.GRAY + "  FUE: " + data.getEffectiveStr() + " (" + formatMod(data.getEffectiveStr()) + ")"
                        + " | DES: " + data.getEffectiveDex() + " (" + formatMod(data.getEffectiveDex()) + ")"
                        + " | CON: " + data.getEffectiveCon() + " (" + formatMod(data.getEffectiveCon()) + ")");
                player.sendMessage(ChatColor.GRAY + "  INT: " + data.getEffectiveInt() + " (" + formatMod(data.getEffectiveInt()) + ")"
                        + " | SAB: " + data.getEffectiveWis() + " (" + formatMod(data.getEffectiveWis()) + ")"
                        + " | CAR: " + data.getEffectiveCha() + " (" + formatMod(data.getEffectiveCha()) + ")");
                player.sendMessage(ChatColor.GOLD + "================================================");
            }
            case "aprobar" -> {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Uso: /dm aprobar <jugador> [campana]");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                String campId = args.length >= 3 ? args[2] : defaultCamp;
                if (!player.isOp() && !campMgr.isDm(campId, player.getUniqueId())) {
                    player.sendMessage(ChatColor.RED + "No tienes permisos de DM.");
                    return true;
                }

                if (target != null) {
                    target.sendMessage(ChatColor.GREEN + "✦ ¡Tu ficha ha sido aprobada por el DM " + player.getName() + "!");
                    target.playSound(target.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                }
                plugin.getDataManager().logCharacterHistory(args[1], "APROBACION", "Ficha aprobada por DM", player.getName());
                player.sendMessage(ChatColor.GREEN + "Ficha de " + args[1] + " aprobada.");
            }
            case "rechazar" -> {
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Uso: /dm rechazar <jugador> <motivo> [campana]");
                    return true;
                }
                String targetName = args[1];
                String reason = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
                String campId = defaultCamp;
                if (!player.isOp() && !campMgr.isDm(campId, player.getUniqueId())) {
                    player.sendMessage(ChatColor.RED + "No tienes permisos de DM.");
                    return true;
                }

                Player target = Bukkit.getPlayer(targetName);
                if (target != null) {
                    target.sendMessage(ChatColor.RED + "✦ Tu ficha ha sido rechazada por el DM " + player.getName() + ".");
                    target.sendMessage(ChatColor.RED + "Motivo: " + ChatColor.YELLOW + reason);
                }
                plugin.getDataManager().logCharacterHistory(targetName, "RECHAZO_FICHA", "Ficha rechazada: " + reason, player.getName());
                player.sendMessage(ChatColor.YELLOW + "Ficha de " + targetName + " rechazada. Motivo: " + reason);
            }
            case "editar" -> {
                if (args.length < 4) {
                    player.sendMessage(ChatColor.RED + "Uso: /dm editar <jugador> <campo> <valor> [campana]");
                    player.sendMessage(ChatColor.GRAY + "Campos: hp, maxhp, xp, oro, nivel, fue, des, con, int, sab, car");
                    return true;
                }
                String targetName = args[1];
                String field = args[2].toLowerCase();
                String value = args[3];
                String campId = args.length >= 5 ? args[4] : defaultCamp;

                if (!player.isOp() && !campMgr.isDm(campId, player.getUniqueId())) {
                    player.sendMessage(ChatColor.RED + "No tienes permisos de DM en '" + campId + "'.");
                    return true;
                }

                Player target = Bukkit.getPlayer(targetName);
                UUID targetUuid = target != null ? target.getUniqueId() : Bukkit.getOfflinePlayer(targetName).getUniqueId();
                CharacterProfile prof = plugin.getDataManager().getActiveCharacterProfile(targetUuid);
                if (prof == null) {
                    List<CharacterProfile> list = plugin.getDataManager().getCharacterProfiles(targetUuid);
                    if (!list.isEmpty()) prof = list.get(0);
                }

                if (prof == null) {
                    player.sendMessage(ChatColor.RED + "No se encontró ningún personaje para " + targetName + ".");
                    return true;
                }

                try {
                    int num = Integer.parseInt(value);
                    switch (field) {
                        case "hp", "vida" -> prof.setHpCurrent(num);
                        case "maxhp" -> prof.setHpMax(num);
                        case "xp", "exp" -> prof.setXp(num);
                        case "oro", "gold" -> prof.setGold(num);
                        case "nivel", "level" -> prof.setLevel(num);
                        case "fue", "str" -> prof.setBaseStr(num);
                        case "des", "dex" -> prof.setBaseDex(num);
                        case "con" -> prof.setBaseCon(num);
                        case "int" -> prof.setBaseInt(num);
                        case "sab", "wis" -> prof.setBaseWis(num);
                        case "car", "cha" -> prof.setBaseCha(num);
                        default -> {
                            player.sendMessage(ChatColor.RED + "Campo desconocido: " + field);
                            return true;
                        }
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "El valor debe ser un número entero.");
                    return true;
                }

                plugin.getDataManager().saveCharacterProfile(prof);
                plugin.getDataManager().logCharacterHistory(prof.getCharacterId(), "EDICION_DM",
                        "DM " + player.getName() + " editó " + field + " = " + value, player.getName());

                if (target != null && target.isOnline()) {
                    PlayerData updated = plugin.getDataManager().loadCharacterData(prof.getCharacterId(), target.getUniqueId());
                    plugin.getPlayerManager().loadPlayerData(target.getUniqueId(), updated);
                    com.tuusuario.mmo.eldencraft.managers.ExperienceManager.applyMaxHealth(target, updated);
                    target.sendMessage(ChatColor.YELLOW + "✦ El DM " + player.getName() + " ha modificado tu ficha (" + field + " = " + value + ").");
                }

                player.sendMessage(ChatColor.GREEN + "Ficha de " + targetName + " actualizada con éxito (" + field + " = " + value + ").");
            }
            case "panel", "menu" -> {
                String campId = args.length >= 2 ? args[1] : defaultCamp;
                if (!player.isOp() && !campMgr.isDm(campId, player.getUniqueId())) {
                    player.sendMessage(ChatColor.RED + "No tienes permisos de DM en '" + campId + "'.");
                    return true;
                }
                Campaign c = campMgr.getCampaign(campId);
                if (c == null) {
                    player.sendMessage(ChatColor.RED + "Campaña no encontrada.");
                    return true;
                }
                new com.tuusuario.mmo.eldencraft.campaign.gui.DMCampaignPanelGUI(plugin, player, c).open();
            }
            case "xp" -> {
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Uso: /dm xp <jugador|all> <cantidad> [campana]");
                    return true;
                }
                int amount;
                try {
                    amount = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Cantidad inválida.");
                    return true;
                }

                String campId = args.length >= 4 ? args[3] : defaultCamp;
                if (!player.isOp() && !campMgr.isDm(campId, player.getUniqueId())) {
                    player.sendMessage(ChatColor.RED + "No tienes permisos de DM en '" + campId + "'.");
                    return true;
                }

                if (args[1].equalsIgnoreCase("all") || args[1].equalsIgnoreCase("todos")) {
                    for (Player member : CampaignContext.getOnlineMembers(plugin, campId)) {
                        giveXpToPlayer(member, amount);
                    }
                    player.sendMessage(ChatColor.GREEN + "Se han otorgado " + amount + " XP a todos los miembros de la campaña " + campId + ".");
                } else {
                    Player target = Bukkit.getPlayer(args[1]);
                    if (target != null) {
                        giveXpToPlayer(target, amount);
                        player.sendMessage(ChatColor.GREEN + "Se han otorgado " + amount + " XP a " + target.getName() + ".");
                    } else {
                        // Jugador desconectado: procesar experiencia y subida de nivel
                        org.bukkit.OfflinePlayer offline = Bukkit.getOfflinePlayer(args[1]);
                        UUID targetUuid = offline.getUniqueId();
                        CharacterProfile prof = plugin.getDataManager().getActiveCharacterProfile(targetUuid);
                        if (prof == null) {
                            List<CharacterProfile> list = plugin.getDataManager().getCharacterProfiles(targetUuid);
                            if (!list.isEmpty()) prof = list.get(0);
                        }
                        if (prof == null) {
                            player.sendMessage(ChatColor.RED + "El jugador " + args[1] + " no tiene ningún personaje registrado.");
                            return true;
                        }

                        int oldLevel = prof.getLevel();
                        int newXp = prof.getXp() + amount;
                        prof.setXp(newXp);

                        int required = plugin.getExperienceManager().getRequiredExp(prof.getLevel());
                        boolean leveledUp = false;
                        while (newXp >= required && prof.getLevel() < 20) {
                            prof.setLevel(prof.getLevel() + 1);
                            leveledUp = true;
                            int conMod = AttributeCalculator.modifier(prof.getBaseCon());
                            int newMaxHp = com.tuusuario.mmo.eldencraft.character.LevelUpManager.calculateMaxHp(prof.getPlayerClass(), prof.getLevel(), conMod);
                            int gain = Math.max(1, newMaxHp - prof.getHpMax());
                            prof.setHpMax(newMaxHp);
                            prof.setHpCurrent(prof.getHpCurrent() + gain);
                            required = plugin.getExperienceManager().getRequiredExp(prof.getLevel());
                        }

                        plugin.getDataManager().saveCharacterProfile(prof);
                        plugin.getDataManager().logCharacterHistory(
                            prof.getCharacterId(),
                            "DM_XP_OFFLINE",
                            "Otorgados " + amount + " XP a personaje offline (Nivel " + oldLevel + " -> " + prof.getLevel() + ")",
                            player.getName()
                        );

                        player.sendMessage(ChatColor.GREEN + "Se han otorgado " + amount + " XP a " + args[1] + " (Offline).");
                        if (leveledUp) {
                            player.sendMessage(ChatColor.GOLD + "✦ ¡" + args[1] + " ha subido al Nivel " + prof.getLevel() + " (PG Max: " + prof.getHpMax() + ")!");
                        }
                    }
                }
            }
            case "oro" -> {
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Uso: /dm oro <jugador|all> <cantidad> [campana]");
                    return true;
                }
                int amount;
                try {
                    amount = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Cantidad inválida.");
                    return true;
                }

                String campId = args.length >= 4 ? args[3] : defaultCamp;
                if (!player.isOp() && !campMgr.isDm(campId, player.getUniqueId())) {
                    player.sendMessage(ChatColor.RED + "No tienes permisos de DM.");
                    return true;
                }

                if (args[1].equalsIgnoreCase("all") || args[1].equalsIgnoreCase("todos")) {
                    for (Player member : CampaignContext.getOnlineMembers(plugin, campId)) {
                        giveGoldToPlayer(member, amount);
                    }
                    player.sendMessage(ChatColor.GREEN + "Se han otorgado " + amount + " de oro a todos los miembros.");
                } else {
                    Player target = Bukkit.getPlayer(args[1]);
                    if (target == null) {
                        player.sendMessage(ChatColor.RED + "Jugador no encontrado.");
                        return true;
                    }
                    giveGoldToPlayer(target, amount);
                    player.sendMessage(ChatColor.GREEN + "Se han otorgado " + amount + " de oro a " + target.getName() + ".");
                }
            }
            case "sesion" -> {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Uso: /dm sesion <iniciar|terminar> [campana]");
                    return true;
                }
                String campId = args.length >= 3 ? args[2] : defaultCamp;
                if (!player.isOp() && !campMgr.isDm(campId, player.getUniqueId())) {
                    player.sendMessage(ChatColor.RED + "No tienes permisos de DM.");
                    return true;
                }

                if (args[1].equalsIgnoreCase("iniciar")) {
                    String sId = "ses_" + campId + "_" + System.currentTimeMillis();
                    plugin.getDataManager().startSession(sId, campId);
                    for (Player m : CampaignContext.getOnlineMembers(plugin, campId)) {
                        m.sendTitle(ChatColor.GOLD + "⚔ SESIÓN INICIADA ⚔", ChatColor.YELLOW + "Campaña: " + campId, 10, 70, 20);
                        m.playSound(m.getLocation(), Sound.EVENT_RAID_HORN, 1f, 1f);
                    }
                    player.sendMessage(ChatColor.GREEN + "Sesión de campaña iniciada (ID: " + sId + ").");
                } else if (args[1].equalsIgnoreCase("terminar")) {
                    String activeId = plugin.getDataManager().getActiveSessionId(campId);
                    if (activeId == null) {
                        player.sendMessage(ChatColor.YELLOW + "No hay una sesión activa para la campaña " + campId + ".");
                        return true;
                    }
                    plugin.getDataManager().endSession(activeId, "Sesión finalizada por DM " + player.getName(), 0, "");
                    for (Player m : CampaignContext.getOnlineMembers(plugin, campId)) {
                        m.sendTitle(ChatColor.YELLOW + "✦ SESIÓN FINALIZADA ✦", ChatColor.GRAY + "¡Buen juego a todos!", 10, 70, 20);
                        m.playSound(m.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
                    }
                    player.sendMessage(ChatColor.YELLOW + "Sesión de campaña finalizada.");
                }
            }
            case "nota" -> {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Uso: /dm nota <agregar|listar> [texto]");
                    return true;
                }
                String campId = defaultCamp;
                if (!player.isOp() && !campMgr.isDm(campId, player.getUniqueId())) {
                    player.sendMessage(ChatColor.RED + "No tienes permisos de DM.");
                    return true;
                }

                if (args[1].equalsIgnoreCase("agregar") && args.length >= 3) {
                    String text = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
                    plugin.getDataManager().addCampaignNote(campId, null, text);
                    player.sendMessage(ChatColor.GREEN + "Nota guardada en la campaña " + campId + ".");
                } else if (args[1].equalsIgnoreCase("listar")) {
                    List<String> notes = plugin.getDataManager().getCampaignNotes(campId);
                    player.sendMessage(ChatColor.GOLD + "=== Notas de la Campaña: " + campId + " ===");
                    if (notes.isEmpty()) player.sendMessage(ChatColor.GRAY + "No hay notas registradas.");
                    for (String n : notes) {
                        player.sendMessage(ChatColor.YELLOW + "• " + ChatColor.WHITE + n);
                    }
                }
            }
            default -> sendHelp(player);
        }
        return true;
    }

    private void giveXpToPlayer(Player player, int amount) {
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data == null) return;
        data.setXp(data.getXp() + amount);
        plugin.getExperienceManager().checkLevelUp(player, data);
        plugin.getDataManager().save(data);
        player.sendMessage(ChatColor.GREEN + "Has recibido " + ChatColor.GOLD + amount + " XP" + ChatColor.GREEN + " del DM.");
    }

    private void giveGoldToPlayer(Player player, int amount) {
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data == null) return;
        data.setGold(data.getGold() + amount);
        plugin.getDataManager().save(data);
        player.sendMessage(ChatColor.GOLD + "Has recibido " + amount + " piezas de oro del DM.");
    }

    private String formatMod(int score) {
        int mod = AttributeCalculator.modifier(score);
        return (mod >= 0 ? "+" : "") + mod;
    }

    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== Panel del Dungeon Master (/dm) ===");
        player.sendMessage(ChatColor.YELLOW + "  /dm panel [campana]" + ChatColor.GRAY + " - Abre panel GUI de campaña");
        player.sendMessage(ChatColor.YELLOW + "  /dm fichas [campana]" + ChatColor.GRAY + " - Lista miembros y fichas");
        player.sendMessage(ChatColor.YELLOW + "  /dm ficha <jugador> [campana]" + ChatColor.GRAY + " - Muestra ficha detallada");
        player.sendMessage(ChatColor.YELLOW + "  /dm aprobar <jugador> [campana]" + ChatColor.GRAY + " - Aprueba ficha");
        player.sendMessage(ChatColor.YELLOW + "  /dm rechazar <jugador> <motivo>" + ChatColor.GRAY + " - Rechaza ficha con motivo");
        player.sendMessage(ChatColor.YELLOW + "  /dm editar <jugador> <campo> <val>" + ChatColor.GRAY + " - Modifica atributos/stats");
        player.sendMessage(ChatColor.YELLOW + "  /dm xp <jugador|all> <cantidad>" + ChatColor.GRAY + " - Otorga experiencia");
        player.sendMessage(ChatColor.YELLOW + "  /dm oro <jugador|all> <cantidad>" + ChatColor.GRAY + " - Otorga oro");
        player.sendMessage(ChatColor.YELLOW + "  /dm sesion <iniciar|terminar>" + ChatColor.GRAY + " - Control de sesiones");
        player.sendMessage(ChatColor.YELLOW + "  /dm nota <agregar|listar> <texto>" + ChatColor.GRAY + " - Notas de campaña");
    }
}
