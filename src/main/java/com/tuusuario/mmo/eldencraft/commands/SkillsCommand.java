package com.tuusuario.mmo.eldencraft.commands;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.listeners.SkillsGUI;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SkillsCommand implements CommandExecutor {

    private final EldenCraft plugin;

    public SkillsCommand(EldenCraft plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;
        PlayerData data = plugin.getPlayerManager().getPlayerData(p.getUniqueId());
        if (data == null || data.getPlayerClass() == null) {
            p.sendMessage(ChatColor.RED + "Aun no has elegido raza/clase.");
            return true;
        }
        new SkillsGUI(plugin).open(p, data);
        return true;
    }
}
