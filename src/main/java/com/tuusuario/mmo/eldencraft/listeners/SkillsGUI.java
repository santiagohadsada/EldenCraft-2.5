package com.tuusuario.mmo.eldencraft.listeners;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import com.tuusuario.mmo.eldencraft.skills.Skill;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

// Menu /skills: muestra las 7 habilidades del jugador con descripcion, coste, nivel necesario y estado (desbloqueada / bloqueada).
public class SkillsGUI implements Listener {

    public static final String TITLE = "§5§l✦ Habilidades §8(/skills)";

    private static final int[] SLOTS = {10, 11, 12, 13, 14, 15, 16};
    private static final int[] HITOS = {1, 3, 6, 9, 12, 16, 20};

    private final EldenCraft plugin;

    public SkillsGUI(EldenCraft plugin) {
        this.plugin = plugin;
    }

    public void open(Player player, PlayerData data) {
        Inventory gui = Bukkit.createInventory(null, 27, TITLE);

        // Relleno gris de fondo
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta paneMeta = pane.getItemMeta();
        paneMeta.setDisplayName(" ");
        pane.setItemMeta(paneMeta);
        for (int i = 0; i < 27; i++) gui.setItem(i, pane);

        for (int i = 0; i < 7; i++) {
            int slotNum = i + 1;
            int hito = HITOS[i];
            Skill skill = plugin.getSkillManager().getSkill(data.getPlayerClass(), slotNum);

            ItemStack item;
            ItemMeta meta;
            List<String> lore = new ArrayList<>();

            if (skill == null) {
                item = new ItemStack(Material.STRUCTURE_VOID);
                meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.DARK_GRAY + "Slot " + slotNum + " - Vacio");
                lore.add(ChatColor.GRAY + "Aun no hay habilidad asignada.");
            } else {
                boolean unlocked = data.getLevel() >= skill.getLevelRequired();
                item = new ItemStack(unlocked ? Material.ENCHANTED_BOOK : Material.BARRIER);
                meta = item.getItemMeta();
                String prefix = unlocked ? ChatColor.GREEN + "" + ChatColor.BOLD : ChatColor.DARK_RED + "";
                meta.setDisplayName(prefix + slotNum + ". " + skill.getName());

                lore.add(ChatColor.DARK_GRAY + "──────────────");
                for (String line : wrapText(skill.getDescription(), 40)) {
                    lore.add(ChatColor.GRAY + line);
                }
                lore.add(ChatColor.DARK_GRAY + "──────────────");
                lore.add(ChatColor.YELLOW + "Nivel requerido: " + ChatColor.WHITE + skill.getLevelRequired());
                lore.add(ChatColor.AQUA  + "Coste: "           + ChatColor.WHITE + (int) skill.getCost());
                lore.add(ChatColor.GOLD  + "Slot: "            + ChatColor.WHITE + "[" + slotNum + "]");
                lore.add("");
                if (unlocked) {
                    lore.add(ChatColor.GREEN + "✔ Desbloqueada");
                    lore.add(ChatColor.GRAY + "Pulsa F y la tecla "
                            + ChatColor.YELLOW + slotNum + ChatColor.GRAY + " en juego.");
                } else {
                    lore.add(ChatColor.RED + "✖ Bloqueada (Niv " + skill.getLevelRequired() + ")");
                }
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
            gui.setItem(SLOTS[i], item);
        }

        // Item informativo en slot 4 (parte superior)
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta im = info.getItemMeta();
        im.setDisplayName(ChatColor.GOLD + "Clase: " + data.getPlayerClass().displayName);
        List<String> ilore = new ArrayList<>();
        ilore.add(ChatColor.GRAY + "Nivel actual: " + ChatColor.WHITE + data.getLevel());
        ilore.add(ChatColor.GRAY + "Pulsa F en el juego para abrir el");
        ilore.add(ChatColor.GRAY + "menu rapido de habilidades.");
        im.setLore(ilore);
        info.setItemMeta(im);
        gui.setItem(4, info);

        // Indicador de Pasiva en slot 22
        ItemStack passiveItem = new ItemStack(Material.NETHER_STAR);
        ItemMeta passiveMeta = passiveItem.getItemMeta();
        passiveMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "✦ Pasiva de Clase: " + ChatColor.WHITE + getPassiveName(data.getPlayerClass()));
        List<String> passiveLore = new ArrayList<>();
        passiveLore.add(ChatColor.DARK_GRAY + "──────────────");
        for (String line : wrapText(getPassiveDescription(data.getPlayerClass()), 40)) {
            passiveLore.add(ChatColor.GRAY + line);
        }
        passiveLore.add(ChatColor.DARK_GRAY + "──────────────");
        passiveMeta.setLore(passiveLore);
        passiveItem.setItemMeta(passiveMeta);
        gui.setItem(22, passiveItem);

        player.openInventory(gui);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!TITLE.equals(e.getView().getTitle())) return;
        e.setCancelled(true);
    }

    private List<String> wrapText(String text, int max) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) return lines;
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();
        for (String word : words) {
            if (currentLine.length() + word.length() + 1 > max) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            } else {
                if (currentLine.length() > 0) currentLine.append(" ");
                currentLine.append(word);
            }
        }
        if (currentLine.length() > 0) lines.add(currentLine.toString());
        return lines;
    }

    private String getPassiveName(com.tuusuario.mmo.eldencraft.models.ClassType type) {
        if (type == null) return "Ninguna";
        return switch (type) {
            case WARRIOR -> "Sed de Batalla";
            case MAGE -> "Sabiduría Arcana";
            case RANGER -> "Instinto Natural";
            case PALADIN -> "Juicio Divino";
            case ROGUE -> "Golpe Crítico";
            case BARD -> "Armonía";
            case BARBARIAN -> "Ira Creciente";
            case SORCERER -> "Reacción Arcana";
            case WARLOCK -> "Pacto Oscuro";
            case MONK -> "Camino del Puño";
            case DRUID -> "Afinidad Natural";
            case CLERIC -> "Fe Absoluta";
            case GUNSLINGER -> "Gatillo Fácil";
            default -> "Desconocida";
        };
    }

    private String getPassiveDescription(com.tuusuario.mmo.eldencraft.models.ClassType type) {
        if (type == null) return "";
        return switch (type) {
            case WARRIOR -> "A menor vida, mayor daño cuerpo a cuerpo (hasta +25%).";
            case MAGE -> "Reduce costes de maná 20% y regenera maná extra.";
            case RANGER -> "Regenera energía y camina más rápido en bosques.";
            case PALADIN -> "Hace x2 daño a no-muertos y recibe 50% menos daño de ellos.";
            case ROGUE -> "Ataques por la espalda hacen un +30% de daño.";
            case BARD -> "Habilidades de soporte mejoradas y sin friendly fire a invocaciones.";
            case BARBARIAN -> "Al entrar en combate o recibir daño, gana velocidad de ataque y +20% daño.";
            case SORCERER -> "Recibir daño acumula cargas. A 5 cargas, la siguiente habilidad explota.";
            case WARLOCK -> "Puede lanzar habilidades con vida si no tiene maná. Sus invocaciones no dañan aliados.";
            case MONK -> "No puede usar espadas. A mano limpia aumenta daño y velocidad.";
            case DRUID -> "Recibe buffs según el bioma en el que se encuentre.";
            case CLERIC -> "Las curaciones en área aumentan su poder según la vida faltante del aliado.";
            case GUNSLINGER -> "Ataques básicos a distancia tienen probabilidad de causar sangrado.";
            default -> "Sin pasiva registrada.";
        };
    }
}
