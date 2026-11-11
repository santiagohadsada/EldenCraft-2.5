package com.tuusuario.mmo.eldencraft.commands;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import com.tuusuario.mmo.eldencraft.rules.CheckEngine;
import com.tuusuario.mmo.eldencraft.rules.CheckResult;
import com.tuusuario.mmo.eldencraft.rules.SkillDefinition;
import com.tuusuario.mmo.eldencraft.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * Comando para realizar pruebas de habilidad D&D 5e (/prueba <habilidad> [cd]).
 * También permite al DM exigir una prueba: /prueba <jugador> <habilidad> [cd].
 */
public class PruebaCommand implements CommandExecutor {

    private final EldenCraft plugin;
    private final CheckEngine checkEngine = new CheckEngine();

    public PruebaCommand(EldenCraft plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.GOLD + "=== Habilidades disponibles ===");
            for (SkillDefinition sd : SkillDefinition.values()) {
                sender.sendMessage(ChatColor.YELLOW + "/prueba " + sd.name().toLowerCase()
                        + ChatColor.GRAY + " (" + sd.nombre + " - " + sd.attribute + ")");
            }
            return true;
        }

        Player targetPlayer;
        SkillDefinition skill;
        int dc = 10;
        int skillArgIndex = 0;

        // Comprobar si el primer argumento es un jugador (modo DM)
        Player maybePlayer = Bukkit.getPlayer(args[0]);
        if (maybePlayer != null && args.length > 1 && (sender.hasPermission("eldencraft.dm") || (sender instanceof Player dmP && com.tuusuario.mmo.eldencraft.campaign.CampaignContext.isDmForPlayer(plugin, dmP, maybePlayer)))) {
            targetPlayer = maybePlayer;
            skillArgIndex = 1;
        } else {
            if (!(sender instanceof Player p)) {
                MessageUtil.send(sender, "player-only");
                return true;
            }
            targetPlayer = p;
        }

        skill = SkillDefinition.fromString(args[skillArgIndex]);
        if (skill == null) {
            sender.sendMessage(ChatColor.RED + "Habilidad desconocida: " + args[skillArgIndex]
                    + ". Usa /prueba para ver la lista.");
            return true;
        }

        if (args.length > skillArgIndex + 1) {
            try {
                dc = Integer.parseInt(args[skillArgIndex + 1]);
            } catch (NumberFormatException ignored) {}
        }

        PlayerData data = plugin.getPlayerManager().getPlayerData(targetPlayer.getUniqueId());
        if (data == null) {
            sender.sendMessage(ChatColor.RED + "No se encontraron datos para " + targetPlayer.getName());
            return true;
        }

        // Ejecutar prueba utilizando las competencias de habilidad del personaje
        boolean isProf = data.hasProficiency(skill);
        CheckResult result = checkEngine.performSkillCheck(data, skill, dc, isProf, false);

        String outcome = result.success ? MessageUtil.get("skill-check.success") : MessageUtil.get("skill-check.failure");

        String msg = MessageUtil.get("skill-check.result", Map.of(
                "habilidad", skill.nombre,
                "dado", String.valueOf(result.roll),
                "mod", (result.modifier >= 0 ? "+" : "") + result.modifier,
                "total", String.valueOf(result.total),
                "cd", String.valueOf(result.dc),
                "exito", outcome
        ));

        String fullMsg = ChatColor.GOLD + "[" + targetPlayer.getName() + "] " + msg;

        boolean isPublic = plugin.getConfig().getBoolean("rolls.public", true);
        String campId = com.tuusuario.mmo.eldencraft.campaign.CampaignContext.getCampaignId(plugin, targetPlayer);
        if (isPublic) {
            com.tuusuario.mmo.eldencraft.campaign.CampaignContext.broadcastToCampaign(plugin, campId, fullMsg);
        } else {
            targetPlayer.sendMessage(fullMsg);
            if (!sender.equals(targetPlayer)) {
                sender.sendMessage(fullMsg);
            }
            com.tuusuario.mmo.eldencraft.campaign.CampaignContext.broadcastToCampaignDMs(plugin, campId, fullMsg);
        }

        return true;
    }
}
