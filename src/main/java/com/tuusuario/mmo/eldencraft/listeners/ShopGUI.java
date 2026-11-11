package com.tuusuario.mmo.eldencraft.listeners;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.managers.ShopManager;
import com.tuusuario.mmo.eldencraft.models.ClassType;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

// Menu de la tienda.
public class ShopGUI implements Listener {

    public static final String MAIN_TITLE = "§8➤ §lTienda MMO";
    public static final String CLASS_TITLE_PREFIX = "§8➤ §lTienda - ";

    private final EldenCraft plugin;

    public ShopGUI(EldenCraft plugin) {
        this.plugin = plugin;
    }

    // Pantalla principal: 1 botón por clase.
    public void openMain(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, MAIN_TITLE);
        inv.setItem(0, classButton(Material.IRON_SWORD,        ChatColor.RED   + "Guerrero",  ClassType.WARRIOR));
        inv.setItem(1, classButton(Material.BLAZE_ROD,         ChatColor.AQUA  + "Mago",       ClassType.MAGE));
        inv.setItem(2, classButton(Material.BOW,               ChatColor.GREEN + "Explorador", ClassType.RANGER));
        inv.setItem(3, classButton(Material.GOLDEN_CHESTPLATE, ChatColor.YELLOW+ "Paladin",    ClassType.PALADIN));
        inv.setItem(4, classButton(Material.IRON_NUGGET,       ChatColor.GRAY  + "Ladron",     ClassType.ROGUE));
        inv.setItem(5, classButton(Material.JUKEBOX,           ChatColor.LIGHT_PURPLE + "Bardo", ClassType.BARD));
        inv.setItem(6, classButton(Material.NETHERITE_AXE,     ChatColor.DARK_RED + "Barbaro", ClassType.BARBARIAN));
        player.openInventory(inv);
    }

    private ItemStack classButton(Material mat, String name, ClassType clazz) {
        ItemStack s = new ItemStack(mat);
        ItemMeta m = s.getItemMeta();
        m.setDisplayName(name);
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Tienda de la clase " + clazz.displayName);
        lore.add(ChatColor.YELLOW + "Click para entrar");
        m.setLore(lore);
        s.setItemMeta(m);
        return s;
    }

    public void openClassShop(Player player, ClassType clazz) {
        List<ShopManager.ShopItem> items = plugin.getShopManager().getItemsForClass(clazz);
        // Calculamos filas para los items + 1 fila inferior (info + volver)
        int itemRows = Math.max(1, (int) Math.ceil(items.size() / 9.0));
        itemRows = Math.min(itemRows, 5);
        int rows = itemRows + 1;
        Inventory inv = Bukkit.createInventory(null, rows * 9, CLASS_TITLE_PREFIX + clazz.displayName);
        int slot = 0;
        int itemSlots = itemRows * 9;
        for (ShopManager.ShopItem si : items) {
            if (slot >= itemSlots) break;
            inv.setItem(slot++, si.item.clone());
        }

        // Fila inferior
        int base = itemRows * 9;
        // Relleno gris
        for (int i = 0; i < 9; i++) inv.setItem(base + i, pane());
        // Boton volver (slot 0 de la fila inferior)
        inv.setItem(base, navItem(Material.ARROW, "§e« Volver a clases", "§7Click para regresar"));
        // Info de saldo (slot 4)
        com.tuusuario.mmo.eldencraft.models.PlayerData data =
            plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        long oro = data == null ? 0 : data.getGold();
        inv.setItem(base + 4, navItem(Material.GOLD_INGOT,
                "§6§l♦ Tu saldo: " + oro + " oro",
                "§7Compras restan oro automaticamente"));
        // Cerrar (slot 8)
        inv.setItem(base + 8, navItem(Material.BARRIER, "§c✖ Cerrar", "§7Click para salir"));

        player.openInventory(inv);
    }

    private ItemStack pane() {
        ItemStack s = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta m = s.getItemMeta();
        if (m != null) { m.setDisplayName(" "); s.setItemMeta(m); }
        return s;
    }

    private ItemStack navItem(Material mat, String name, String... lore) {
        ItemStack s = new ItemStack(mat);
        ItemMeta m = s.getItemMeta();
        if (m != null) {
            m.setDisplayName(name);
            if (lore.length > 0) m.setLore(java.util.List.of(lore));
            s.setItemMeta(m);
        }
        return s;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        String title = e.getView().getTitle();
        if (title == null) return;

        // Pantalla principal: clic en clase -> abrir submenu
        if (title.equals(MAIN_TITLE)) {
            e.setCancelled(true);
            ItemStack it = e.getCurrentItem();
            if (it == null || !it.hasItemMeta() || !it.getItemMeta().hasDisplayName()) return;
            String name = ChatColor.stripColor(it.getItemMeta().getDisplayName());
            ClassType clazz = matchClass(name);
            if (clazz != null) openClassShop((Player) e.getWhoClicked(), clazz);
            return;
        }

        // Submenu de clase: comprar
        if (title.startsWith(CLASS_TITLE_PREFIX)) {
            e.setCancelled(true);
            Player p = (Player) e.getWhoClicked();
            ItemStack it = e.getCurrentItem();
            if (it == null || !it.hasItemMeta()) return;

            // Botones de navegacion (volver / saldo / cerrar)
            Material clicked = it.getType();
            if (clicked == Material.ARROW) { openMain(p); return; }
            if (clicked == Material.BARRIER) { p.closeInventory(); return; }
            if (clicked == Material.GOLD_INGOT
                && it.getItemMeta().hasDisplayName()
                && it.getItemMeta().getDisplayName().contains("Tu saldo")) return;
            if (clicked == Material.GRAY_STAINED_GLASS_PANE) return;

            String classDisplay = title.substring(CLASS_TITLE_PREFIX.length());
            ClassType clazz = matchClass(classDisplay);
            if (clazz == null) return;

            // Buscar item en la tienda por nombre exacto
            ShopManager.ShopItem found = null;
            for (ShopManager.ShopItem si : plugin.getShopManager().getItemsForClass(clazz)) {
                if (si.item.isSimilar(it)) { found = si; break; }
            }
            if (found == null) return;

            PlayerData data = plugin.getPlayerManager().getPlayerData(p.getUniqueId());
            if (data == null) return;
            if (!plugin.getEconomyManager().removeGold(data, found.price)) {
                p.sendMessage(ChatColor.RED + "No tienes suficiente oro (necesitas " + found.price + ").");
                p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                return;
            }
            // Entregar item limpio (sin el lore de precio)
            ItemStack give = found.item.clone();
            ItemMeta meta = give.getItemMeta();
            if (meta != null && meta.hasLore()) {
                List<String> lore = meta.getLore();
                if (lore != null) {
                    lore.removeIf(l -> l == null || l.contains("Precio:") || l.contains("Click izq para comprar"));
                    if (!lore.isEmpty() && lore.get(lore.size() - 1).isEmpty()) lore.remove(lore.size() - 1);
                    meta.setLore(lore);
                    give.setItemMeta(meta);
                }
            }
            p.getInventory().addItem(give);
            p.sendMessage(ChatColor.GREEN + "Has comprado " + ChatColor.WHITE + meta.getDisplayName()
                    + ChatColor.GRAY + " por " + ChatColor.GOLD + found.price + " oro.");
            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_YES, 1f, 1f);
        }
    }

    private ClassType matchClass(String displayName) {
        for (ClassType c : ClassType.values()) {
            if (c.displayName.equalsIgnoreCase(displayName)
             || c.name().equalsIgnoreCase(displayName)) return c;
        }
        // Busqueda mas flexible
        String lower = displayName.toLowerCase();
        if (lower.contains("guerrero"))   return ClassType.WARRIOR;
        if (lower.contains("mago"))       return ClassType.MAGE;
        if (lower.contains("explorador")) return ClassType.RANGER;
        if (lower.contains("paladin"))    return ClassType.PALADIN;
        if (lower.contains("ladron"))     return ClassType.ROGUE;
        if (lower.contains("bardo"))      return ClassType.BARD;
        if (lower.contains("barbaro"))    return ClassType.BARBARIAN;
        return null;
    }
}
