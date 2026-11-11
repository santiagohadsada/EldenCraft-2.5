package com.tuusuario.mmo.eldencraft.armor.command;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.armor.armor.ArmorCategory;
import com.tuusuario.mmo.eldencraft.armor.armor.ArmorRarity;
import com.tuusuario.mmo.eldencraft.armor.armor.ArmorSlot;
import com.tuusuario.mmo.eldencraft.armor.armor.ArmorType;
import com.tuusuario.mmo.eldencraft.armor.armor.RpgArmor;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

/**
 * Comandos administrativos del sistema de armaduras RPG:
 *
 *   /rpgarmor reload
 *   /rpgarmor give <type> <slot> <rarity> [upgrade]
 *   /rpgarmor info
 *
 * <p>La asignacion de clase del jugador se hace con /mmo elegir &lt;raza&gt; &lt;clase&gt;
 * del propio EldenCraft, no aqui.
 */
public final class RpgArmorCommand implements CommandExecutor {

    private final EldenCraft plugin;

    public RpgArmorCommand(EldenCraft plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("rpgarmor.admin")) {
            sender.sendMessage(ChatColor.RED + "Sin permisos.");
            return true;
        }
        if (args.length == 0) { sendHelp(sender); return true; }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                plugin.armorConfig().reload();
                sender.sendMessage(ChatColor.GREEN + "EldenCraft (modulo Armor) recargado.");
            }
            case "give" -> handleGive(sender, args);
            case "info" -> handleInfo(sender);
            case "diag" -> handleDiag(sender);
            default -> sendHelp(sender);
        }
        return true;
    }

    private void handleDiag(CommandSender sender) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Solo jugadores.");
            return;
        }
        sender.sendMessage(ChatColor.GOLD + "=== Diagnostico EldenCraft ===");

        PlayerData data = plugin.getPlayerManager().getPlayerData(p.getUniqueId());
        String className = data != null && data.getPlayerClass() != null
                ? data.getPlayerClass().name() : "(NINGUNA)";
        sender.sendMessage(ChatColor.YELLOW + "Tu clase: " + ChatColor.WHITE + className);

        Set<ArmorCategory> allowed = plugin.armorConfig().allowedCategories(className);
        sender.sendMessage(ChatColor.YELLOW + "Categorias permitidas: "
                + ChatColor.WHITE + allowed);

        ItemStack hand = p.getInventory().getItemInMainHand();
        if (hand != null && hand.getType().name().contains("_")) {
            ArmorCategory cat = ArmorCategory.fromMaterial(hand.getType());
            boolean isRpg = RpgArmor.isRpgArmor(hand);
            boolean canEquip = plugin.armorManager().canEquip(p, hand);
            sender.sendMessage(ChatColor.YELLOW + "Item en mano: " + ChatColor.WHITE + hand.getType());
            sender.sendMessage(ChatColor.YELLOW + "  - Categoria: " + ChatColor.WHITE + cat);
            sender.sendMessage(ChatColor.YELLOW + "  - Es RPG: " + ChatColor.WHITE + isRpg);
            sender.sendMessage(ChatColor.YELLOW + "  - Puedes equiparla: "
                    + (canEquip ? ChatColor.GREEN + "SI" : ChatColor.RED + "NO"));
        }

        int recipes = plugin.getStarterRecipes() != null
                ? plugin.getStarterRecipes().registeredCount() : 0;
        sender.sendMessage(ChatColor.YELLOW + "Recetas iniciales registradas: "
                + ChatColor.WHITE + recipes);
    }

    private void handleGive(CommandSender sender, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Solo jugadores.");
            return;
        }
        if (args.length < 4) {
            sender.sendMessage(ChatColor.YELLOW + "/rpgarmor give <type> <slot> <rarity> [upgrade]");
            return;
        }
        try {
            ArmorType type   = ArmorType.valueOf(args[1].toUpperCase());
            ArmorSlot slot   = ArmorSlot.valueOf(args[2].toUpperCase());
            ArmorRarity rar  = ArmorRarity.valueOf(args[3].toUpperCase());
            int up = args.length >= 5 ? Integer.parseInt(args[4]) : 0;

            ItemStack item = plugin.armorManager().generate(type, slot, rar, up);
            if (item == null) {
                sender.sendMessage(ChatColor.RED + "Combinacion invalida.");
                return;
            }
            p.getInventory().addItem(item);
            sender.sendMessage(ChatColor.GREEN + "Pieza entregada.");
        } catch (Exception ex) {
            sender.sendMessage(ChatColor.RED + "Argumentos invalidos: " + ex.getMessage());
        }
    }

    private void handleInfo(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== EldenCraft Armor ===");
        sender.sendMessage(ChatColor.GRAY + "Version: " + plugin.getDescription().getVersion());
        sender.sendMessage(ChatColor.GRAY + "Class manager: " + plugin.classManager().getClass().getSimpleName());
        sender.sendMessage(ChatColor.GRAY + "Mejora maxima: +" + plugin.armorConfig().maxUpgradeLevel());
        sender.sendMessage(ChatColor.GRAY + "Drops: " + plugin.armorConfig().dropsEnabled());
    }

    private void sendHelp(CommandSender s) {
        s.sendMessage(ChatColor.GOLD + "/rpgarmor reload");
        s.sendMessage(ChatColor.GOLD + "/rpgarmor give <type> <slot> <rarity> [upgrade]");
        s.sendMessage(ChatColor.GOLD + "/rpgarmor info");
        s.sendMessage(ChatColor.GOLD + "/rpgarmor diag" + ChatColor.GRAY + "  (diagnostico)");
    }
}
