package com.tuusuario.mmo.eldencraft.commands;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

// /boss <id>  -> invoca un jefe en tu posicion.
public class BossCommand implements CommandExecutor {

    private final EldenCraft plugin;

    public BossCommand(EldenCraft plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("eldencraft.admin")) {
            sender.sendMessage(ChatColor.RED + "No tienes permiso.");
            return true;
        }
        if (!(sender instanceof Player p)) {
            sender.sendMessage(ChatColor.RED + "Solo jugadores.");
            return true;
        }
        if (args.length == 0 || args[0].equalsIgnoreCase("list")) {
            sender.sendMessage(ChatColor.GOLD + "Jefes disponibles:");
            plugin.getBossManager().getBosses().forEach((id, def) ->
                sender.sendMessage(ChatColor.YELLOW + " - " + ChatColor.WHITE + id
                        + ChatColor.GRAY + " (" + def.display + ChatColor.GRAY + ")"));
            sender.sendMessage(ChatColor.GRAY + "Uso: /boss <id>");
            return true;
        }
        var le = plugin.getBossManager().spawn(args[0], p.getLocation());
        if (le == null) {
            sender.sendMessage(ChatColor.RED + "No se pudo invocar al jefe '" + args[0] + "'.");
        }
        return true;
    }
}
