package com.tuusuario.mmo.eldencraft.campaign.gui;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.campaign.Campaign;
import com.tuusuario.mmo.eldencraft.campaign.CampaignRole;
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

import java.util.*;

/**
 * Selector gráfico de Campañas (SPEC 15.4 / F3B-2).
 */
public class CampaignSelectorGUI implements InventoryHolder {

    private final EldenCraft plugin;
    private final Player player;
    private Inventory inventory;
    private final List<Campaign> displayedCampaigns = new ArrayList<>();

    public CampaignSelectorGUI(EldenCraft plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public void open() {
        this.inventory = Bukkit.createInventory(this, 27, ChatColor.DARK_GRAY + "➤ " + ChatColor.DARK_AQUA + "Campañas de EldenCraft");
        refresh();
        player.openInventory(this.inventory);
    }

    public void refresh() {
        inventory.clear();
        displayedCampaigns.clear();

        List<Campaign> campaigns = plugin.getCampaignManager().getAllCampaigns();
        var campMgr = plugin.getCampaignManager();
        boolean isAdmin = player.isOp() || player.hasPermission("eldencraft.admin");

        int slot = 0;
        for (Campaign c : campaigns) {
            if (slot >= 27) break;

            CampaignRole role = campMgr.getRole(c.getId(), player.getUniqueId());
            boolean isMemberOrDm = role != null;
            boolean hasInvite = plugin.getDataManager().hasPendingInvite(c.getId(), player.getUniqueId());

            // Filtrado según estado (C3: SPEC 15.4)
            if (c.getStatus() == CampaignStatus.SOLO_INVITADOS && !hasInvite && !isMemberOrDm && !isAdmin) {
                continue; // Un jugador no invitado no ve campañas SOLO_INVITADOS
            }
            if ((c.getStatus() == CampaignStatus.BORRADOR || c.getStatus() == CampaignStatus.ARCHIVADA) && !isMemberOrDm && !isAdmin) {
                continue; // Borradores y archivadas solo para miembros/DM/admin
            }

            displayedCampaigns.add(c);

            Material mat;
            try {
                mat = Material.valueOf(c.getIcon());
            } catch (Exception ignored) {
                mat = Material.COMPASS;
            }

            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GOLD + c.getName() + ChatColor.GRAY + " (" + c.getId() + ")");

                List<String> lore = new ArrayList<>();
                if (!c.getDescription().isBlank()) {
                    lore.add(ChatColor.GRAY + c.getDescription());
                }
                lore.add(ChatColor.YELLOW + "Estado: " + ChatColor.WHITE + c.getStatus().displayName);
                lore.add(ChatColor.YELLOW + "Niveles: " + ChatColor.WHITE + c.getMinLevel() + " - " + c.getMaxLevel());
                lore.add(ChatColor.YELLOW + "Límite: " + ChatColor.WHITE + campMgr.getMembers(c.getId()).size() + "/" + c.getMaxPlayers());
                lore.add(ChatColor.AQUA + "Tu rol: " + (role != null ? (ChatColor.GREEN + role.displayName) : (ChatColor.GRAY + "No miembro")));
                lore.add(ChatColor.DARK_GRAY + "------------------------");
                if (hasInvite) {
                    lore.add(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "✦ ¡TIENES UNA INVITACIÓN PENDIENTE!");
                    lore.add(ChatColor.GREEN + "Click: " + ChatColor.WHITE + "Aceptar invitación y entrar");
                } else if (role != null) {
                    lore.add(ChatColor.GREEN + "Click: " + ChatColor.WHITE + "Viajar al mundo de la campaña");
                } else {
                    lore.add(ChatColor.YELLOW + "Click: " + ChatColor.WHITE + "Intentar unirse");
                }
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            inventory.setItem(slot++, item);
        }
    }

    public List<Campaign> getDisplayedCampaigns() {
        return displayedCampaigns;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public static class CampaignGUIListener implements Listener {
        private final EldenCraft plugin;

        public CampaignGUIListener(EldenCraft plugin) {
            this.plugin = plugin;
        }

        @EventHandler
        public void onInventoryClick(InventoryClickEvent e) {
            if (!(e.getWhoClicked() instanceof Player player)) return;
            if (e.getInventory().getHolder() instanceof CampaignSelectorGUI gui) {
                e.setCancelled(true);
                int slot = e.getRawSlot();
                if (slot < 0 || slot >= gui.getDisplayedCampaigns().size()) return;

                Campaign c = gui.getDisplayedCampaigns().get(slot);
                var campMgr = plugin.getCampaignManager();
                CampaignRole role = campMgr.getRole(c.getId(), player.getUniqueId());

                player.closeInventory();
                if (role != null) {
                    // Ya es miembro: viajar a la campaña
                    campMgr.teleportToCampaignSpawn(player, c);
                } else {
                    // Unirse
                    campMgr.joinCampaign(player, c.getId());
                }
            }
        }
    }
}
