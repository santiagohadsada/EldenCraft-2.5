package com.tuusuario.mmo.eldencraft.managers;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

// da las recompensas cuando subis de nivel, se configura en el config
public class RewardManager {

    private final EldenCraft plugin;

    public RewardManager(EldenCraft plugin) {
        this.plugin = plugin;
    }

    public void giveLevelReward(Player player, PlayerData data, int level) {
        ConfigurationSection sec = plugin.getConfig()
                .getConfigurationSection("level-rewards." + level);
        if (sec == null) return;

        int gold = sec.getInt("gold", 0);
        if (gold > 0) {
            plugin.getEconomyManager().addGold(data, gold);
            player.sendMessage(ChatColor.GOLD + "Recompensa nivel " + level + ": +" + gold + " oro");
        }

        List<String> items = sec.getStringList("items");
        for (String entry : items) {
            ItemStack stack = parseItem(entry);
            if (stack != null) {
                player.getInventory().addItem(stack);
                player.sendMessage(ChatColor.GREEN + "Recibiste: "
                        + ChatColor.WHITE + entry);
            }
        }
    }

    // parsea un item de un string tipo "DIAMOND_SWORD"
    private ItemStack parseItem(String entry) {
        if (entry == null || entry.isEmpty()) return null;
        Material mat = Material.matchMaterial(entry);
        if (mat == null) {
            plugin.getLogger().warning("[Rewards] Material desconocido: " + entry);
            return null;
        }
        return new ItemStack(mat);
    }
}
