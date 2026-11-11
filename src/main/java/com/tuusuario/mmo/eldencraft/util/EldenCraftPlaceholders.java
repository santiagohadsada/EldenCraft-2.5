package com.tuusuario.mmo.eldencraft.util;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class EldenCraftPlaceholders extends PlaceholderExpansion {

    private final EldenCraft plugin;

    public EldenCraftPlaceholders(EldenCraft plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "eldencraft";
    }

    @Override
    public @NotNull String getAuthor() {
        return "NaNO3 & Antigravity";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true; // Conservar el registro durante las recargas de PlaceholderAPI
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return "";

        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data == null) {
            // Si no está en línea, cargamos sus datos de forma síncrona/directa del disco/DB
            data = plugin.getDataManager().load(player.getUniqueId());
        }

        if (data == null) return "";

        switch (params.toLowerCase()) {
            case "class":
                return data.getPlayerClass() != null ? data.getPlayerClass().displayName : "Ninguna";
            case "race":
                return data.getRace() != null ? data.getRace().displayName : "Ninguna";
            case "level":
                return String.valueOf(data.getLevel());
            case "xp":
                return String.valueOf(data.getXp());
            case "gold":
                return String.valueOf(data.getGold());
            case "fatigue":
                return String.valueOf(data.getFatigue());
            case "resource":
                return String.valueOf((int) data.getCurrentResource());
            case "max_resource":
                return String.valueOf((int) data.getMaxResource());
            default:
                return null; // El placeholder especificado no existe
        }
    }
}
