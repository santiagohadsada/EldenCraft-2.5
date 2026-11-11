package com.tuusuario.mmo.eldencraft.managers;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.models.ClassType;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// carga la tienda del shop.yml, cada clase tiene sus propios items
public class ShopManager {

    public static class ShopItem {
        public final ItemStack item;
        public final int price;

        public ShopItem(ItemStack item, int price) {
            this.item = item;
            this.price = price;
        }
    }

    private final EldenCraft plugin;
    private final Map<ClassType, List<ShopItem>> shop = new HashMap<>();

    public ShopManager(EldenCraft plugin) {
        this.plugin = plugin;
        loadShop();
    }

    private void loadShop() {
        shop.clear();
        File file = new File(plugin.getDataFolder(), "shop.yml");
        if (!file.exists()) plugin.saveResource("shop.yml", false);

        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        for (ClassType clazz : ClassType.values()) {
            List<Map<?, ?>> raw = cfg.getMapList(clazz.name());
            List<ShopItem> list = new ArrayList<>();
            for (Map<?, ?> entry : raw) {
                try {
                    String mat = String.valueOf(entry.get("material"));
                    Material material = Material.matchMaterial(mat);
                    if (material == null) {
                        plugin.getLogger().warning("[Shop] Material desconocido: " + mat);
                        continue;
                    }
                    int price = entry.containsKey("price") ? ((Number) entry.get("price")).intValue() : 100;
                    String name = entry.containsKey("name")
                            ? ChatColor.translateAlternateColorCodes('&', String.valueOf(entry.get("name")))
                            : material.name();

                    ItemStack stack = new ItemStack(material);
                    ItemMeta meta = stack.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName(name);
                        List<String> lore = new ArrayList<>();
                        if (entry.get("lore") instanceof List<?> ll) {
                            for (Object l : ll) {
                                lore.add(ChatColor.translateAlternateColorCodes('&', String.valueOf(l)));
                            }
                        }
                        lore.add("");
                        lore.add(ChatColor.GOLD + "Precio: " + price + " oro");
                        lore.add(ChatColor.YELLOW + "Click izq para comprar");
                        meta.setLore(lore);
                        stack.setItemMeta(meta);
                    }
                    list.add(new ShopItem(stack, price));
                } catch (Exception e) {
                    plugin.getLogger().warning("[Shop] Error cargando item: " + e.getMessage());
                }
            }
            shop.put(clazz, list);
        }
        plugin.getLogger().info("[EldenCraft] Tienda cargada para " + shop.size() + " clases.");
    }

    public List<ShopItem> getItemsForClass(ClassType clazz) {
        return shop.getOrDefault(clazz, new ArrayList<>());
    }

    public void reload() {
        loadShop();
    }
}
