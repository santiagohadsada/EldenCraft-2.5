package com.tuusuario.mmo.eldencraft.commands;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

// Comandos /eldencraft o /ec para admins: /ec reload, /ec descanso, etc.
public class AdminCommand implements CommandExecutor {

    private final EldenCraft plugin;

    public AdminCommand(EldenCraft plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("eldencraft.admin")) {
            sender.sendMessage(ChatColor.RED + "Sin permisos.");
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "Uso: /ec <reload|setlevel|givemoney|resetdata|spawnboss|listbosses|descanso>");
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "reload" -> {
                plugin.reloadAll();
                sender.sendMessage(ChatColor.GREEN + "EldenCraft recargado completamente.");
            }
            case "descanso" -> {
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Uso: /ec descanso <corto|largo> <jugador|todos>");
                    return true;
                }
                String tipo = args[1].toLowerCase();
                String targetName = args[2];
                if (targetName.equalsIgnoreCase("todos")) {
                    for (Player online : Bukkit.getOnlinePlayers()) {
                        if (tipo.equals("corto")) {
                            plugin.getRestManager().grantShortRest(online);
                        } else {
                            plugin.getRestManager().grantLongRest(online);
                        }
                    }
                    sender.sendMessage(ChatColor.GREEN + "Descanso " + tipo + " concedido a todos los jugadores.");
                } else {
                    Player target = Bukkit.getPlayer(targetName);
                    if (target == null) {
                        sender.sendMessage(ChatColor.RED + "Jugador no encontrado: " + targetName);
                        return true;
                    }
                    if (tipo.equals("corto")) {
                        plugin.getRestManager().grantShortRest(target);
                    } else {
                        plugin.getRestManager().grantLongRest(target);
                    }
                    sender.sendMessage(ChatColor.GREEN + "Descanso " + tipo + " concedido a " + target.getName() + ".");
                }
            }
            case "setlevel" -> {
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "/ec setlevel <jugador> <nivel>");
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) { sender.sendMessage(ChatColor.RED + "Jugador no conectado."); return true; }
                int lvl;
                try { lvl = Integer.parseInt(args[2]); }
                catch (NumberFormatException e) { sender.sendMessage(ChatColor.RED + "Nivel invalido."); return true; }
                PlayerData d = plugin.getPlayerManager().getPlayerData(target.getUniqueId());
                if (d == null) return true;
                d.setLevel(lvl);
                com.tuusuario.mmo.eldencraft.managers.ExperienceManager.applyMaxHealth(target, d);
                sender.sendMessage(ChatColor.GREEN + "Nivel de " + target.getName() + " puesto en " + lvl);
            }
            case "givemoney" -> {
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "/ec givemoney <jugador> <oro>");
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) { sender.sendMessage(ChatColor.RED + "Jugador no conectado."); return true; }
                int n;
                try { n = Integer.parseInt(args[2]); }
                catch (NumberFormatException e) { sender.sendMessage(ChatColor.RED + "Cantidad invalida."); return true; }
                PlayerData d = plugin.getPlayerManager().getPlayerData(target.getUniqueId());
                if (d == null) return true;
                plugin.getEconomyManager().addGold(d, n);
                sender.sendMessage(ChatColor.GREEN + "Diste " + n + " oro a " + target.getName());
            }
            case "resetdata" -> {
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "/ec resetdata <jugador>");
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target != null) {
                    plugin.getPlayerManager().unloadPlayer(target.getUniqueId());
                    plugin.getDataManager().delete(target.getUniqueId());
                    plugin.getPlayerManager().loadPlayer(target.getUniqueId());
                    sender.sendMessage(ChatColor.GREEN + "Datos de " + target.getName() + " borrados.");
                } else {
                    sender.sendMessage(ChatColor.RED + "Jugador no conectado.");
                }
            }
            case "spawnboss" -> {
                if (!(sender instanceof Player p)) {
                    sender.sendMessage(ChatColor.RED + "Solo jugadores.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "/ec spawnboss <id>  (usa /ec listbosses para ver los IDs)");
                    return true;
                }
                var le = plugin.getBossManager().spawn(args[1], p.getLocation());
                if (le == null) {
                    sender.sendMessage(ChatColor.RED + "No se pudo invocar al jefe '" + args[1] + "'.");
                }
            }
            case "listbosses" -> {
                sender.sendMessage(ChatColor.GOLD + "Jefes disponibles:");
                plugin.getBossManager().getBosses().forEach((id, def) ->
                    sender.sendMessage(ChatColor.YELLOW + " - " + ChatColor.WHITE + id
                            + ChatColor.GRAY + "  " + def.display));
            }
            default -> sender.sendMessage(ChatColor.YELLOW + "Uso: /ec <reload|descanso|setlevel|givemoney|resetdata|spawnboss|listbosses>");
        }
        return true;
    }
}
