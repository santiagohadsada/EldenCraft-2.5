package com.tuusuario.mmo.eldencraft.armor.listeners;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.armor.armor.ArmorCategory;
import com.tuusuario.mmo.eldencraft.armor.armor.ArmorManager;
import com.tuusuario.mmo.eldencraft.armor.armor.ArmorSlot;
import com.tuusuario.mmo.eldencraft.armor.stats.StatManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * Detecta TODOS los caminos por los que una armadura puede equiparse.
 * Si la clase no la permite cancela la accion y muestra el mensaje;
 * si por algun motivo se equipa, el tick de comprobacion la elimina.
 */
public final class ArmorListener implements Listener {

    private static final String DENY_MSG = ChatColor.RED + "Tu clase no puede usar este tipo de armadura";

    private final EldenCraft plugin;

    public ArmorListener(EldenCraft plugin) {
        this.plugin = plugin;
    }

    // ------------------------------------------------------------------
    //  Inventory click + shift click
    // ------------------------------------------------------------------

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;

        ItemStack moving = e.getCurrentItem();
        ArmorManager am = plugin.armorManager();

        // Caso 1: Shift-click desde inventario hacia slot de armadura
        if (e.getClick() == ClickType.SHIFT_LEFT || e.getClick() == ClickType.SHIFT_RIGHT) {
            if (moving != null && isArmorPiece(moving) && !am.canEquip(player, moving)) {
                e.setCancelled(true);
                player.sendMessage(DENY_MSG);
                return;
            }
        }

        // Caso 2: Click directo sobre slot de armadura (RawSlot 5..8)
        int raw = e.getRawSlot();
        if (raw >= 5 && raw <= 8 && e.getView().getTopInventory().getType() == InventoryType.CRAFTING) {
            ItemStack cursor = e.getCursor();
            if (cursor != null && isArmorPiece(cursor) && !am.canEquip(player, cursor)) {
                e.setCancelled(true);
                player.sendMessage(DENY_MSG);
                return;
            }
            if (e.getAction() == InventoryAction.HOTBAR_SWAP) {
                ItemStack hotbar = e.getView().getBottomInventory().getItem(e.getHotbarButton());
                if (hotbar != null && isArmorPiece(hotbar) && !am.canEquip(player, hotbar)) {
                    e.setCancelled(true);
                    player.sendMessage(DENY_MSG);
                }
            }
        }

        Bukkit.getScheduler().runTaskLater(plugin,
                () -> plugin.statManager().onEquipmentChange(player), 1L);
    }

    // ------------------------------------------------------------------
    //  Drag & drop multi-slot
    // ------------------------------------------------------------------

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDrag(InventoryDragEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        ItemStack item = e.getOldCursor();
        if (item == null || !isArmorPiece(item)) return;

        for (int slot : e.getRawSlots()) {
            if (slot >= 5 && slot <= 8) {
                if (!plugin.armorManager().canEquip(player, item)) {
                    e.setCancelled(true);
                    player.sendMessage(DENY_MSG);
                    return;
                }
            }
        }
    }

    // ------------------------------------------------------------------
    //  Right-click para auto-equipar
    // ------------------------------------------------------------------

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND && e.getHand() != EquipmentSlot.OFF_HAND) return;
        ItemStack item = e.getItem();
        if (item == null || !isArmorPiece(item)) return;
        if (!plugin.armorManager().canEquip(e.getPlayer(), item)) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(DENY_MSG);
        }
    }

    // ------------------------------------------------------------------
    //  Dispensers
    // ------------------------------------------------------------------

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDispense(BlockDispenseArmorEvent e) {
        Entity target = e.getTargetEntity();
        if (!(target instanceof Player player)) return;
        if (!plugin.armorManager().canEquip(player, e.getItem())) {
            e.setCancelled(true);
            player.sendMessage(DENY_MSG);
        }
    }

    // ------------------------------------------------------------------
    //  Daño combate: aplica stats RPG ofensivos/defensivos
    // ------------------------------------------------------------------

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        StatManager sm = plugin.statManager();
        double damage = e.getDamage();

        if (e.getDamager() instanceof Player attacker && e.getEntity() instanceof LivingEntity victim) {
            damage = sm.computeOutgoingDamage(attacker, victim, damage);
        }
        if (e.getEntity() instanceof Player victim) {
            damage = sm.computeIncomingDamage(victim, damage);
        }
        e.setDamage(damage);
    }

    // ------------------------------------------------------------------
    //  Sesion del jugador
    // ------------------------------------------------------------------

    // Captura definitiva: Paper dispara este evento ante CUALQUIER cambio
    // de armadura (click, drag, dispenser, /equip, comando, plugin externo).
    // No es cancelable, asi que devolvemos la pieza al inventario al siguiente tick.
    @EventHandler(priority = EventPriority.MONITOR)
    public void onArmorChange(PlayerArmorChangeEvent e) {
        Player player = e.getPlayer();
        ItemStack nuevo = e.getNewItem();
        if (nuevo == null || nuevo.getType() == Material.AIR) return;
        if (ArmorCategory.fromMaterial(nuevo.getType()) == null) return;
        if (plugin.armorManager().canEquip(player, nuevo)) return;

        final ItemStack copy = nuevo.clone();
        final PlayerArmorChangeEvent.SlotType st = e.getSlotType();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            var eq = player.getEquipment();
            if (eq == null) return;
            switch (st) {
                case HEAD  -> eq.setHelmet(null);
                case CHEST -> eq.setChestplate(null);
                case LEGS  -> eq.setLeggings(null);
                case FEET  -> eq.setBoots(null);
            }
            var overflow = player.getInventory().addItem(copy);
            overflow.values().forEach(it ->
                    player.getWorld().dropItemNaturally(player.getLocation(), it));
            player.sendMessage(DENY_MSG);
            plugin.statManager().applyInvalidPenalty(player);
            plugin.statManager().onEquipmentChange(player);
        }, 1L);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        plugin.statManager().onEquipmentChange(e.getPlayer());
        scheduleAntiExploitCheck(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        plugin.statManager().clear(e.getPlayer());
    }

    private void scheduleAntiExploitCheck(Player player) {
        long interval = plugin.armorConfig().checkIntervalTicks();
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline()) return;
            removeInvalidPieces(player);
        }, interval, interval);
    }

    private void removeInvalidPieces(Player player) {
        var eq = player.getEquipment();
        if (eq == null) return;
        ItemStack[] pieces = {eq.getHelmet(), eq.getChestplate(), eq.getLeggings(), eq.getBoots()};
        boolean changed = false;
        for (int i = 0; i < pieces.length; i++) {
            ItemStack p = pieces[i];
            if (p == null || p.getType() == Material.AIR) continue;
            if (ArmorCategory.fromMaterial(p.getType()) == null) continue;
            if (!plugin.armorManager().canEquip(player, p)) {
                if (plugin.armorConfig().removePieceOnInvalid()) {
                    Map<Integer, ItemStack> overflow = player.getInventory().addItem(p);
                    overflow.values().forEach(it -> player.getWorld().dropItemNaturally(player.getLocation(), it));
                    switch (i) {
                        case 0 -> eq.setHelmet(null);
                        case 1 -> eq.setChestplate(null);
                        case 2 -> eq.setLeggings(null);
                        case 3 -> eq.setBoots(null);
                    }
                    changed = true;
                }
                plugin.statManager().applyInvalidPenalty(player);
                player.sendMessage(DENY_MSG);
            }
        }
        if (changed) plugin.statManager().onEquipmentChange(player);
    }

    private boolean isArmorPiece(ItemStack item) {
        return item != null && ArmorSlot.fromMaterial(item.getType()) != null;
    }
}
