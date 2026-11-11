package com.tuusuario.mmo.eldencraft.campaign.gui;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.campaign.Campaign;
import com.tuusuario.mmo.eldencraft.campaign.CampaignStatus;
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
import java.util.List;

/**
 * GUI para edición y configuración de una campaña (SPEC 15.4 / C3).
 */
public class CampaignEditorGUI implements InventoryHolder {

    private final EldenCraft plugin;
    private final Player player;
    private final Campaign campaign;
    private Inventory inventory;

    public CampaignEditorGUI(EldenCraft plugin, Player player, Campaign campaign) {
        this.plugin = plugin;
        this.player = player;
        this.campaign = campaign;
    }

    public Campaign getCampaign() {
        return campaign;
    }

    public void open() {
        this.inventory = Bukkit.createInventory(this, 27, ChatColor.DARK_GRAY + "Editar: " + ChatColor.GOLD + campaign.getName());
        refresh();
        player.openInventory(this.inventory);
    }

    public void refresh() {
        inventory.clear();

        // Slot 10: Estado
        inventory.setItem(10, createItem(Material.BEACON, ChatColor.GOLD + "Estado de la Campaña",
                ChatColor.GRAY + "Estado actual: " + ChatColor.AQUA + campaign.getStatus().displayName,
                "",
                ChatColor.YELLOW + "Click izquierdo: siguiente estado",
                ChatColor.GRAY + "(BORRADOR ➔ ABIERTA ➔ SOLO_INVITADOS ➔ CERRADA ➔ ARCHIVADA)"));

        // Slot 12: Límite de Jugadores
        inventory.setItem(12, createItem(Material.PLAYER_HEAD, ChatColor.YELLOW + "Límite de Jugadores",
                ChatColor.GRAY + "Máximo actual: " + ChatColor.WHITE + campaign.getMaxPlayers(),
                "",
                ChatColor.GREEN + "Click izq: +1 jugador",
                ChatColor.RED + "Click der: -1 jugador"));

        // Slot 14: Rango de Niveles
        inventory.setItem(14, createItem(Material.EXPERIENCE_BOTTLE, ChatColor.GREEN + "Rango de Niveles",
                ChatColor.GRAY + "Nivel Mínimo: " + ChatColor.WHITE + campaign.getMinLevel(),
                ChatColor.GRAY + "Nivel Máximo: " + ChatColor.WHITE + campaign.getMaxLevel(),
                "",
                ChatColor.YELLOW + "Click izq: Subir Nivel Mín (+1)",
                ChatColor.AQUA + "Click der: Subir Nivel Máx (+1)"));

        // Slot 16: Reglas / PvP
        boolean pvp = Boolean.parseBoolean(plugin.getDataManager().getCampaignSetting(campaign.getId(), "pvp", "false"));
        inventory.setItem(16, createItem(Material.IRON_SWORD, ChatColor.RED + "Modo PvP de Campaña",
                ChatColor.GRAY + "PvP entre jugadores: " + (pvp ? ChatColor.GREEN + "ACTIVADO" : ChatColor.RED + "DESACTIVADO"),
                "",
                ChatColor.YELLOW + "Click para alternar combate entre jugadores"));

        // Slot 22: Guardar y Salir
        inventory.setItem(22, createItem(Material.ARROW, ChatColor.WHITE + "« Guardar y Salir",
                ChatColor.GRAY + "Aplica los cambios a la campaña."));
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

    public static class CampaignEditorListener implements Listener {
        private final EldenCraft plugin;

        public CampaignEditorListener(EldenCraft plugin) {
            this.plugin = plugin;
        }

        @EventHandler
        public void onInventoryClick(InventoryClickEvent e) {
            if (!(e.getWhoClicked() instanceof Player player)) return;
            if (e.getInventory().getHolder() instanceof CampaignEditorGUI gui) {
                e.setCancelled(true);
                Campaign c = gui.getCampaign();
                int rawSlot = e.getRawSlot();

                switch (rawSlot) {
                    case 10 -> { // Ciclar Estado
                        CampaignStatus next = switch (c.getStatus()) {
                            case BORRADOR -> CampaignStatus.ABIERTA;
                            case ABIERTA -> CampaignStatus.SOLO_INVITADOS;
                            case SOLO_INVITADOS -> CampaignStatus.EN_PAUSA;
                            case EN_PAUSA -> CampaignStatus.CERRADA;
                            case CERRADA -> CampaignStatus.ARCHIVADA;
                            case ARCHIVADA -> CampaignStatus.BORRADOR;
                        };
                        c.setStatus(next);
                        plugin.getDataManager().saveCampaign(c);
                        gui.refresh();
                        player.sendMessage(ChatColor.GREEN + "Estado cambiado a: " + next.displayName);
                    }
                    case 12 -> { // Límite de jugadores
                        int delta = e.isRightClick() ? -1 : 1;
                        int newLimit = Math.max(1, Math.min(32, c.getMaxPlayers() + delta));
                        c.setMaxPlayers(newLimit);
                        plugin.getDataManager().saveCampaign(c);
                        gui.refresh();
                    }
                    case 14 -> { // Niveles
                        if (e.isRightClick()) {
                            int newMax = c.getMaxLevel() >= 20 ? 1 : c.getMaxLevel() + 1;
                            c.setMaxLevel(newMax);
                        } else {
                            int newMin = c.getMinLevel() >= 20 ? 1 : c.getMinLevel() + 1;
                            c.setMinLevel(newMin);
                        }
                        plugin.getDataManager().saveCampaign(c);
                        gui.refresh();
                    }
                    case 16 -> { // PvP
                        boolean currentPvp = Boolean.parseBoolean(plugin.getDataManager().getCampaignSetting(c.getId(), "pvp", "false"));
                        plugin.getDataManager().setCampaignSetting(c.getId(), "pvp", String.valueOf(!currentPvp));
                        gui.refresh();
                        player.sendMessage(ChatColor.YELLOW + "PvP en " + c.getName() + " ahora está: " + (!currentPvp ? "ACTIVADO" : "DESACTIVADO"));
                    }
                    case 22 -> { // Salir
                        player.closeInventory();
                        player.sendMessage(ChatColor.GREEN + "✦ Cambios de campaña guardados con éxito.");
                    }
                }
            }
        }
    }
}
