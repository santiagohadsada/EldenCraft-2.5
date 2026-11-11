package com.tuusuario.mmo.eldencraft.listeners;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.character.LevelUpManager;
import com.tuusuario.mmo.eldencraft.models.ClassType;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import com.tuusuario.mmo.eldencraft.rules.AttributeCalculator;
import com.tuusuario.mmo.eldencraft.spells.SpellSlotTable;
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

public class ClassSelectionGUI implements Listener {

    private final EldenCraft plugin;
    private final String title = "§8➤ §lSelecciona tu Clase";

    public ClassSelectionGUI(EldenCraft plugin) {
        this.plugin = plugin;
    }

    public void openClassMenu(Player player) {
        // 18 slots = 2 filas pa las 13 clases
        Inventory gui = Bukkit.createInventory(null, 18, title);

        // fila 1: las clases originales
        gui.setItem(0, createItem(Material.IRON_SWORD,        "§c§lGUERRERO",    "§7Maestros del cuerpo a cuerpo.",     "§6Recurso: Energia"));
        gui.setItem(1, createItem(Material.BLAZE_ROD,         "§b§lMAGO",        "§7Maestros de la magia arcana.",       "§3Recurso: Mana"));
        gui.setItem(2, createItem(Material.BOW,               "§a§lEXPLORADOR",  "§7Maestros de la caza y el bosque.",  "§6Recurso: Energia"));
        gui.setItem(3, createItem(Material.GOLDEN_CHESTPLATE, "§e§lPALADIN",     "§7Guerreros sagrados de la luz.",     "§3Recurso: Mana"));
        gui.setItem(4, createItem(Material.IRON_NUGGET,       "§8§lLADRON",      "§7Maestros del sigilo y veneno.",     "§6Recurso: Energia"));
        gui.setItem(5, createItem(Material.JUKEBOX,           "§d§lBARDO",       "§7Maestros de la música y encanto.",  "§3Recurso: Mana"));
        gui.setItem(6, createItem(Material.NETHERITE_AXE,     "§4§lBARBARO",     "§7Guerreros feroces sin límite.",     "§6Recurso: Energia"));

        // fila 2: las clases q metimos despues
        gui.setItem(9,  createItem(Material.BLAZE_POWDER,     "§5§lHECHICERO",   "§7Magia caótica y control de masas.", "§3Recurso: Mana"));
        gui.setItem(10, createItem(Material.WITHER_SKELETON_SKULL, "§8§lBRUJO",  "§7Pactos oscuros y magia de sombras.","§3Recurso: Mana"));
        gui.setItem(11, createItem(Material.STICK,            "§6§lMONJE",       "§7Combate sin armas con Ki puro.",    "§6Recurso: Energia"));
        gui.setItem(12, createItem(Material.OAK_SAPLING,      "§2§lDRUIDA",      "§7Controla la naturaleza y sus fieras.","§3Recurso: Mana"));
        gui.setItem(13, createItem(Material.GOLDEN_APPLE,     "§f§lCLERIGO",     "§7Soporte divino y curación masiva.", "§3Recurso: Mana"));
        gui.setItem(14, createItem(Material.CROSSBOW,         "§3§lPISTOLERO",   "§7Movilidad y daño a distancia rápido.","§6Recurso: Energia"));

        player.openInventory(gui);
    }

    private ItemStack createItem(Material material, String name, String desc, String recurso) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        List<String> lore = new ArrayList<>();
        lore.add(desc);
        lore.add(recurso);
        lore.add("");
        lore.add("§e¡Haz clic para elegir!");
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

        if      (name.contains("GUERRERO"))   data.setPlayerClass(ClassType.WARRIOR);
        else if (name.contains("MAGO"))       data.setPlayerClass(ClassType.MAGE);
        else if (name.contains("EXPLORADOR")) data.setPlayerClass(ClassType.RANGER);
        else if (name.contains("PALADIN"))    data.setPlayerClass(ClassType.PALADIN);
        else if (name.contains("LADRON"))     data.setPlayerClass(ClassType.ROGUE);
        else if (name.contains("BARDO"))      data.setPlayerClass(ClassType.BARD);
        else if (name.contains("BARBARO"))    data.setPlayerClass(ClassType.BARBARIAN);
        else if (name.contains("HECHICERO"))  data.setPlayerClass(ClassType.SORCERER);
        else if (name.contains("BRUJO"))      data.setPlayerClass(ClassType.WARLOCK);
        else if (name.contains("MONJE"))      data.setPlayerClass(ClassType.MONK);
        else if (name.contains("DRUIDA"))     data.setPlayerClass(ClassType.DRUID);
        else if (name.contains("CLERIGO"))    data.setPlayerClass(ClassType.CLERIC);
        else if (name.contains("PISTOLERO"))  data.setPlayerClass(ClassType.GUNSLINGER);
        else return;

        // F2: Asignar Standard Array si es nuevo
        if (!data.hasStandardArray()) {
            int[] stats = LevelUpManager.getStandardArrayForClass(data.getPlayerClass());
            data.setBaseStr(stats[0]);
            data.setBaseDex(stats[1]);
            data.setBaseCon(stats[2]);
            data.setBaseInt(stats[3]);
            data.setBaseWis(stats[4]);
            data.setBaseCha(stats[5]);
        }

        // F1/F2: Calcular PG D&D iniciales
        int conMod = AttributeCalculator.modifier(data.getEffectiveCon());
        int dndHp = LevelUpManager.calculateMaxHp(data.getPlayerClass(), data.getLevel(), conMod);
        data.setDndHpMax(dndHp);
        data.setDndHpCurrent(dndHp);

        // F3: Inicializar espacios de conjuro si es lanzador
        if (SpellSlotTable.isSpellcaster(data.getPlayerClass())) {
            int[] slots = SpellSlotTable.getSlots(data.getPlayerClass(), data.getLevel());
            System.arraycopy(slots, 0, data.getSpellSlotsMax(), 0, 9);
        }

        com.tuusuario.mmo.eldencraft.managers.ExperienceManager.applyMaxHealth(player, data);
        player.closeInventory();
        player.sendMessage("§a¡Ahora eres un " + name + "§a!");
        player.sendMessage("§7Tu aventura comienza ahora. Pulsa §f[F] §7para ver tus habilidades o usa §f/ficha §7para ver tus atributos.");
        player.sendMessage("§7Usa §f/missions §7para ver tus misiones y §f/shop §7para abrir la tienda.");
    }
}
