package com.tuusuario.mmo.eldencraft.listeners;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import com.tuusuario.mmo.eldencraft.models.RaceType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class SelectionGUI implements Listener {

    private final EldenCraft plugin;
    private final String title = "§8➤ §lSelecciona tu Raza";

    public SelectionGUI(EldenCraft plugin) {
        this.plugin = plugin;
    }

    public void openRaceMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 18, title);

        gui.setItem(0, createItem(Material.IRON_CHESTPLATE, "§f§lHUMANO", "§7Equilibrados y versátiles.", RaceType.HUMAN));
        gui.setItem(1, createItem(Material.IRON_PICKAXE,    "§6§lENANO",  "§7Resistentes y mineros.",     RaceType.DWARF));
        gui.setItem(2, createItem(Material.BOW,             "§a§lELFO",   "§7Ágiles y cazadores.",        RaceType.ELF));
        gui.setItem(3, createItem(Material.WHEAT,           "§e§lHOBBIT", "§7Pequeños y agrícolas.",      RaceType.HOBBIT));
        gui.setItem(4, createItem(Material.NETHERITE_AXE,   "§2§lORCO",   "§7Fuertes y agresivos.",       RaceType.ORC));
        gui.setItem(5, createItem(Material.CLOCK,           "§b§lGNOMO",  "§7Pequeños e inventores.",     RaceType.GNOME));
        gui.setItem(6, createItem(Material.BREAD,           "§e§lMEDIANO","§7Ágiles y sigilosos.",        RaceType.MEDIANO));
        gui.setItem(7, createItem(Material.BLAZE_POWDER,    "§c§lTIEFLING","§7Herencia infernal mágica.", RaceType.TIEFLING));
        gui.setItem(8, createItem(Material.STONE,           "§8§lGOLIAT", "§7Gigantes de las montañas.",  RaceType.GOLIATH));
        gui.setItem(9, createItem(Material.IRON_BLOCK,      "§7§lFORJADO","§7Constructos de guerra.",     RaceType.FORGED));
        gui.setItem(10, createItem(Material.ROTTEN_FLESH,   "§4§lNO-MUERTO","§7Levantados de la tumba.", RaceType.UNDEAD));
        gui.setItem(11, createItem(Material.GLOWSTONE_DUST, "§e§lAASIMAR","§7Sangre celestial radiante.", RaceType.AASIMAR));
        gui.setItem(12, createItem(Material.FEATHER, "§b§lAARAKOCRA","§7Humanoide pájaro, ágil y sabio.", RaceType.AARAKOCRA));

        player.openInventory(gui);
    }

    private ItemStack createItem(Material material, String name, String desc, RaceType race) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        List<String> lore = new ArrayList<>();
        lore.add(desc);
        lore.add("");
        lore.add("§8--- Estadísticas ---");
        lore.add("§c❤ Vida: §f" + (int)(race.baseHealth / 2) + " corazones");
        lore.add("§e⚔ Fuerza: §f"       + race.str);
        lore.add("§a🏃 Destreza: §f"    + race.dex);
        lore.add("§6🛡 Constitución: §f" + race.con);
        lore.add("§b✦ Inteligencia: §f" + race.intel);
        lore.add("§d👁 Sabiduría: §f"   + race.wis);
        lore.add("§f✿ Carisma: §f"      + race.cha);
        lore.add("");
        lore.add("§e¡Haz clic para seleccionar!");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(title)) return;
        event.setCancelled(true);

        if (event.getCurrentItem() == null) return;
        Player player = (Player) event.getWhoClicked();
        String name = event.getCurrentItem().getItemMeta().getDisplayName();

        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data == null) return;

        if      (name.contains("HUMANO")) data.setRace(RaceType.HUMAN);
        else if (name.contains("ENANO"))  data.setRace(RaceType.DWARF);
        else if (name.contains("ELFO"))   data.setRace(RaceType.ELF);
        else if (name.contains("HOBBIT")) data.setRace(RaceType.HOBBIT);
        else if (name.contains("ORCO"))   data.setRace(RaceType.ORC);
        else if (name.contains("GNOMO"))  data.setRace(RaceType.GNOME);
        else if (name.contains("MEDIANO")) data.setRace(RaceType.MEDIANO);
        else if (name.contains("TIEFLING")) data.setRace(RaceType.TIEFLING);
        else if (name.contains("GOLIAT")) data.setRace(RaceType.GOLIATH);
        else if (name.contains("FORJADO")) data.setRace(RaceType.FORGED);
        else if (name.contains("NO-MUERTO")) data.setRace(RaceType.UNDEAD);
        else if (name.contains("AASIMAR")) data.setRace(RaceType.AASIMAR);
        else if (name.contains("AARAKOCRA")) data.setRace(RaceType.AARAKOCRA);
        else return;

        org.bukkit.attribute.AttributeInstance scale = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_SCALE);
        if (scale != null && data.getRace() != null) scale.setBaseValue(data.getRace().scale);

        plugin.getFlyManager().sendFakeElytra(player, false);

        com.tuusuario.mmo.eldencraft.managers.ExperienceManager.applyMaxHealth(player, data);
        player.closeInventory();
        player.sendMessage("§aHas elegido la raza: " + name);

        Bukkit.getScheduler().runTaskLater(plugin, () ->
                new ClassSelectionGUI(plugin).openClassMenu(player), 5L);
    }
}
