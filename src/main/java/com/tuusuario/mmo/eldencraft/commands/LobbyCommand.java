package com.tuusuario.mmo.eldencraft.commands;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.character.gui.CharacterSelectorGUI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Comando /lobby (SPEC 15.3 / F3B-1).
 */
public class LobbyCommand implements CommandExecutor {

    private final EldenCraft plugin;

    public LobbyCommand(EldenCraft plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Este comando solo puede ser ejecutado por un jugador.");
            return true;
        }

        // Si ya está en el lobby, abrir el selector
        if (plugin.getLobbyManager().isInLobby(player)) {
            new CharacterSelectorGUI(plugin, player).open();
            return true;
        }

        // Guardar estado del personaje actual antes de ir al lobby
        String activeCharId = plugin.getCharacterManager().getActiveCharacterId(player.getUniqueId());
        if (activeCharId != null) {
            plugin.getCharacterManager().savePlayerToCharacter(player, activeCharId);
        }

        // Teletransportar al lobby
        plugin.getLobbyManager().sendToLobby(player);
        player.sendMessage(ChatColor.GREEN + "Has regresado al Lobby de EldenCraft.");
        new CharacterSelectorGUI(plugin, player).open();
        return true;
    }
}
