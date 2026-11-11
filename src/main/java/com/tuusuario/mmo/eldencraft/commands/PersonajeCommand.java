package com.tuusuario.mmo.eldencraft.commands;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.character.CharacterProfile;
import com.tuusuario.mmo.eldencraft.character.gui.CharacterSelectorGUI;
import com.tuusuario.mmo.eldencraft.models.ClassType;
import com.tuusuario.mmo.eldencraft.models.RaceType;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Comando /personaje <crear|cambiar|borrar|recuperar|info|duplicar|exportar> (SPEC 15.2 / F3B-1).
 */
public class PersonajeCommand implements CommandExecutor {

    private final EldenCraft plugin;

    public PersonajeCommand(EldenCraft plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Este comando solo puede ser ejecutado por un jugador.");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("menu")) {
            new CharacterSelectorGUI(plugin, player).open();
            return true;
        }

        String sub = args[0].toLowerCase();
        var charMgr = plugin.getCharacterManager();

        switch (sub) {
            case "cambiar" -> {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Uso: /personaje cambiar <slot (1-27)>");
                    return true;
                }
                try {
                    int slot = Integer.parseInt(args[1]);
                    String charId = player.getUniqueId().toString() + "_" + slot;
                    charMgr.selectCharacter(player, charId);
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "El slot debe ser un número entre 1 y 27.");
                }
            }
            case "borrar" -> {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Uso: /personaje borrar <slot (1-27)>");
                    return true;
                }
                try {
                    int slot = Integer.parseInt(args[1]);
                    String charId = player.getUniqueId().toString() + "_" + slot;
                    boolean ok = charMgr.softDeleteCharacter(player, charId);
                    if (ok) {
                        player.sendMessage(ChatColor.YELLOW + "Personaje en slot " + slot + " eliminado (recuperable durante 7 días).");
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "El slot debe ser un número entre 1 y 27.");
                }
            }
            case "recuperar" -> {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Uso: /personaje recuperar <slot (1-27)>");
                    return true;
                }
                try {
                    int slot = Integer.parseInt(args[1]);
                    String charId = player.getUniqueId().toString() + "_" + slot;
                    boolean ok = charMgr.recoverCharacter(player, charId);
                    if (ok) {
                        player.sendMessage(ChatColor.GREEN + "¡Personaje en slot " + slot + " recuperado!");
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "El slot debe ser un número entre 1 y 27.");
                }
            }
            case "duplicar" -> {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Uso: /personaje duplicar <slot>");
                    return true;
                }
                try {
                    int slot = Integer.parseInt(args[1]);
                    String charId = player.getUniqueId().toString() + "_" + slot;
                    CharacterProfile dup = charMgr.duplicateCharacterLevel1(player, charId);
                    if (dup != null) {
                        player.sendMessage(ChatColor.GREEN + "¡Personaje duplicado a nivel 1 en el slot " + dup.getSlot() + "!");
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Número de slot inválido.");
                }
            }
            case "exportar" -> {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Uso: /personaje exportar <slot>");
                    return true;
                }
                try {
                    int slot = Integer.parseInt(args[1]);
                    String charId = player.getUniqueId().toString() + "_" + slot;
                    String json = charMgr.exportCharacterToJson(charId);
                    player.sendMessage(ChatColor.GREEN + "--- JSON Personaje Slot " + slot + " ---");
                    player.sendMessage(ChatColor.GRAY + json);
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Número de slot inválido.");
                }
            }
            case "renombrar" -> {
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Uso: /personaje renombrar <slot> <nuevo_nombre>");
                    return true;
                }
                try {
                    int slot = Integer.parseInt(args[1]);
                    String newName = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
                    String charId = player.getUniqueId().toString() + "_" + slot;
                    charMgr.renameCharacter(player, charId, newName);
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "El slot debe ser un número entero (1-27).");
                }
            }
            case "importar" -> {
                int targetSlot = -1;
                String json = null;
                if (args.length >= 2) {
                    try {
                        targetSlot = Integer.parseInt(args[1]);
                        if (args.length >= 3) {
                            json = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
                        }
                    } catch (NumberFormatException e) {
                        json = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
                    }
                }

                if (targetSlot == -1) {
                    List<CharacterProfile> profiles = charMgr.getProfiles(player.getUniqueId());
                    for (int s = 1; s <= charMgr.getMaxSlots(); s++) {
                        final int checkSlot = s;
                        if (profiles.stream().noneMatch(p -> p.getSlot() == checkSlot)) {
                            targetSlot = s;
                            break;
                        }
                    }
                }

                if (targetSlot == -1) {
                    player.sendMessage(ChatColor.RED + "No tienes slots libres disponibles para importar.");
                    return true;
                }

                if (json == null || json.isBlank()) {
                    final int freeSlot = targetSlot;
                    player.sendMessage(ChatColor.GOLD + "Pega el texto JSON de tu personaje en el chat:");
                    var importListener = new org.bukkit.event.Listener() {
                        @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.LOWEST)
                        public void onChat(org.bukkit.event.player.AsyncPlayerChatEvent chatEvt) {
                            if (!chatEvt.getPlayer().getUniqueId().equals(player.getUniqueId())) return;
                            chatEvt.setCancelled(true);
                            org.bukkit.event.HandlerList.unregisterAll(this);
                            String raw = chatEvt.getMessage().trim();
                            org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                                CharacterProfile imported = charMgr.importFromJson(player.getUniqueId(), freeSlot, raw);
                                if (imported != null) {
                                    player.sendMessage(ChatColor.GREEN + "¡Personaje " + imported.getName() + " importado con éxito en slot " + freeSlot + "!");
                                    charMgr.selectCharacter(player, imported.getCharacterId());
                                } else {
                                    player.sendMessage(ChatColor.RED + "Error al importar personaje: formato JSON inválido.");
                                }
                            });
                        }
                    };
                    org.bukkit.Bukkit.getPluginManager().registerEvents(importListener, plugin);
                    return true;
                }

                CharacterProfile imported = charMgr.importFromJson(player.getUniqueId(), targetSlot, json);
                if (imported != null) {
                    player.sendMessage(ChatColor.GREEN + "¡Personaje " + imported.getName() + " importado con éxito en slot " + targetSlot + "!");
                    charMgr.selectCharacter(player, imported.getCharacterId());
                } else {
                    player.sendMessage(ChatColor.RED + "Error al importar personaje: formato JSON inválido.");
                }
            }
            case "crear" -> {
                if (args.length < 4) {
                    player.sendMessage(ChatColor.RED + "Uso: /personaje crear <nombre> <raza> <clase>");
                    return true;
                }
                String name = args[1];
                RaceType race = null;
                for (RaceType r : RaceType.values()) {
                    if (r.name().equalsIgnoreCase(args[2]) || r.displayName.equalsIgnoreCase(args[2])) {
                        race = r;
                        break;
                    }
                }
                ClassType clazz = null;
                for (ClassType c : ClassType.values()) {
                    if (c.name().equalsIgnoreCase(args[3]) || c.displayName.equalsIgnoreCase(args[3])) {
                        clazz = c;
                        break;
                    }
                }

                if (race == null || clazz == null) {
                    player.sendMessage(ChatColor.RED + "Raza o clase no válida.");
                    return true;
                }

                // Encontrar primer slot libre
                List<CharacterProfile> profiles = charMgr.getProfiles(player.getUniqueId());
                int freeSlot = -1;
                for (int s = 1; s <= charMgr.getMaxSlots(); s++) {
                    final int checkSlot = s;
                    if (profiles.stream().noneMatch(p -> p.getSlot() == checkSlot)) {
                        freeSlot = s;
                        break;
                    }
                }

                if (freeSlot == -1) {
                    player.sendMessage(ChatColor.RED + "No tienes slots libres disponibles (máximo " + charMgr.getMaxSlots() + ").");
                    return true;
                }

                CharacterProfile created = charMgr.createCharacter(
                        player.getUniqueId(), freeSlot, name, race, clazz, null, null, "Neutral", "Aventurero"
                );
                player.sendMessage(ChatColor.GREEN + "¡Personaje " + name + " creado con éxito en slot " + freeSlot + "!");
                charMgr.selectCharacter(player, created.getCharacterId());
            }
            case "info" -> {
                int slot = 1;
                if (args.length >= 2) {
                    try { slot = Integer.parseInt(args[1]); } catch (NumberFormatException ignored) {}
                }
                String charId = player.getUniqueId().toString() + "_" + slot;
                CharacterProfile p = charMgr.getProfile(charId);
                if (p == null) {
                    player.sendMessage(ChatColor.RED + "No existe personaje en el slot " + slot + ".");
                    return true;
                }
                player.sendMessage(ChatColor.GOLD + "=== Slot " + slot + ": " + p.getName() + " ===");
                player.sendMessage(ChatColor.WHITE + "Raza: " + (p.getRace() != null ? p.getRace().displayName : "N/A"));
                player.sendMessage(ChatColor.WHITE + "Clase: " + (p.getPlayerClass() != null ? p.getPlayerClass().displayName : "N/A") + " Niv " + p.getLevel());
                player.sendMessage(ChatColor.WHITE + "PG: " + p.getHpCurrent() + "/" + p.getHpMax() + " | Oro: " + p.getGold());
                player.sendMessage(ChatColor.WHITE + "Campaña: " + p.getCampaignId());
                player.sendMessage(ChatColor.WHITE + "Alineamiento: " + p.getAlignment());
            }
            default -> {
                player.sendMessage(ChatColor.YELLOW + "Comandos de /personaje:");
                player.sendMessage(ChatColor.GRAY + "  /personaje menu");
                player.sendMessage(ChatColor.GRAY + "  /personaje cambiar <slot>");
                player.sendMessage(ChatColor.GRAY + "  /personaje crear <nombre> <raza> <clase>");
                player.sendMessage(ChatColor.GRAY + "  /personaje borrar <slot>");
                player.sendMessage(ChatColor.GRAY + "  /personaje recuperar <slot>");
                player.sendMessage(ChatColor.GRAY + "  /personaje duplicar <slot>");
                player.sendMessage(ChatColor.GRAY + "  /personaje exportar <slot>");
                player.sendMessage(ChatColor.GRAY + "  /personaje info [slot]");
            }
        }
        return true;
    }
}
