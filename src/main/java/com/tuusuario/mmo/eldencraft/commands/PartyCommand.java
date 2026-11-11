package com.tuusuario.mmo.eldencraft.commands;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.managers.PartyManager;
import com.tuusuario.mmo.eldencraft.managers.PartyManager.Party;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PartyCommand implements CommandExecutor {

    private final EldenCraft plugin;

    public PartyCommand(EldenCraft plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Solo jugadores.");
            return true;
        }

        PartyManager pm = plugin.getPartyManager();

        if (args.length == 0) {
            sendHelp(p);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "create" -> pm.createParty(p);

            case "invite" -> {
                if (args.length < 2) { p.sendMessage(ChatColor.RED + "Uso: /party invite <jugador>"); return true; }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) { p.sendMessage(ChatColor.RED + "Jugador no encontrado."); return true; }
                pm.invite(p, target);
            }

            case "accept" -> pm.accept(p);

            case "leave"  -> pm.leave(p);

            case "kick" -> {
                if (args.length < 2) { p.sendMessage(ChatColor.RED + "Uso: /party kick <jugador>"); return true; }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) { p.sendMessage(ChatColor.RED + "Jugador no encontrado."); return true; }
                pm.kick(p, target);
            }

            case "disband" -> pm.disband(p);

            case "list", "info" -> sendInfo(p, pm);

            default -> sendHelp(p);
        }
        return true;
    }

    private void sendHelp(Player p) {
        p.sendMessage(ChatColor.GOLD + "═══════ Party ═══════");
        p.sendMessage(ChatColor.YELLOW + "/party create"   + ChatColor.GRAY + " - Crear una party.");
        p.sendMessage(ChatColor.YELLOW + "/party invite <j>" + ChatColor.GRAY + " - Invitar a un jugador.");
        p.sendMessage(ChatColor.YELLOW + "/party accept"   + ChatColor.GRAY + " - Aceptar una invitacion.");
        p.sendMessage(ChatColor.YELLOW + "/party leave"    + ChatColor.GRAY + " - Salir de la party.");
        p.sendMessage(ChatColor.YELLOW + "/party kick <j>" + ChatColor.GRAY + " - Expulsar (solo lider).");
        p.sendMessage(ChatColor.YELLOW + "/party disband"  + ChatColor.GRAY + " - Disolver (solo lider).");
        p.sendMessage(ChatColor.YELLOW + "/party list"     + ChatColor.GRAY + " - Ver miembros y lider.");
        p.sendMessage(ChatColor.GRAY  + "Comparten EXP en un radio de "
                + (int) PartyManager.XP_SHARE_RADIUS + " bloques.");
    }

    private void sendInfo(Player p, PartyManager pm) {
        Party party = pm.getParty(p.getUniqueId());
        if (party == null) {
            p.sendMessage(ChatColor.RED + "No estas en ninguna party.");
            return;
        }
        p.sendMessage(ChatColor.GOLD + "═══════ Tu Party ═══════");
        p.sendMessage(ChatColor.YELLOW + "Lider: " + ChatColor.WHITE
                + Bukkit.getOfflinePlayer(party.leader).getName());
        p.sendMessage(ChatColor.YELLOW + "Miembros (" + party.members.size() + "):");
        for (UUID id : party.members) {
            Player m = Bukkit.getPlayer(id);
            String name = (m != null && m.isOnline())
                    ? ChatColor.GREEN + m.getName() + ChatColor.GRAY + " (online)"
                    : ChatColor.GRAY + Bukkit.getOfflinePlayer(id).getName() + " (offline)";
            String tag = id.equals(party.leader) ? ChatColor.GOLD + "★ " : ChatColor.WHITE + "• ";
            p.sendMessage("  " + tag + name);
        }
    }
}
