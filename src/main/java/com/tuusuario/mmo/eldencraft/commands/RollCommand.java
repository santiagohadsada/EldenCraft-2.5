package com.tuusuario.mmo.eldencraft.commands;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.rules.DiceRoller;
import com.tuusuario.mmo.eldencraft.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * Comando para realizar tiradas de dados D&D 5e (/roll [expresion] [adv|dis]).
 */
public class RollCommand implements CommandExecutor {

    private final EldenCraft plugin;
    private final DiceRoller diceRoller = new DiceRoller();

    public RollCommand(EldenCraft plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String expr = args.length > 0 ? args[0] : "1d20";
        boolean advantage = false;
        boolean disadvantage = false;

        if (args.length > 1) {
            String mod = args[1].toLowerCase();
            if (mod.equals("adv") || mod.equals("ventaja")) advantage = true;
            else if (mod.equals("dis") || mod.equals("desventaja")) disadvantage = true;
        }

        DiceRoller.RollResult result;
        if (advantage) {
            result = diceRoller.rollWithAdvantage(expr);
        } else if (disadvantage) {
            result = diceRoller.rollWithDisadvantage(expr);
        } else {
            result = diceRoller.roll(expr);
        }

        String senderName = (sender instanceof Player p) ? p.getName() : "Consola";
        String tag = advantage ? " [Ventaja]" : (disadvantage ? " [Desventaja]" : "");

        String msg;
        if (result.isCritical) {
            msg = MessageUtil.get("roll.critical-hit", Map.of("expr", expr + tag, "total", String.valueOf(result.total)));
        } else if (result.isFumble) {
            msg = MessageUtil.get("roll.critical-fail", Map.of("expr", expr + tag));
        } else {
            msg = MessageUtil.get("roll.result", Map.of(
                    "expr", expr + tag,
                    "total", String.valueOf(result.total),
                    "desglose", result.breakdown()
            ));
        }

        String fullMessage = ChatColor.GOLD + "[" + senderName + "] " + msg;

        boolean isPublic = plugin.getConfig().getBoolean("rolls.public", true);
        if (sender instanceof Player p) {
            String campId = com.tuusuario.mmo.eldencraft.campaign.CampaignContext.getCampaignId(plugin, p);
            if (isPublic) {
                com.tuusuario.mmo.eldencraft.campaign.CampaignContext.broadcastToCampaign(plugin, campId, fullMessage);
            } else {
                sender.sendMessage(fullMessage);
                com.tuusuario.mmo.eldencraft.campaign.CampaignContext.broadcastToCampaignDMs(plugin, campId, fullMessage);
            }
        } else {
            if (isPublic) {
                Bukkit.broadcastMessage(fullMessage);
            } else {
                sender.sendMessage(fullMessage);
            }
        }

        return true;
    }
}
