package com.tuusuario.mmo.eldencraft.character;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;

/**
 * Gestor del mundo Lobby de EldenCraft (SPEC 15.3 / F3B-1).
 */
public class LobbyManager {

    private final EldenCraft plugin;
    private final String lobbyWorldName;

    public LobbyManager(EldenCraft plugin) {
        this.plugin = plugin;
        this.lobbyWorldName = plugin.getConfig().getString("lobby.world-name", "ec_lobby");
    }

    public boolean isForceOnJoin() {
        return plugin.getConfig().getBoolean("lobby.force-on-join", true);
    }

    public boolean isRejoinAuto() {
        return plugin.getConfig().getBoolean("rejoin.auto", true);
    }

    public World getLobbyWorld() {
        World world = Bukkit.getWorld(lobbyWorldName);
        if (world == null) {
            try {
                plugin.getLogger().info("[LobbyManager] Creando/cargando mundo de lobby: " + lobbyWorldName);
                WorldCreator creator = new WorldCreator(lobbyWorldName);
                creator.type(WorldType.FLAT);
                world = creator.createWorld();
                if (world != null) {
                    world.setGameRule(org.bukkit.GameRule.DO_MOB_SPAWNING, false);
                    world.setGameRule(org.bukkit.GameRule.DO_DAYLIGHT_CYCLE, false);
                    world.setGameRule(org.bukkit.GameRule.DO_WEATHER_CYCLE, false);
                    world.setTime(6000L); // mediodía permanente
                }
            } catch (Exception e) {
                plugin.getLogger().warning("[LobbyManager] No se pudo crear " + lobbyWorldName + ": " + e.getMessage());
            }
        }
        if (world == null && !Bukkit.getWorlds().isEmpty()) {
            world = Bukkit.getWorlds().get(0);
        }
        return world;
    }

    public Location getLobbySpawn() {
        World w = getLobbyWorld();
        if (w == null) return null;
        double x = plugin.getConfig().getDouble("lobby.spawn.x", 0.5);
        double y = plugin.getConfig().getDouble("lobby.spawn.y", 65.0);
        double z = plugin.getConfig().getDouble("lobby.spawn.z", 0.5);
        float yaw = (float) plugin.getConfig().getDouble("lobby.spawn.yaw", 0.0);
        float pitch = (float) plugin.getConfig().getDouble("lobby.spawn.pitch", 0.0);
        return new Location(w, x, y, z, yaw, pitch);
    }

    public boolean isInLobby(Player player) {
        if (player == null || player.getWorld() == null) return false;
        World lobbyWorld = getLobbyWorld();
        return lobbyWorld != null && player.getWorld().getUID().equals(lobbyWorld.getUID());
    }

    public void sendToLobby(Player player) {
        if (player == null) return;
        Location spawn = getLobbySpawn();
        if (spawn != null) {
            player.teleport(spawn);
        }
        player.setGameMode(GameMode.ADVENTURE);
        player.setFoodLevel(20);
        player.setSaturation(20.0f);
        player.setHealth(player.getMaxHealth());
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
    }
}
