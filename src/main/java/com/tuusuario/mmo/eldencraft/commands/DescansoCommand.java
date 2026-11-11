package com.tuusuario.mmo.eldencraft.commands;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Solicita o concede un descanso corto o largo D&D 5e (/descanso <corto|largo> [jugador]).
 */
public class DescansoCommand implements CommandExecutor {

    private final EldenCraft plugin;

    public DescansoCommand(EldenCraft plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "Uso: /descanso <corto|largo> [jugador]");
            return true;
        }

        String tipo = args[0].toLowerCase();
        if (!tipo.equals("corto") && !tipo.equals("largo")) {
            MessageUtil.send(sender, "rest.invalid-type");
            return true;
        }

        // Si es DM y pasa un jugador como segundo argumento, concede el descanso directamente
        if (args.length > 1 && sender.hasPermission("eldencraft.dm")) {
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Jugador no encontrado: " + args[1]);
                return true;
            }
            if (tipo.equals("corto")) {
                plugin.getRestManager().grantShortRest(target);
                sender.sendMessage(ChatColor.GREEN + "Descanso corto concedido a " + target.getName());
            } else {
                plugin.getRestManager().grantLongRest(target);
                sender.sendMessage(ChatColor.GREEN + "Descanso largo concedido a " + target.getName());
            }
            return true;
        }

        if (!(sender instanceof Player player)) {
            MessageUtil.send(sender, "player-only");
            return true;
        }

        // Jugador solicita descanso al DM
        if (tipo.equals("corto")) {
            plugin.getRestManager().requestShortRest(player);
        } else {
            plugin.getRestManager().requestLongRest(player);
        }

        return true;
    }
}
