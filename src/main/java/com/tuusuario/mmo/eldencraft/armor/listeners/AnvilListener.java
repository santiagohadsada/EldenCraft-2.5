package com.tuusuario.mmo.eldencraft.armor.listeners;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.armor.upgrade.UpgradeManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

// Engancha el yunque vanilla para mejorar piezas RPG con CUALQUIER material
// que actue como refuerzo. La logica gorda esta en UpgradeManager.
public final class AnvilListener implements Listener {

    private final EldenCraft plugin;

    public AnvilListener(EldenCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPrepare(PrepareAnvilEvent e) {
        final AnvilInventory inv = e.getInventory();
        ItemStack base = inv.getItem(0);
        ItemStack ing  = inv.getItem(1);
        UpgradeManager um = plugin.upgradeManager();

        if (!um.canUpgrade(base, ing)) return;

        final ItemStack result = um.upgrade(base, ing);
        if (result == null) return;
        final int cost = Math.max(1, um.experienceCost(base, ing));

        e.setResult(result);
        inv.setRepairCost(cost);

        // Vanilla puede recalcular el resultado tras nuestro evento;
        // forzamos el resultado al siguiente tick y refrescamos al jugador
        // para que el slot 2 muestre la pieza mejorada de verdad.
        Bukkit.getScheduler().runTask(plugin, () -> {
            inv.setItem(2, result);
            inv.setRepairCost(cost);
            for (HumanEntity h : inv.getViewers()) {
                if (h instanceof Player p) p.updateInventory();
            }
        });
    }

    @EventHandler
    public void onTake(InventoryClickEvent e) {
        if (!(e.getInventory() instanceof AnvilInventory inv)) return;
        if (e.getRawSlot() != 2) return;
        if (!(e.getWhoClicked() instanceof Player player)) return;
        ItemStack result = inv.getItem(2);
        if (result == null) return;

        // tras tomar el resultado, refrescar stats al ciclo siguiente
        Bukkit.getScheduler().runTaskLater(plugin,
                () -> plugin.statManager().onEquipmentChange(player), 1L);
    }
}
