package com.tuusuario.mmo.eldencraft.managers;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

// la economia del juego, tenemos nuestro propio sistema de oro, no usamos Vault
public class EconomyManager {

    private final EldenCraft plugin;

    public EconomyManager(EldenCraft plugin) {
        this.plugin = plugin;
    }

    // cuanta plata tiene
    public int getGold(PlayerData data) {
        return data.getGold();
    }

    // le da oro al jugador
    public void addGold(PlayerData data, int amount) {
        if (amount <= 0) return;
        data.setGold(data.getGold() + amount);
    }

    // le cobra oro, si no tiene retorna false
    public boolean removeGold(PlayerData data, int amount) {
        if (data.getGold() < amount) return false;
        data.setGold(data.getGold() - amount);
        return true;
    }

    // setea el oro directo, pa cuando un admin lo necesita
    public void setGold(PlayerData data, int amount) {
        data.setGold(Math.max(0, amount));
    }

    // mensajito de q te dieron oro
    public void notifyGold(Player player, int amount) {
        player.sendMessage(ChatColor.GOLD + "✦ +" + amount + " oro");
    }

    // multiplicador de oro q dropean los mobs, se lee del config
    public double getHostileDropMultiplier() {
        return plugin.getConfig().getDouble("economy.hostile-mob-drop-multiplier", 0.5);
    }

    // oro q te dan al empezar
    public int getStartingGold() {
        return plugin.getConfig().getInt("economy.starting-gold", 50);
    }

    // oro extra q recibes por subir de nivel
    public int getGoldPerLevel() {
        return plugin.getConfig().getInt("economy.gold-per-level", 100);
    }
}
