package com.tuusuario.mmo.eldencraft.listeners;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.missions.MissionType;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerMoveEvent;

// Captura los eventos del juego para alimentar el progreso de las misiones (mata mobs, pica bloques, pesca, cria animales, viaja, craftea).
public class MissionProgressListener implements Listener {

    private final EldenCraft plugin;

    public MissionProgressListener(EldenCraft plugin) {
        this.plugin = plugin;
    }

    // kill
    @EventHandler
    public void onKill(EntityDeathEvent e) {
        if (e.getEntity().getKiller() == null) return;
        Player p = e.getEntity().getKiller();
        plugin.getMissionManager().addProgress(
                p, MissionType.KILL,
                e.getEntityType().name(), 1
        );
    }

    // gather (block break)
    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        plugin.getMissionManager().addProgress(
                p, MissionType.GATHER,
                e.getBlock().getType().name(), 1
        );
    }

    // fish
    @EventHandler
    public void onFish(PlayerFishEvent e) {
        if (e.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;
        plugin.getMissionManager().addProgress(
                e.getPlayer(), MissionType.FISH, "ANY", 1
        );
    }

    // breed
    @EventHandler
    public void onBreed(EntityBreedEvent e) {
        if (!(e.getBreeder() instanceof Player)) return;
        Player p = (Player) e.getBreeder();
        plugin.getMissionManager().addProgress(
                p, MissionType.BREED,
                e.getEntityType().name(), 1
        );
    }

    // craft
    @EventHandler
    public void onCraft(CraftItemEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (e.getRecipe() == null || e.getRecipe().getResult() == null) return;
        plugin.getMissionManager().addProgress(
                p, MissionType.CRAFT,
                e.getRecipe().getResult().getType().name(), 1
        );
    }

    // travel
    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Location from = e.getFrom();
        Location to = e.getTo();
        if (to == null) return;
        if (from.getBlockX() == to.getBlockX()
            && from.getBlockY() == to.getBlockY()
            && from.getBlockZ() == to.getBlockZ()) return;

        Player p = e.getPlayer();
        PlayerData d = plugin.getPlayerManager().getPlayerData(p.getUniqueId());
        if (d == null) return;
        // Para no spamear, anadimos 1 cada bloque cambiado
        plugin.getMissionManager().addProgress(p, MissionType.TRAVEL, "ANY", 1);
    }
}
