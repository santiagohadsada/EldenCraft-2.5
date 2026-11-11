package com.tuusuario.mmo.eldencraft.commands;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.models.ClassType;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import com.tuusuario.mmo.eldencraft.models.RaceType;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MMOCommand implements CommandExecutor {

    private final EldenCraft plugin;

    public MMOCommand(EldenCraft plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;
        PlayerData data = plugin.getPlayerManager().getPlayerData(p.getUniqueId());
        if (data == null) {
            p.sendMessage(ChatColor.RED + "Tu ficha aun no esta lista, intentalo en un segundo.");
            return true;
        }

        if (args.length == 0) {
            sendInfo(p, data);
            return true;
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("elegir")) {
            try {
                RaceType raza = RaceType.valueOf(args[1].toUpperCase());
                ClassType clase = ClassType.valueOf(args[2].toUpperCase());
                data.setRace(raza);
                data.setPlayerClass(clase);
                com.tuusuario.mmo.eldencraft.managers.ExperienceManager.applyMaxHealth(p, data);
                try {
                    org.bukkit.attribute.AttributeInstance scale = p.getAttribute(org.bukkit.attribute.Attribute.GENERIC_SCALE);
                    if (scale != null) scale.setBaseValue(raza.scale);
                } catch (Throwable ignored) {}
                plugin.getFlyManager().sendFakeElytra(p, false);
                p.sendMessage(ChatColor.GREEN + "¡Ahora eres un " + raza.displayName + " " + clase.displayName + "!");
            } catch (IllegalArgumentException e) {
                p.sendMessage(ChatColor.RED + "Raza o Clase invalida.");
            }
            return true;
        }

        p.sendMessage(ChatColor.YELLOW + "Usa: /mmo  o  /mmo elegir <RAZA> <CLASE>");
        return true;
    }

    private void sendInfo(Player p, PlayerData d) {
        p.sendMessage(ChatColor.GOLD + "═══════ EldenCraft ═══════");
        p.sendMessage(ChatColor.YELLOW + "Raza: " + ChatColor.WHITE + (d.getRace() != null ? d.getRace().displayName : "?"));
        p.sendMessage(ChatColor.YELLOW + "Clase: " + ChatColor.WHITE + (d.getPlayerClass() != null ? d.getPlayerClass().displayName : "?"));
        p.sendMessage(ChatColor.YELLOW + "Nivel: " + ChatColor.WHITE + d.getLevel()
                + ChatColor.GRAY + " (EXP " + d.getXp() + "/" + plugin.getExperienceManager().getRequiredExp(d.getLevel()) + ")");
        p.sendMessage(ChatColor.YELLOW + "Oro: " + ChatColor.GOLD + d.getGold());
        p.sendMessage(ChatColor.YELLOW + "Vida max: " + ChatColor.WHITE + (int)(d.getMaxHealth() / 2) + " corazones");
        p.sendMessage(ChatColor.YELLOW + "Misiones activas: " + ChatColor.WHITE + d.getActiveMissions().size()
                + ChatColor.GRAY + "  Completadas: " + d.getCompletedMissions().size());
        p.sendMessage(ChatColor.GRAY + "Comandos: /shop /missions /balance");
    }
}
