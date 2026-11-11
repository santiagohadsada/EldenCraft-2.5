package com.tuusuario.mmo.eldencraft.character.gui;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.character.CharacterProfile;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

/**
 * Menú de acciones contextuales para un personaje (SPEC 15.2 / F3B-1).
 */
public class CharacterActionsGUI implements InventoryHolder {

    private final EldenCraft plugin;
    private final Player player;
    private final CharacterProfile profile;
    private Inventory inventory;

    public CharacterActionsGUI(EldenCraft plugin, Player player, CharacterProfile profile) {
        this.plugin = plugin;
        this.player = player;
        this.profile = profile;
    }

    public CharacterProfile getProfile() {
        return profile;
    }

    public void open() {
        this.inventory = Bukkit.createInventory(this, 27, ChatColor.DARK_GRAY + "Opciones: " + ChatColor.GOLD + profile.getName());

        // Slot 10: Ver Ficha
        inventory.setItem(10, createItem(Material.BOOK, ChatColor.YELLOW + "Ver Ficha de Personaje",
                ChatColor.GRAY + "Abre la vista detallada de atributos,",
                ChatColor.GRAY + "habilidades, vida y estadísticas."));

        // Slot 11: Renombrar
        inventory.setItem(11, createItem(Material.NAME_TAG, ChatColor.GOLD + "Renombrar Personaje",
                ChatColor.GRAY + "Cambia el nombre de este personaje.",
                ChatColor.YELLOW + "Click para escribir nuevo nombre en el chat."));

        // Slot 12: Duplicar a nivel 1
        inventory.setItem(12, createItem(Material.ANVIL, ChatColor.AQUA + "Duplicar a Nivel 1",
                ChatColor.GRAY + "Copia la raza, clase y apariencia",
                ChatColor.GRAY + "a un nuevo slot libre con nivel 1."));

        // Slot 14: Exportar JSON
        inventory.setItem(14, createItem(Material.PAPER, ChatColor.GREEN + "Exportar a JSON",
                ChatColor.GRAY + "Genera un backup exportable",
                ChatColor.GRAY + "con todos los datos de la ficha."));

        // Slot 16: Eliminar Personaje
        inventory.setItem(16, createItem(Material.LAVA_BUCKET, ChatColor.RED + "Eliminar Personaje",
                ChatColor.GRAY + "Envía a la papelera con",
                ChatColor.GRAY + "período de gracia de 7 días."));

        // Slot 22: Volver
        inventory.setItem(22, createItem(Material.ARROW, ChatColor.WHITE + "« Volver al Selector",
                ChatColor.GRAY + "Regresar a la lista de personajes."));

        player.openInventory(this.inventory);
    }

    private ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public static class ActionsListener implements Listener {
        private final EldenCraft plugin;

        public ActionsListener(EldenCraft plugin) {
            this.plugin = plugin;
        }

        @EventHandler
        public void onInventoryClick(InventoryClickEvent e) {
            if (!(e.getWhoClicked() instanceof Player player)) return;
            if (e.getInventory().getHolder() instanceof CharacterActionsGUI gui) {
                e.setCancelled(true);
                int slot = e.getRawSlot();
                CharacterProfile prof = gui.getProfile();

                switch (slot) {
                    case 10 -> { // Ver Ficha
                        player.closeInventory();
                        player.performCommand("ficha");
                    }
                    case 11 -> { // Renombrar
                        player.closeInventory();
                        player.sendMessage(ChatColor.GOLD + "Escribe el nuevo nombre para " + prof.getName() + " en el chat (o 'cancelar'):");
                        var renameListener = new org.bukkit.event.Listener() {
                            @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.LOWEST)
                            public void onChat(org.bukkit.event.player.AsyncPlayerChatEvent chatEvt) {
                                if (!chatEvt.getPlayer().getUniqueId().equals(player.getUniqueId())) return;
                                chatEvt.setCancelled(true);
                                org.bukkit.event.HandlerList.unregisterAll(this);
                                String input = chatEvt.getMessage().trim();
                                if (input.equalsIgnoreCase("cancelar")) {
                                    player.sendMessage(ChatColor.RED + "Renombrado cancelado.");
                                    return;
                                }
                                Bukkit.getScheduler().runTask(plugin, () -> {
                                    plugin.getCharacterManager().renameCharacter(player, prof.getCharacterId(), input);
                                });
                            }
                        };
                        Bukkit.getPluginManager().registerEvents(renameListener, plugin);
                    }
                    case 12 -> { // Duplicar
                        player.closeInventory();
                        CharacterProfile dup = plugin.getCharacterManager().duplicateCharacterLevel1(player, prof.getCharacterId());
                        if (dup != null) {
                            player.sendMessage(ChatColor.GREEN + "¡Personaje duplicado en el slot " + dup.getSlot() + "!");
                        }
                    }
                    case 14 -> { // Exportar JSON
                        player.closeInventory();
                        String json = plugin.getCharacterManager().exportCharacterToJson(prof.getCharacterId());
                        player.sendMessage(ChatColor.GREEN + "--- JSON de Personaje (" + prof.getName() + ") ---");
                        player.sendMessage(ChatColor.GRAY + json);
                    }
                    case 16 -> { // Eliminar
                        player.closeInventory();
                        boolean ok = plugin.getCharacterManager().softDeleteCharacter(player, prof.getCharacterId());
                        if (ok) {
                            player.sendMessage(ChatColor.YELLOW + "Personaje " + prof.getName() + " eliminado (recuperable durante 7 días).");
                        }
                    }
                    case 22 -> { // Volver
                        player.closeInventory();
                        new CharacterSelectorGUI(plugin, player).open();
                    }
                }
            }
        }
    }
}
