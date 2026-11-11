package com.tuusuario.mmo.eldencraft.models;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.character.LevelUpManager;
import com.tuusuario.mmo.eldencraft.rules.AttributeCalculator;
import com.tuusuario.mmo.eldencraft.spells.SpellSlotTable;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

// aca guardamos los datos de los jugadores en ram, como un cache
public class PlayerManager {

    private final EldenCraft plugin;
    private final Map<UUID, PlayerData> players = new ConcurrentHashMap<>();

    public PlayerManager(EldenCraft plugin) {
        this.plugin = plugin;
    }

    // cuando el jugador entra, cargamos sus datos del disco
    public PlayerData loadPlayer(UUID uuid) {
        PlayerData data = plugin.getDataManager().load(uuid);

        // F2/F1: Migración automática de personajes existentes
        if (data.getPlayerClass() != null) {
            // 1. Asignar standard array si no lo tenía
            if (!data.hasStandardArray()) {
                int[] stats = LevelUpManager.getStandardArrayForClass(data.getPlayerClass());
                data.setBaseStr(stats[0]);
                data.setBaseDex(stats[1]);
                data.setBaseCon(stats[2]);
                data.setBaseInt(stats[3]);
                data.setBaseWis(stats[4]);
                data.setBaseCha(stats[5]);
            }

            // 2. Asignar PG D&D si no estaban calculados
            if (data.getDndHpMax() <= 0) {
                int conMod = AttributeCalculator.modifier(data.getEffectiveCon());
                int hp = LevelUpManager.calculateMaxHp(data.getPlayerClass(), data.getLevel(), conMod);
                data.setDndHpMax(hp);
                data.setDndHpCurrent(hp);
            }

            // 3. Inicializar espacios de conjuro si es lanzador
            if (SpellSlotTable.isSpellcaster(data.getPlayerClass())) {
                int[] slots = SpellSlotTable.getSlots(data.getPlayerClass(), data.getLevel());
                System.arraycopy(slots, 0, data.getSpellSlotsMax(), 0, 9);
            }
        }

        players.put(uuid, data);
        org.bukkit.entity.Player player = org.bukkit.Bukkit.getPlayer(uuid);
        if (player != null && data.getRace() != null) {
            org.bukkit.attribute.AttributeInstance scale = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_SCALE);
            if (scale != null) scale.setBaseValue(data.getRace().scale);
        }
        return data;
    }

    // cuando se va, guardamos y liberamos la memoria
    public void unloadPlayer(UUID uuid) {
        PlayerData data = players.remove(uuid);
        if (data != null) plugin.getDataManager().save(data);
    }

    // guarda todos los jugadores de una, se puede llamar asincronamente
    public void saveAll() {
        List<PlayerData> toSave = new ArrayList<>(players.values());
        for (PlayerData d : toSave) {
            plugin.getDataManager().save(d);
        }
    }

    public PlayerData getPlayerData(UUID uuid) {
        return players.get(uuid);
    }

    public void loadPlayerData(UUID uuid, PlayerData data) {
        if (uuid != null && data != null) {
            players.put(uuid, data);
        }
    }

    public void setPlayerData(UUID uuid, PlayerData data) {
        loadPlayerData(uuid, data);
    }

    public Map<UUID, PlayerData> getAll() {
        return players;
    }
}
