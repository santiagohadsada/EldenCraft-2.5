package com.tuusuario.mmo.eldencraft.armor.listeners;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

// Cuando muere un mob delego en el DropManager para ver si toca soltar
// alguna pieza RPG. La logica de cuales mobs y con que probabilidad
// vive en DropManager.handleMobDeath().
public final class MobDeathListener implements Listener {

    private final EldenCraft plugin;

    public MobDeathListener(EldenCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDeath(EntityDeathEvent e) {
        Player killer = e.getEntity().getKiller(); // puede ser null
        plugin.dropManager().handleMobDeath(e.getEntity(), killer);
    }
}
