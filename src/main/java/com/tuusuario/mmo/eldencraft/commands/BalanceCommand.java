package com.tuusuario.mmo.eldencraft.commands;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BalanceCommand implements CommandExecutor {

    private final EldenCraft plugin;

    public BalanceCommand(EldenCraft plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;
        PlayerData data = plugin.getPlayerManager().getPlayerData(p.getUniqueId());
        if (data == null) {
            p.sendMessage(ChatColor.RED + "Tu ficha aun no esta cargada.");
            return true;
        }
        p.sendMessage(ChatColor.GOLD + "✦ Saldo: " + ChatColor.YELLOW + data.getGold() + " oro");
        return true;
    }
}
