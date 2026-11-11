package com.tuusuario.mmo.eldencraft.campaign.gui;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.campaign.Campaign;
import com.tuusuario.mmo.eldencraft.campaign.CampaignMember;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Panel GUI del Dungeon Master con lista de miembros y acciones rápidas (SPEC 15.5 / C3).
 */
public class DMCampaignPanelGUI implements InventoryHolder {

    private final EldenCraft plugin;
    private final Player dm;
    private final Campaign campaign;
    private Inventory inventory;
    private final List<CampaignMember> displayedMembers = new ArrayList<>();

    public DMCampaignPanelGUI(EldenCraft plugin, Player dm, Campaign campaign) {
        this.plugin = plugin;
        this.dm = dm;
        this.campaign = campaign;
    }

    public Campaign getCampaign() {
        return campaign;
    }

    public List<CampaignMember> getDisplayedMembers() {
        return displayedMembers;
    }

    public void open() {
        this.inventory = Bukkit.createInventory(this, 27, ChatColor.DARK_GRAY + "DM Panel: " + ChatColor.GOLD + campaign.getName());
        refresh();
        dm.openInventory(this.inventory);
    }

    public void refresh() {
        inventory.clear();
        displayedMembers.clear();

        List<CampaignMember> members = plugin.getCampaignManager().getMembers(campaign.getId());
        int slot = 0;

        for (CampaignMember m : members) {
            if (slot >= 22) break;
            displayedMembers.add(m);

            Player online = Bukkit.getPlayer(m.getPlayerUuid());
            boolean isOnline = online != null && online.isOnline();
            String pName = isOnline ? online.getName() : Bukkit.getOfflinePlayer(m.getPlayerUuid()).getName();
            if (pName == null) pName = "Jugador";

            CharacterProfile prof = null;
            if (m.getCharacterId() != null) {
                prof = plugin.getDataManager().getCharacterProfile(m.getCharacterId());
            }
            if (prof == null) {
                List<CharacterProfile> list = plugin.getDataManager().getCharacterProfiles(m.getPlayerUuid());
                if (!list.isEmpty()) prof = list.get(0);
            }

            ItemStack item = new ItemStack(isOnline ? Material.PLAYER_HEAD : Material.SKELETON_SKULL);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName((isOnline ? ChatColor.GREEN : ChatColor.GRAY) + pName + " (" + m.getRole().displayName + ")");
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Estado: " + (isOnline ? ChatColor.GREEN + "En línea" : ChatColor.RED + "Desconectado"));

                if (prof != null) {
                    lore.add(ChatColor.WHITE + "Personaje: " + ChatColor.YELLOW + prof.getName());
                    lore.add(ChatColor.WHITE + "Clase: " + ChatColor.AQUA + (prof.getPlayerClass() != null ? prof.getPlayerClass().displayName : "N/A") + " Niv " + prof.getLevel());
                    lore.add(ChatColor.WHITE + "PG: " + ChatColor.RED + prof.getHpCurrent() + "/" + prof.getHpMax() + " | Oro: " + ChatColor.GOLD + prof.getGold());
                } else {
                    lore.add(ChatColor.DARK_GRAY + "(Sin personaje activo)");
                }

                lore.add(ChatColor.DARK_GRAY + "------------------------");
                lore.add(ChatColor.YELLOW + "Click izquierdo: " + ChatColor.WHITE + "Ver Ficha Completa");
                lore.add(ChatColor.GOLD + "Click derecho: " + ChatColor.WHITE + "Otorgar +100 XP rápida");
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            inventory.setItem(slot++, item);
        }

        // Slot 22: Ajustes de Campaña (Abrir Editor GUI)
        ItemStack settings = new ItemStack(Material.REPEATER);
        ItemMeta ms = settings.getItemMeta();
        if (ms != null) {
            ms.setDisplayName(ChatColor.GOLD + "Ajustes de la Campaña");
            ms.setLore(List.of(ChatColor.YELLOW + "Click para abrir editor de campaña"));
            settings.setItemMeta(ms);
        }
        inventory.setItem(22, settings);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public static class DMCampaignPanelListener implements Listener {
        private final EldenCraft plugin;

        public DMCampaignPanelListener(EldenCraft plugin) {
            this.plugin = plugin;
        }

        @EventHandler
        public void onInventoryClick(InventoryClickEvent e) {
            if (!(e.getWhoClicked() instanceof Player dm)) return;
            if (e.getInventory().getHolder() instanceof DMCampaignPanelGUI gui) {
                e.setCancelled(true);
                int rawSlot = e.getRawSlot();

                if (rawSlot == 22) {
                    dm.closeInventory();
                    new CampaignEditorGUI(plugin, dm, gui.getCampaign()).open();
                    return;
                }

                if (rawSlot >= 0 && rawSlot < gui.getDisplayedMembers().size()) {
                    CampaignMember m = gui.getDisplayedMembers().get(rawSlot);
                    String pName = Bukkit.getOfflinePlayer(m.getPlayerUuid()).getName();
                    if (pName == null) pName = m.getPlayerUuid().toString();

                    if (e.isRightClick()) {
                        // Dar +100 XP rápida
                        dm.performCommand("dm xp " + pName + " 100 " + gui.getCampaign().getId());
                        gui.refresh();
                    } else {
                        // Ver Ficha
                        dm.closeInventory();
                        dm.performCommand("dm ficha " + pName + " " + gui.getCampaign().getId());
                    }
                }
            }
        }
    }
}
