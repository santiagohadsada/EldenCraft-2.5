package com.tuusuario.mmo.eldencraft.character.gui;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.character.CharacterProfile;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * GUI Selector de Personajes de 27 slots (3 filas x 9 columnas). SPEC 15.2 / F3B-1.
 */
public class CharacterSelectorGUI implements InventoryHolder, Listener {

    private final EldenCraft plugin;
    private final Player player;
    private Inventory inventory;

    public CharacterSelectorGUI(EldenCraft plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public void open() {
        this.inventory = Bukkit.createInventory(this, 27, ChatColor.DARK_GRAY + "➤ " + ChatColor.GOLD + "Personajes (Slots 1-27)");
        refresh();
        player.openInventory(this.inventory);
    }

    public void refresh() {
        if (inventory == null) return;
        inventory.clear();

        List<CharacterProfile> profiles = plugin.getCharacterManager().getProfiles(player.getUniqueId());
        Map<Integer, CharacterProfile> slotMap = new HashMap<>();
        for (CharacterProfile p : profiles) {
            slotMap.put(p.getSlot(), p);
        }

        String activeCharId = plugin.getCharacterManager().getActiveCharacterId(player.getUniqueId());

        for (int i = 0; i < 27; i++) {
            int slotNum = i + 1;
            CharacterProfile prof = slotMap.get(slotNum);

            if (prof == null) {
                // Slot vacío
                ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(ChatColor.GRAY + "Slot " + slotNum + ": " + ChatColor.DARK_GRAY + "[Vacío]");
                    meta.setLore(Arrays.asList(
                            ChatColor.YELLOW + "Click izquierdo: " + ChatColor.WHITE + "Crear nuevo personaje",
                            ChatColor.DARK_GRAY + "Slot disponible de 1 a 27"
                    ));
                    item.setItemMeta(meta);
                }
                inventory.setItem(i, item);
            } else if (prof.isDeleted()) {
                // Personaje eliminado en periodo de gracia
                ItemStack item = new ItemStack(Material.BARRIER);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(ChatColor.RED + "Slot " + slotNum + ": " + prof.getName() + " [ELIMINADO]");
                    long remainingDays = 7;
                    if (prof.getDeletedAt() != null) {
                        long elapsedMs = System.currentTimeMillis() - prof.getDeletedAt();
                        remainingDays = Math.max(0, 7 - (elapsedMs / (24L * 3600L * 1000L)));
                    }
                    meta.setLore(Arrays.asList(
                            ChatColor.GRAY + "Raza: " + (prof.getRace() != null ? prof.getRace().displayName : "Desconocida"),
                            ChatColor.GRAY + "Clase: " + (prof.getPlayerClass() != null ? prof.getPlayerClass().displayName : "Desconocida") + " Niv " + prof.getLevel(),
                            ChatColor.RED + "Gracia restante: " + remainingDays + " días",
                            "",
                            ChatColor.GREEN + "Click: " + ChatColor.WHITE + "RECUPERAR personaje"
                    ));
                    item.setItemMeta(meta);
                }
                inventory.setItem(i, item);
            } else {
                // Personaje activo o inactivo
                boolean isActive = prof.getCharacterId().equalsIgnoreCase(activeCharId);
                Material mat = isActive ? Material.GOLDEN_HELMET : Material.IRON_HELMET;
                ItemStack item = new ItemStack(mat);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    String prefix = isActive ? (ChatColor.GOLD + "✦ ") : (ChatColor.WHITE + "");
                    String suffix = isActive ? (ChatColor.GREEN + " (ACTIVO)") : "";
                    meta.setDisplayName(prefix + "Slot " + slotNum + ": " + ChatColor.YELLOW + prof.getName() + suffix);

                    List<String> lore = new ArrayList<>();
                    lore.add(ChatColor.GRAY + "Raza: " + ChatColor.WHITE + (prof.getRace() != null ? prof.getRace().displayName : "N/A"));
                    lore.add(ChatColor.GRAY + "Clase: " + ChatColor.WHITE + (prof.getPlayerClass() != null ? prof.getPlayerClass().displayName : "N/A") + " Niv " + prof.getLevel());
                    lore.add(ChatColor.GRAY + "Campaña: " + ChatColor.AQUA + prof.getCampaignId());
                    lore.add(ChatColor.GRAY + "Alineamiento: " + ChatColor.WHITE + prof.getAlignment());
                    lore.add(ChatColor.GRAY + "PG: " + ChatColor.RED + prof.getHpCurrent() + "/" + prof.getHpMax() + ChatColor.GRAY + " | Oro: " + ChatColor.GOLD + prof.getGold());
                    lore.add(ChatColor.DARK_GRAY + "------------------------");
                    lore.add(ChatColor.GREEN + "Click izquierdo: " + ChatColor.WHITE + (isActive ? "Entrar al juego" : "Seleccionar personaje"));
                    lore.add(ChatColor.GOLD + "Click derecho: " + ChatColor.WHITE + "Menú de acciones");
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                }
                inventory.setItem(i, item);
            }
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public static class SelectorListener implements Listener {
        private final EldenCraft plugin;

        public SelectorListener(EldenCraft plugin) {
            this.plugin = plugin;
        }

        @EventHandler
        public void onInventoryClick(InventoryClickEvent e) {
            if (!(e.getWhoClicked() instanceof Player player)) return;
            if (e.getInventory().getHolder() instanceof CharacterSelectorGUI gui) {
                e.setCancelled(true);
                int rawSlot = e.getRawSlot();
                if (rawSlot < 0 || rawSlot >= 27) return;

                int slotNum = rawSlot + 1;
                List<CharacterProfile> profiles = plugin.getCharacterManager().getProfiles(player.getUniqueId());
                CharacterProfile target = null;
                for (CharacterProfile p : profiles) {
                    if (p.getSlot() == slotNum) {
                        target = p;
                        break;
                    }
                }

                if (target == null) {
                    // Click en slot vacío: abrir creador
                    player.closeInventory();
                    new CharacterCreationWizard(plugin, player, slotNum).start();
                    return;
                }

                if (target.isDeleted()) {
                    // Recuperar
                    boolean ok = plugin.getCharacterManager().recoverCharacter(player, target.getCharacterId());
                    if (ok) {
                        player.sendMessage(ChatColor.GREEN + "¡Personaje " + target.getName() + " recuperado!");
                        gui.refresh();
                    }
                    return;
                }

                if (e.getClick() == ClickType.RIGHT) {
                    // Abrir menú de acciones
                    player.closeInventory();
                    new CharacterActionsGUI(plugin, player, target).open();
                } else {
                    // Click izquierdo: Seleccionar personaje
                    player.closeInventory();
                    plugin.getCharacterManager().selectCharacter(player, target.getCharacterId());
                }
            }
        }
    }
}
